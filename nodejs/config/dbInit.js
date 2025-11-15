const { Client } = require('pg');
const sequelize = require('./db');
const User = require('../models/User');
const Card = require('../models/Card');
const SavedCard = require('../models/SavedCard'); // 新增：导入 SavedCard 模型
require('dotenv').config();

// 解析 DB_URI 连接信息（原逻辑不变）
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

// 检查数据库是否已初始化（新增：检查 saved_cards 表）
async function checkIfDatabaseInitialized() {
  let connection;
  try {
    const { user, password, host, port, dbName } = parseDbUri();
    connection = new Client({
      user,
      password,
      host,
      port,
      database: dbName
    });
    
    await connection.connect();
    
    // 1. 检查 users 表是否存在且有数据
    const usersTableResult = await connection.query(`
      SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'users'
      );
    `);
    if (!usersTableResult.rows[0].exists) return false;

    const userCountResult = await connection.query(`SELECT COUNT(*) FROM users;`);
    if (parseInt(userCountResult.rows[0].count) === 0) return false;
    
    // 2. 检查 cards 表是否存在
    const cardsTableResult = await connection.query(`
      SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'cards'
      );
    `);
    if (!cardsTableResult.rows[0].exists) return false;
    
    // 新增：3. 检查 saved_cards 表是否存在（同步后需检查）
    const savedCardsTableResult = await connection.query(`
      SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'saved_cards'
      );
    `);
    if (!savedCardsTableResult.rows[0].exists) return false;
    
    console.log('ℹ️  数据库已存在且包含完整表结构+数据，跳过初始化');
    return true;
  } catch (error) {
    console.log('ℹ️  检查数据库状态时出错，继续执行初始化:', error.message);
    return false;
  } finally {
    if (connection) {
      await connection.end().catch(err => console.error('关闭检查连接时出错:', err.message));
    }
  }
}

// 步骤1：创建目标数据库（若不存在）（原逻辑不变）
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

// 简化的残留检测（新增：清理 saved_cards 相关冲突表）
async function checkOldResidues() {
  let connection;
  try {
    const { user, password, host, port, dbName } = parseDbUri();
    connection = new Client({
      user,
      password,
      host,
      port,
      database: dbName
    });
    
    await connection.connect();
    
    // 新增：包含 saved_cards 相关所有可能的冲突表名
    const tablesResult = await connection.query(`
      SELECT table_name 
      FROM information_schema.tables 
      WHERE table_schema = 'public' 
      AND table_name IN (
        'users', 'cards', 'saved_cards',
        'Users', 'Cards', 'SavedCards',
        'USER', 'CARD', 'SAVEDCARD',
        'USERS', 'CARDS', 'SAVED_CARDS'
      )
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

-- 清理用户表
DROP TABLE IF EXISTS "users" CASCADE;
DROP TABLE IF EXISTS "Users" CASCADE;
DROP TABLE IF EXISTS "USER" CASCADE;
DROP TABLE IF EXISTS "USERS" CASCADE;

-- 清理名片表
DROP TABLE IF EXISTS "cards" CASCADE;
DROP TABLE IF EXISTS "Cards" CASCADE;
DROP TABLE IF EXISTS "CARD" CASCADE;
DROP TABLE IF EXISTS "CARDS" CASCADE;

-- 清理收藏关联表（新增）
DROP TABLE IF EXISTS "saved_cards" CASCADE;
DROP TABLE IF EXISTS "SavedCards" CASCADE;
DROP TABLE IF EXISTS "SAVEDCARD" CASCADE;
DROP TABLE IF EXISTS "SAVED_CARDS" CASCADE;

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
      await connection.end().catch(err => console.error('关闭残留检测连接时出错:', err.message));
    }
  }
}

// 步骤3：创建新表 + 插入测试数据（核心修改：严格顺序同步模型）
async function initTablesAndTestData() {
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

    // 核心：所有模型绑定到当前 localSequelize 连接（新增 SavedCard）
    User._sequelize = localSequelize;
    Card._sequelize = localSequelize;
    SavedCard._sequelize = localSequelize; // 新增：绑定 SavedCard 模型
    
    // 强制锁定表名为小写（双重保障）
    User.tableName = 'users';
    Card.tableName = 'cards';
    SavedCard.tableName = 'saved_cards'; // 新增：锁定收藏表名
    
    if (User.options) User.options.tableName = 'users';
    if (Card.options) Card.options.tableName = 'cards';
    if (SavedCard.options) SavedCard.options.tableName = 'saved_cards'; // 新增

    // 关键修改：按依赖顺序单独同步（User → Card → SavedCard）
    // 1. 先同步 User 模型（无依赖，第一个）
    await User.sync({ force: false });
    console.log('✅ users 表创建完成');
    
    // 2. 再同步 Card 模型（依赖 User，第二个）
    await Card.sync({ force: false });
    console.log('✅ cards 表创建完成');
    
    // 3. 最后同步 SavedCard 模型（依赖 User + Card，第三个）
    await SavedCard.sync({ force: false });
    console.log('✅ saved_cards 表创建完成');

    console.log('✅ 数据库表结构（users + cards + saved_cards）创建完成');

    // 创建测试用户（原逻辑不变）
    const testUser = await User.create({
      username: 'testuser',
      phone: '13800138000',
      password: 'test123456'
    });
    console.log(`✅ 测试用户创建成功：用户名=${testUser.username}`);

    // 创建测试名片（原逻辑不变）
    const testCard = await Card.create({
      name: '测试名片',
      department: '产品研发部',
      contact: { phone: '13800138000', email: 'test@example.com' },
      userId: testUser.id,
      cardUniqueId: require('uuid').v4() // 新增：生成 UUID（适配收藏功能）
    });
    console.log(`✅ 测试名片创建成功：ID=${testCard.id}，cardUniqueId=${testCard.cardUniqueId}`);

    // 可选：创建测试收藏记录（让数据更完整）
    await SavedCard.create({
      userId: testUser.id,
      targetCardId: testCard.id,
      cardUniqueId: testCard.cardUniqueId,
      remark: '测试收藏-自己的名片（仅测试用，实际接口会禁止）'
    });
    console.log(`✅ 测试收藏记录创建成功`);

    console.log('\n🎉 数据库初始化全部完成！可直接启动项目测试接口');
    console.log(`📌 测试账号：用户名=testuser / 手机号=13800138000，密码=test123456`);
    console.log(`📌 测试名片cardUniqueId：${testCard.cardUniqueId}（可用于测试保存他人名片接口）`);
  } catch (err) {
    throw new Error(`创建表/测试数据失败：${err.message}`);
  } finally {
    if (localSequelize) {
      await localSequelize.close().catch(closeErr => {
        console.error('⚠️  关闭表创建连接失败：', closeErr.message);
      });
    }
  }
}

// 主流程（原逻辑不变，新增 SavedCard 相关检查）
async function initDatabase() {
  try {
    console.log('🚀 开始数据库初始化...');
    await createDatabaseIfNotExists(); // 步骤1：确保数据库存在
    
    // 检查数据库是否已初始化（含 saved_cards 表）
    const isInitialized = await checkIfDatabaseInitialized();
    if (isInitialized) {
      console.log('✅ 数据库已初始化，跳过初始化流程');
      return;
    }
    
    // 检查残留（含 saved_cards 表）
    await checkOldResidues();
    
    // 创建表和测试数据（严格顺序同步）
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