const jwt = require('jsonwebtoken');
const User = require('../models/User');

exports.protect = async (req, res, next) => {
  let token;

  if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
    try {
      token = req.headers.authorization.split(' ')[1];
      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      // 从数据库查询用户并挂载到req上
      req.user = await User.findByPk(decoded.id);
      next();
    } catch (err) {
      res.status(401).json({ message: '认证失败，令牌无效' });
    }
  }

  if (!token) {
    res.status(401).json({ message: '未提供认证令牌' });
  }
};