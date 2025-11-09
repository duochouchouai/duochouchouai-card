const express = require('express');
const router = express.Router();
const authRoutes = require('./auth_routes');
const cardRoutes = require('./card_routes');

// API版本前缀
const apiVersion = process.env.API_VERSION || 'v1';

// 挂载认证路由
router.use(`/api/${apiVersion}/auth`, authRoutes);

// 挂载名片路由
router.use(`/api/${apiVersion}/cards`, cardRoutes);

// 健康检查路由
router.get('/health', (req, res) => {
	res.status(200).json({
		status: 'ok',
		message: '服务运行正常',
		timestamp: new Date().toISOString()
	});
});

// 根路径路由
router.get('/', (req, res) => {
	res.status(200).json({
		message: '考核服务运行中',
		version: '1.0.0',
		api_version: apiVersion,
		status: 'running'
	});
});

module.exports = router;