const User = require('../models/User');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const { Op } = require('sequelize'); // 新增：导入Sequelize的Op操作符

// 生成JWT令牌
const generateToken = (id) => {
  return jwt.sign({ id }, process.env.JWT_SECRET, { expiresIn: '30d' });
};

// 用户注册处理函数
exports.register = async (req, res) => {
  try {
    const { username, phone, password } = req.body;
    // 检查用户是否已存在（使用Op.or实现多条件查询）
    const userExists = await User.findOne({
      where: { [Op.or]: [{ username }, { phone }] }
    });
    if (userExists) {
      return res.status(400).json({ message: '用户已存在' });
    }
    // 加密密码并创建用户（Model的beforeCreate钩子已处理加密，此处可简化）
    const user = await User.create({
      username,
      phone,
      password // 无需手动加密，Model钩子自动处理
    });
    res.status(201).json({
      id: user.id,
      username: user.username,
      phone: user.phone,
      token: generateToken(user.id)
    });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// 用户登录处理函数
exports.login = async (req, res) => {
  try {
    const { identifier, password } = req.body;
    // 查找用户（手机号/用户名匹配）
    const user = await User.findOne({
      where: { [Op.or]: [{ username: identifier }, { phone: identifier }] }
    });
    if (user && (await user.matchPassword(password))) { // 使用Model的matchPassword方法验证
      res.json({
        id: user.id,
        username: user.username,
        phone: user.phone,
        token: generateToken(user.id)
      });
    } else {
      res.status(401).json({ message: '账号或密码错误' });
    }
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};