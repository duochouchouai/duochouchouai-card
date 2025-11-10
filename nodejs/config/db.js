require('dotenv').config();
const { Sequelize } = require('sequelize');

// 从环境变量中获取 PostgreSQL 连接信息
const dbUri = process.env.DB_URI;

// 初始化 Sequelize 实例
const sequelize = new Sequelize(dbUri, {
  logging: false, // 关闭 SQL 日志输出（开发环境可改为 console.log 便于调试）
});

module.exports = sequelize;