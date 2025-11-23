const User = require('../models/User');
const Card = require('../models/Card');
const SavedCard = require('../models/SavedCard');
const jwt = require('jsonwebtoken');
const asyncHandler = require('express-async-handler');
const { Op } = require('sequelize');

// 生成JWT令牌
const generateToken = (id) => {
  return jwt.sign({ id }, process.env.JWT_SECRET, { expiresIn: '30d' });
};

// 用户注册处理函数（保持你原来的完整实现）
exports.register = async (req, res) => {
  try {
    const { username, phone, password } = req.body;

    // 检查用户是否已存在（用户名或手机号任一重复即拒绝）
    const userExists = await User.findOne({
      where: { [Op.or]: [{ username }, { phone }] }
    });
    if (userExists) {
      return res.status(400).json({ message: '用户已存在或该手机号已被注册' });
    }

    // 密码加密由模型钩子自动完成
    const user = await User.create({ username, phone, password });

    res.status(201).json({
      id: user.id,
      username: user.username,
      phone: user.phone,
      token: generateToken(user.id)
    });
  } catch (err) {
    console.error('注册失败：', err);
    res.status(500).json({ message: err.message || '注册失败，请稍后重试' });
  }
};

// 用户登录处理函数（保持你原来的完整实现）
exports.login = async (req, res) => {
  try {
    const { username, phone, password } = req.body;

    if (!password) {
      return res.status(400).json({ message: '请输入密码' });
    }
    if (!username && !phone) {
      return res.status(400).json({ message: '请输入用户名或手机号' });
    }

    const queryCondition = {};
    if (username && phone) {
      queryCondition[Op.or] = [{ username }, { phone }];
    } else if (username) {
      queryCondition.username = username;
    } else {
      queryCondition.phone = phone;
    }

    const user = await User.findOne({ where: queryCondition });
    if (!user || !(await user.matchPassword(password))) {
      return res.status(401).json({ message: '用户名/手机号未注册或密码错误' });
    }

    res.json({
      id: user.id,
      username: user.username,
      phone: user.phone,
      token: generateToken(user.id)
    });
  } catch (err) {
    console.error('登录接口错误：', err);
    res.status(500).json({ message: '服务器内部错误，请稍后重试' });
  }
};

// 修改密码（需登录）
exports.changePassword = asyncHandler(async (req, res) => {
  const { oldPassword, newPassword } = req.body;

  if (!oldPassword || !newPassword) {
    return res.status(400).json({ message: '旧密码和新密码均为必填' });
  }
  if (newPassword.length < 6) {
    return res.status(400).json({ message: '新密码长度不能少于6位' });
  }

  const user = await User.findByPk(req.user.id);
  if (!user) {
    return res.status(404).json({ message: '用户不存在' });
  }

  const isMatch = await user.matchPassword(oldPassword);
  if (!isMatch) {
    return res.status(401).json({ message: '旧密码错误' });
  }

  user.password = newPassword; // 触发 beforeUpdate 钩子自动加密
  await user.save();

  res.json({ message: '密码修改成功' });
});

// 注销账户（需登录，删除用户及其所有名片和收藏记录）
exports.deleteAccount = asyncHandler(async (req, res) => {
  const userId = req.user.id;

  // 1. 先删除该用户的所有名片
  await Card.destroy({ where: { userId } });

  // 2. 删除该用户的所有收藏记录（别人收藏他的名片不会被删）
  await SavedCard.destroy({ where: { userId } });

  // 3. 最后删除用户本身
  await User.destroy({ where: { id: userId } });

  res.json({ message: '账户已彻底注销，所有数据已清除' });
});