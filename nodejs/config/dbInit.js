const { Client } = require('pg');
const sequelize = require('./db');
const User = require('../models/User');
const Card = require('../models/Card');
require('dotenv').config();

// 解析 DB_URI 连接信息
const parseDbUri = () => {
  const dbUri = process.env.DB_URI;
  if (!dbUri) throw new Error('请在.env文件中配置DB_URI');
  const match = dbUri.match(/postgres:\/\/([^:]+):([^@]+)@([^:]+):(\d+)\/(.+)/);
  if (!match) throw new Error('DB_URI格式错误，正确格式：postgres://用户名:密码@localhost:5432/数据库名');
  return {
    user: match[1],
    password: match[2],
    host: match[3],
    port: match[4],
    dbName: match[5],
    defaultDb: 'postgres'
  };
};

// 检查数据库是否已初始化
async function checkIfDatabaseInitialized() {
  let connection;
  try {
    // 创建新的数据库连接，避免使用可能已关闭的连接
    const { user, password, host, port, dbName } = parseDbUri();
    connection = new Client({
      user,
      password,
      host,
      port,
      database: dbName
    });
    
    await connection.connect();
    
    // 检查 users 表是否存在且有数据
    const usersTableResult = await connection.query(`
      SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'users'
      );
    `);
    
    if (!usersTableResult.rows[0].exists) {
      return false; // users 表不存在，需要初始化
    }
    
    // 检查 users 表中是否有数据
    const userCountResult = await connection.query(`SELECT COUNT(*) FROM users;`);
    
    if (parseInt(userCountResult.rows[0].count) === 0) {
      return false; // users 表存在但为空，需要初始化
    }
    
    // 检查 cards 表是否存在
    const cardsTableResult = await connection.query(`
      SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'cards'
      );
    `);
    
    if (!cardsTableResult.rows[0].exists) {
      return false; // cards 表不存在，需要初始化
    }
    
    // 所有检查通过，数据库已初始化
    console.log('ℹ️  数据库已存在且包含数据，跳过初始化');
    return true;
  } catch (error) {
    // 如果查询出错，假设数据库需要初始化
    console.log('ℹ️  检查数据库状态时出错，继续执行初始化:', error.message);
    return false;
  } finally {
    // 确保连接被正确关闭
    if (connection) {
      await connection.end().catch(err => {
        console.error('关闭检查连接时出错:', err.message);
      });
    }
  }
}

// 步骤1：创建目标数据库（若不存在）
async function createDatabaseIfNotExists() {
  const { user, password, host, port, dbName, defaultDb } = parseDbUri();
  const pgClient = new Client({
    user,
    password,
    host,
    port,
    database: defaultDb
  });

  try {
    await pgClient.connect();
    console.log(`✅ 已连接PostgreSQL默认数据库：${defaultDb}`);

    const res = await pgClient.query(
      'SELECT 1 FROM pg_database WHERE datname = $1',
      [dbName]
    );

    if (res.rows.length === 0) {
      await pgClient.query(`CREATE DATABASE "${dbName}"`);
      console.log(`✅ 目标数据库 ${dbName} 已创建`);
    } else {
      console.log(`ℹ️  目标数据库 ${dbName} 已存在，跳过创建`);
    }
  } catch (err) {
    if (err.code === '23505') {
      console.log(`ℹ️  目标数据库 ${dbName} 已存在（自动跳过）`);
    } else {
      throw new Error(`创建数据库失败：${err.message}`);
    }
  } finally {
    await pgClient.end();
  }
}

