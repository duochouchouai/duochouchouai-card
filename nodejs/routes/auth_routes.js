const express = require('express');
const router = express.Router();

// 1. 导入所有需要的 handler
const {
  register,
  login,
  changePassword,
  deleteAccount
} = require('../router_handler/user_handler');

// 2. 引入认证中间件
const { protect } = require('../middleware/authMiddleware');


// 用户注册
router.post('/register', register);

// 用户登录
router.post('/login', login);

// 修改密码（需要登录）
router.patch('/change-password', protect, changePassword);

// 注销账户（需要登录，会删除所有数据）
router.delete('/delete-account', protect, deleteAccount);

module.exports = router;