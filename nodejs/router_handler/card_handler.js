const Card = require('../models/Card');
const qrcode = require('qrcode');
const path = require('path');
const fs = require('fs');
const sequelize = require('../config/db');
const SavedCard = require('../models/SavedCard');
const User = require('../models/User');
const asyncHandler = require('express-async-handler');
const { Op } = require('sequelize');
const { v4: uuidv4 } = require('uuid');
const { fileToBase64, getQrCodeFilePath } = require('../utils/CodeUtil');

// 1. 配置本地存储路径（无需手动创建，代码自动生成）
const QRCODE_STORAGE_DIR = path.join(__dirname, '../uploads/qrcode');
const QRCODE_BASE_URL = '/qrcode';

// 2. 自动创建存储目录（避免报错）
if (!fs.existsSync(QRCODE_STORAGE_DIR)) {
  fs.mkdirSync(QRCODE_STORAGE_DIR, { recursive: true });
  console.log(`二维码存储目录已创建：${QRCODE_STORAGE_DIR}`);
}

// 创建名片处理函数（支持前端传 tags）
exports.createCard = async (req, res) => {
  try {
    const { name, department, contact, tags = [] } = req.body; // 新增 tags 支持
    const card = await Card.create({
      name,
      department,
      contact,
      tags,                    // 写入标签字段
      userId: req.user.id
    });
    res.status(201).json(card);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// 获取个人名片列表（支持 ?search=关键词 & ?tag=工作名片）
exports.getMyCards = asyncHandler(async (req, res) => {
  const { search, tag } = req.query;

  const where = { userId: req.user.id };

  // 标签筛选（PostgreSQL 数组包含）
  if (tag) {
    where.tags = { [Op.contains]: [tag] };
  }

  // 关键词模糊搜索（姓名、部门、公司、邮箱、电话）
  if (search) {
    where[Op.or] = [
      { name: { [Op.iLike]: `%${search}%` } },
      { department: { [Op.iLike]: `%${search}%` } },
      sequelize.where(
        sequelize.cast(sequelize.col('contact->>\'company\''), 'text'),
        { [Op.iLike]: `%${search}%` }
      ),
      sequelize.where(
        sequelize.cast(sequelize.col('contact->>\'email\''), 'text'),
        { [Op.iLike]: `%${search}%` }
      ),
      sequelize.where(
        sequelize.cast(sequelize.col('contact->>\'phone\''), 'text'),
        { [Op.iLike]: `%${search}%` }
      ),
    ];
  }

  const cards = await Card.findAll({
    where,
    include: [{ model: User, attributes: ['username', 'avatar'] }],
    order: [['name', 'ASC']]
  });

  res.json(cards);
});

// 生成二维码（保持你原来的完整实现不变）
exports.generateQrCode = async (req, res) => {
  try {
    const card = await Card.findOne({
      where: { id: req.params.cardId, userId: req.user.id }
    });
    if (!card) return res.status(404).json({ message: '名片不存在' });

    const qrData = JSON.stringify({
      id: card.id,
      name: card.name,
      department: card.department,
      contact: card.contact
    });

    const fileName = `card-${card.id}-${Date.now()}.png`;
    const filePath = path.join(QRCODE_STORAGE_DIR, fileName);
    const relativePath = `${QRCODE_BASE_URL}/${fileName}`;
    const qrCodeUrl = `http://localhost:3000${relativePath}`;

    await qrcode.toFile(filePath, qrData, {
      width: 200,
      margin: 1,
      errorCorrectionLevel: 'M'
    });

    await card.update({ qrCode: relativePath });

    res.json({
      qrCode: relativePath,
      qrCodeUrl,
      message: '二维码生成成功'
    });
  } catch (err) {
    console.error('二维码生成失败：', err);
    res.status(500).json({ message: `二维码生成失败：${err.message}` });
  }
};

// 展示个人名片二维码（保持你原来的完整实现不变）
exports.displayQrCode = async (req, res, next) => {
  try {
    const { cardId } = req.query;
    const errors = [];

    if (!cardId) errors.push('名片ID不能为空');
    else {
      const parsedCardId = parseInt(cardId, 10);
      if (isNaN(parsedCardId) || parsedCardId < 1) errors.push('名片ID必须为正整数');
    }

    if (errors.length > 0) {
      return res.status(400).json({ code: 400, message: '参数错误', details: errors });
    }

    const userId = req.user?.id;
    if (!userId) {
      return res.status(401).json({ code: 401, message: '未授权，请先登录', details: [] });
    }

    const parsedCardId = parseInt(cardId, 10);
    const card = await Card.findOne({ where: { id: parsedCardId, userId } });

    if (!card) {
      return res.status(404).json({
        code: 404,
        message: '名片不存在',
        details: ['名片不存在或无访问权限']
      });
    }

    const { filePath, storageDir } = getQrCodeFilePath(parsedCardId);
    let qrCodeBase64;

    if (!fs.existsSync(storageDir)) {
      fs.mkdirSync(storageDir, { recursive: true });
    }

    if (fs.existsSync(filePath)) {
      qrCodeBase64 = fileToBase64(filePath);
    } else {
      const qrData = JSON.stringify({
        id: card.id,
        name: card.name,
        department: card.department,
        contact: card.contact,
        cardUniqueId: card.cardUniqueId || uuidv4()
      });

      await qrcode.toFile(filePath, qrData, { width: 200, margin: 1, errorCorrectionLevel: 'M' });
      qrCodeBase64 = fileToBase64(filePath);
    }

    let cardUniqueId = card.cardUniqueId;
    if (!cardUniqueId) {
      cardUniqueId = uuidv4();
      await card.update({ cardUniqueId });
    }

    const lastGenerateTime = new Date();
    await card.update({ lastGenerateTime });

    res.json({
      code: 200,
      message: '二维码获取成功',
      data: { qrCodeBase64, cardUniqueId, lastGenerateTime: lastGenerateTime.toISOString() }
    });
  } catch (error) {
    console.error('二维码展示失败：', error);
    res.status(500).json({
      code: 500,
      message: '服务器内部错误',
      details: process.env.NODE_ENV === 'development' ? [error.message] : []
    });
  }
};

// 保存他人名片（保持你原来的完整实现不变）
const isUUID = (str) => {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  return uuidRegex.test(str);
};

exports.saveOtherCard = async (req, res) => {
  const t = await sequelize.transaction();
  try {
    const { cardUniqueId, remark } = req.body;
    const userId = req.user?.id;
    const errors = [];

    if (!cardUniqueId) errors.push('名片唯一标识不能为空');
    else if (!isUUID(cardUniqueId)) errors.push('名片唯一标识格式错误（需为UUID）');
    if (remark && remark.length > 50) errors.push('备注长度不能超过50字');

    if (errors.length > 0) {
      await t.rollback();
      return res.status(400).json({ code: 400, message: '参数错误', details: errors });
    }

    if (!userId) {
      await t.rollback();
      return res.status(401).json({ code: 401, message: '未授权，请先登录', details: [] });
    }

    const targetCard = await Card.findOne({ where: { cardUniqueId }, transaction: t });
    if (!targetCard) {
      await t.rollback();
      return res.status(404).json({ code: 404, message: '资源不存在', details: ['该名片不存在或已被删除'] });
    }

    if (targetCard.userId === userId) {
      await t.rollback();
      return res.status(403).json({ code: 403, message: '禁止访问', details: ['不可保存自己的名片'] });
    }

    const existingSaved = await SavedCard.findOne({
      where: { userId, targetCardId: targetCard.id },
      transaction: t
    });

    if (existingSaved) {
      await t.rollback();
      return res.status(409).json({ code: 409, message: '资源冲突', details: ['你已保存过该名片，无需重复保存'] });
    }

    const savedCard = await SavedCard.create({
      userId,
      targetCardId: targetCard.id,
      cardUniqueId,
      remark: remark || '',
      saveTime: new Date()
    }, { transaction: t });

    const responseData = {
      savedCardId: savedCard.savedCardId,
      targetCardInfo: {
        cardId: targetCard.id,
        userName: targetCard.name,
        department: targetCard.department,
        contactPhone: targetCard.contact,
        company: targetCard.company || '未填写'
      }
    };

    await t.commit();
    res.json({ code: 200, message: '名片保存成功', data: responseData });
  } catch (error) {
    await t.rollback();
    console.error('保存他人名片失败：', error);
    res.status(500).json({
      code: 500,
      message: '服务器内部错误',
      details: process.env.NODE_ENV === 'development' ? [error.message] : []
    });
  }
};

// 获取已保存的名片列表（支持 ?search=关键词 & ?tag=社团名片）
exports.getSavedCards = asyncHandler(async (req, res) => {
  const { search, tag } = req.query;

  const where = { userId: req.user.id };

  // 标签筛选
  if (tag) where.tags = { [Op.contains]: [tag] };

  // 关键词搜索
  if (search) {
    where[Op.or] = [
      { remark: { [Op.iLike]: `%${search}%` } },
      { '$targetCard.name$': { [Op.iLike]: `%${search}%` } },
      { '$targetCard.department$': { [Op.iLike]: `%${search}%` } },
      sequelize.where(
        sequelize.cast(sequelize.col('targetCard.contact->>\'company\''), 'text'),
        { [Op.iLike]: `%${search}%` }
      ),
      sequelize.where(
        sequelize.cast(sequelize.col('targetCard.contact->>\'email\''), 'text'),
        { [Op.iLike]: `%${search}%` }
      ),
    ];
  }

  const savedCards = await SavedCard.findAll({
    where,
    include: [{
      model: Card,
      as: 'targetCard',
      include: [{ model: User, attributes: ['username', 'avatar'] }]
    }],
    order: [['saveTime', 'DESC']]
  });

  // 直接返回原始数据结构（前端已适配）
  res.json(savedCards);
});

// ==================== 新增功能 ====================

// 上传个人头像（DataURL）
exports.updateAvatar = asyncHandler(async (req, res) => {
  const { avatar } = req.body;
  if (!avatar || !avatar.startsWith('data:image/')) {
    return res.status(400).json({ message: '头像格式错误，必须为 dataURL' });
  }
  await User.update({ avatar }, { where: { id: req.user.id } });
  res.json({ message: '头像上传成功', avatar });
});

// 给自己的名片添加标签
exports.addTagToCard = asyncHandler(async (req, res) => {
  const { cardUniqueId } = req.params;
  const { tag } = req.body;
  if (!tag) return res.status(400).json({ message: '标签不能为空' });

  const card = await Card.findOne({ where: { cardUniqueId, userId: req.user.id } });
  if (!card) return res.status(404).json({ message: '名片不存在或无权限' });

  if (!card.tags.includes(tag)) {
    card.tags = [...card.tags, tag];
    await card.save();
  }
  res.json({ message: '标签添加成功', tags: card.tags });
});

// 移除自己的名片标签
exports.removeTagFromCard = asyncHandler(async (req, res) => {
  const { cardUniqueId, tag } = req.params;

  const card = await Card.findOne({ where: { cardUniqueId, userId: req.user.id } });
  if (!card) return res.status(404).json({ message: '名片不存在或无权限' });

  card.tags = card.tags.filter(t => t !== tag);
  await card.save();
  res.json({ message: '标签已移除', tags: card.tags });
});

// 给收藏的名片添加标签
exports.addTagToSavedCard = asyncHandler(async (req, res) => {
  const { savedCardId } = req.params;
  const { tag } = req.body;
  if (!tag) return res.status(400).json({ message: '标签不能为空' });

  const saved = await SavedCard.findOne({
    where: { savedCardId: Number(savedCardId), userId: req.user.id }
  });
  if (!saved) return res.status(404).json({ message: '收藏记录不存在' });

  if (!saved.tags.includes(tag)) {
    saved.tags = [...saved.tags, tag];
    await saved.save();
  }
  res.json({ message: '标签添加成功', tags: saved.tags });
});

// 移除收藏名片的标签
exports.removeTagFromSavedCard = asyncHandler(async (req, res) => {
  const { savedCardId, tag } = req.params;

  const saved = await SavedCard.findOne({
    where: { savedCardId: Number(savedCardId), userId: req.user.id }
  });
  if (!saved) return res.status(404).json({ message: '收藏记录不存在' });

  saved.tags = saved.tags.filter(t => t !== tag);
  await saved.save();
  res.json({ message: '标签已移除', tags: saved.tags });
});

// 导出所有处理函数
module.exports = {
  createCard: exports.createCard,
  getMyCards: exports.getMyCards,
  generateQrCode: exports.generateQrCode,
  displayQrCode: exports.displayQrCode,
  saveOtherCard: exports.saveOtherCard,
  getSavedCards: exports.getSavedCards,
  updateAvatar: exports.updateAvatar,
  addTagToCard: exports.addTagToCard,
  removeTagFromCard: exports.removeTagFromCard,
  addTagToSavedCard: exports.addTagToSavedCard,
  removeTagFromSavedCard: exports.removeTagFromSavedCard  
};