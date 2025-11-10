const express = require('express');
const router = express.Router();
const { register, login } = require('../router_handler/user_handler.js');

// 用户注册路由
router.post('/register', register);
// 用户登录路由
router.post('/login', login);

module.exports = router;