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
      return res.status(400).json({ message: '用户已存在或该手机号已被注册' });
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
    const { username, phone, password } = req.body;

    // 1. 核心参数校验：密码必填 + 用户名/手机号至少填一个
    if (!password) {
      return res.status(400).json({ message: '请输入密码' });
    }
    // 用户名和手机号不能同时为空（至少传一个）
    if (!username && !phone) {
      return res.status(400).json({ message: '请输入用户名或手机号' });
    }

    // 2. 构建查询条件：匹配用户名 或 手机号（忽略空值）
    const queryCondition = {};
    if (username && phone) {
      // 两者都传：匹配任意一个（原逻辑保持不变）
      queryCondition[Op.or] = [{ username }, { phone }];
    } else if (username) {
      // 只传用户名：仅匹配用户名
      queryCondition.username = username;
    } else {
      // 只传手机号：仅匹配手机号
      queryCondition.phone = phone;
    }

    // 3. 查找用户（精确匹配，避免模糊查询导致的安全风险）
    const user = await User.findOne({ where: queryCondition });

    // 4. 验证用户存在 + 密码正确
    if (!user) {
      // 用户不存在（用户名/手机号未注册）
      return res.status(401).json({ message: '用户名/手机号未注册' });
    }
    const isPasswordValid = await user.matchPassword(password);
    if (!isPasswordValid) {
      // 密码错误
      return res.status(401).json({ message: '密码错误' });
    }

    // 5. 登录成功：返回用户信息 + Token
    res.json({
      id: user.id,
      username: user.username,
      phone: user.phone,
      token: generateToken(user.id)
    });

  } catch (err) {
    // 服务器异常处理
    console.error('登录接口错误：', err);
    res.status(500).json({ message: '服务器内部错误，请稍后重试' });
  }
};