// 简化的残留检测（修复兼容性问题）
async function checkOldResidues() {
  let connection;
  try {
    // 创建新的数据库连接
    const { user, password, host, port, dbName } = parseDbUri();
    connection = new Client({
      user,
      password,
      host,
      port,
      database: dbName
    });
    
    await connection.connect();
    
    // 直接查询可能存在的表
    const tablesResult = await connection.query(`
      SELECT table_name 
      FROM information_schema.tables 
      WHERE table_schema = 'public' 
      AND table_name IN ('users', 'cards', 'Users', 'Cards', 'USER', 'CARD', 'USERS', 'CARDS')
    `);

    if (tablesResult.rows.length > 0) {
      const tableNames = tablesResult.rows.map(t => t.table_name).join('、');
      
      throw new Error(`
检测到冲突表：${tableNames}

请按以下步骤彻底清理：

方法1：使用SQL清理（推荐）
----------------------------------------
1. 使用pgAdmin连接到 ${dbName} 数据库
2. 在查询工具中执行以下SQL：

DROP TABLE IF EXISTS "users" CASCADE;
DROP TABLE IF EXISTS "cards" CASCADE;
DROP TABLE IF EXISTS "Users" CASCADE;
DROP TABLE IF EXISTS "Cards" CASCADE;
DROP TABLE IF EXISTS "USER" CASCADE;
DROP TABLE IF EXISTS "CARD" CASCADE;
DROP TABLE IF EXISTS "USERS" CASCADE;
DROP TABLE IF EXISTS "CARDS" CASCADE;

方法2：重建数据库（彻底）
----------------------------------------
1. 在postgres默认数据库中执行：
SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${dbName}' AND pid <> pg_backend_pid();
DROP DATABASE IF EXISTS "${dbName}";
CREATE DATABASE "${dbName}";

清理完成后重新运行：node config/dbInit.js
      `);
    }
  } catch (err) {
    // 如果检测失败，直接抛出错误提示重建数据库
    const { dbName } = parseDbUri();
    throw new Error(`
残留检测失败，建议直接重建数据库：

1. 使用pgAdmin连接到postgres默认数据库
2. 执行以下SQL：
SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '${dbName}' AND pid <> pg_backend_pid();
DROP DATABASE IF EXISTS "${dbName}";
CREATE DATABASE "${dbName}";

3. 重新运行：node config/dbInit.js
    `);
  } finally {
    if (connection) {
      await connection.end().catch(err => {
        console.error('关闭残留检测连接时出错:', err.message);
      });
    }
  }
}

// 步骤3：创建新表 + 插入测试数据
async function initTablesAndTestData() {
  // 创建新的 Sequelize 连接，避免使用可能已关闭的连接
  const { Sequelize } = require('sequelize');
  const { user, password, host, port, dbName } = parseDbUri();
  
  const localSequelize = new Sequelize(`postgres://${user}:${password}@${host}:${port}/${dbName}`, {
    dialect: 'postgres',
    logging: false,
    define: {
      freezeTableName: true,
      timestamps: true
    }
  });
  
  try {
    await localSequelize.authenticate();
    console.log('✅ 已为表创建连接目标数据库');

    // 强制锁定表名为小写，避免任何自动变体
    User._sequelize = localSequelize;
    Card._sequelize = localSequelize;
    
    User.tableName = 'users';
    Card.tableName = 'cards';
    
    // 禁用Sequelize表名复数化（双重保障）
    if (User.options) User.options.tableName = 'users';
    if (Card.options) Card.options.tableName = 'cards';

    // 同步模型（仅创建新表，禁用force，确保安全）
    await localSequelize.sync({ force: false });
    console.log('✅ 数据库表结构（users + cards）创建完成');

    // 创建测试用户
    const testUser = await User.create({
      username: 'testuser',
      phone: '13800138000',
      password: 'test123456'
    });
    console.log(`✅ 测试用户创建成功：用户名=${testUser.username}`);

    // 创建测试名片
    const testCard = await Card.create({
      name: '测试名片',
      department: '产品研发部',
      contact: { phone: '13800138000', email: 'test@example.com' },
      userId: testUser.id
    });
    console.log(`✅ 测试名片创建成功：ID=${testCard.id}`);

    console.log('\n🎉 数据库初始化全部完成！可直接启动项目测试接口');
    console.log(`📌 测试账号：用户名=testuser / 手机号=13800138000，密码=test123456`);
  } catch (err) {
    throw new Error(`创建表/测试数据失败：${err.message}`);
  } finally {
    // 关闭本地 Sequelize 连接
    if (localSequelize) {
      await localSequelize.close().catch(closeErr => {
        console.error('⚠️  关闭表创建连接失败：', closeErr.message);
      });
    }
  }
}

// 主流程：创建数据库 → 检查是否已初始化 → 检查冲突 → 建新表 → 插数据
async function initDatabase() {
  try {
    console.log('🚀 开始数据库初始化...');
    await createDatabaseIfNotExists(); // 步骤1：确保数据库存在
    
    // 检查数据库是否已初始化
    const isInitialized = await checkIfDatabaseInitialized();
    if (isInitialized) {
      console.log('✅ 数据库已初始化，跳过初始化流程');
      return;
    }
    
    // 检查残留
    await checkOldResidues();
    
    // 创建表和测试数据
    await initTablesAndTestData();
    
  } catch (err) {
    console.error('\n❌ 数据库初始化失败：', err.message);
    process.exit(1);
  }
}

// 执行初始化
if (require.main === module) {
  initDatabase();
}

module.exports = initDatabase;