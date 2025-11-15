const Card = require('../models/Card');
const qrcode = require('qrcode');
const path = require('path');
const fs = require('fs');
const sequelize = require('../config/db');
const SavedCard = require('../models/SavedCard');
const { Op } = require('sequelize');
// 1. 配置本地存储路径（无需手动创建，代码自动生成）
const QRCODE_STORAGE_DIR = path.join(__dirname, '../uploads/qrcode'); 
// 前端访问基础路径（需和你的 Express 静态托管配置一致）
const QRCODE_BASE_URL = '/qrcode';
// 创建名片处理函数
exports.createCard = async (req, res) => {
  try {
    const { name, department, contact } = req.body;
    const card = await Card.create({
      name,
      department,
      contact,
      userId: req.user.id
    });
    res.status(201).json(card);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// 获取个人名片列表处理函数
exports.getMyCards = async (req, res) => {
  try {
    const cards = await Card.findAll({
      where: { userId: req.user.id }
    });
    res.json(cards);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// 2. 自动创建存储目录（避免报错）
if (!fs.existsSync(QRCODE_STORAGE_DIR)) {
  fs.mkdirSync(QRCODE_STORAGE_DIR, { recursive: true });
  console.log(`二维码存储目录已创建：${QRCODE_STORAGE_DIR}`);
}

exports.generateQrCode = async (req, res) => {
  try {
    // 原有逻辑：查询当前用户的名片
    const card = await Card.findOne({
      where: { id: req.params.cardId, userId: req.user.id }
    });
    if (!card) return res.status(404).json({ message: '名片不存在' });

    // 原有逻辑：二维码内容（保持不变）
    const qrData = JSON.stringify({
      id: card.id,
      name: card.name,
      department: card.department,
      contact: card.contact
    });

    // 3. 生成唯一图片文件名（避免覆盖，用 cardId + 时间戳）
    const fileName = `card-${card.id}-${Date.now()}.png`;
    // 图片绝对路径（本地存储位置）
    const filePath = path.join(QRCODE_STORAGE_DIR, fileName);
    // 图片相对路径（存储到数据库）
    const relativePath = `${QRCODE_BASE_URL}/${fileName}`;
    // 图片完整访问 URL（返回给前端，方便直接使用）
    const qrCodeUrl = `http://localhost:3000${relativePath}`; // 3000 替换为你的后端端口

    // 4. 生成二维码并保存到本地（替换原有的 toDataURL）
    await qrcode.toFile(filePath, qrData, {
      width: 200, // 二维码尺寸（按需调整）
      margin: 1,
      errorCorrectionLevel: 'M' // 中等纠错等级，日常够用
    });

    // 5. 更新名片的 qrCode 字段（存储相对路径，而非 Base64）
    await card.update({ qrCode: relativePath });

    // 6. 返回结果（同时给相对路径和完整 URL，方便前端选择）
    res.json({
      qrCode: relativePath, // 数据库存储的相对路径
      qrCodeUrl: qrCodeUrl, // 完整访问 URL（前端直接渲染图片）
      message: '二维码生成成功'
    });

  } catch (err) {
    console.error('二维码生成失败：', err);
    res.status(500).json({ message: `二维码生成失败：${err.message}` });
  }
};
const { v4: uuidv4 } = require('uuid'); // 生成 UUID（cardUniqueId）
const { fileToBase64, getQrCodeFilePath } = require('../utils/CodeUtil');

/**
 * 展示个人名片二维码（业务逻辑核心）
 * @param {number} cardId - 卡片 ID
 * @param {number} userId - 当前登录用户 ID（从 Token 解析）
 * @returns {object} - 符合 Swagger 要求的返回数据
 */
exports.displayQrCode = async (req, res, next) => {
  try {
    // 1. 参数校验（按 Swagger 要求）
    const { cardId } = req.query;
    const errors = [];

    if (!cardId) {
      errors.push('名片ID不能为空');
    } else {
      const parsedCardId = parseInt(cardId, 10);
      if (isNaN(parsedCardId) || parsedCardId < 1) {
        errors.push('名片ID必须为正整数');
      }
    }

    if (errors.length > 0) {
      return res.status(400).json({
        code: 400,
        message: '参数错误',
        details: errors
      });
    }

    // 2. 获取当前登录用户 ID（依赖认证中间件挂载 req.user）
    const userId = req.user?.id;
    if (!userId) {
      return res.status(401).json({
        code: 401,
        message: '未授权，请先登录',
        details: []
      });
    }

    // 3. 查询卡片并校验权限
    const parsedCardId = parseInt(cardId, 10);
    const card = await Card.findOne({
      where: { id: parsedCardId, userId: userId }
    });

    if (!card) {
      return res.status(404).json({
        code: 404,
        message: '名片不存在',
        details: ['名片不存在或无访问权限']
      });
    }

    // 4. 二维码生成/读取 + Base64 转换
    const { filePath, storageDir } = getQrCodeFilePath(parsedCardId);
    let qrCodeBase64;

    // 创建存储目录（若不存在）
    if (!fs.existsSync(storageDir)) {
      fs.mkdirSync(storageDir, { recursive: true });
    }

    // 读取已有二维码或生成新二维码
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

      await qrcode.toFile(filePath, qrData, {
        width: 200,
        margin: 1,
        errorCorrectionLevel: 'M'
      });

      qrCodeBase64 = fileToBase64(filePath);
    }

    // 5. 处理 cardUniqueId（不存在则生成）
    let cardUniqueId = card.cardUniqueId;
    if (!cardUniqueId) {
      cardUniqueId = uuidv4();
      await card.update({ cardUniqueId });
    }

    // 6. 更新最后生成时间
    const lastGenerateTime = new Date();
    await card.update({ lastGenerateTime });

    // 7. 成功响应
    res.json({
      code: 200,
      message: '二维码获取成功',
      data: {
        qrCodeBase64,
        cardUniqueId,
        lastGenerateTime: lastGenerateTime.toISOString()
      }
    });

  } catch (error) {
    // 统一错误处理
    console.error('二维码展示失败：', error);
    res.status(500).json({
      code: 500,
      message: '服务器内部错误',
      details: process.env.NODE_ENV === 'development' ? [error.message] : []
    });
  }
};
const isUUID = (str) => {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  return uuidRegex.test(str);
};
exports.saveOtherCard = async (req, res) => {
  const t = await sequelize.transaction(); // 事务：确保数据一致性
  try {
    const { cardUniqueId, remark } = req.body;
    const userId = req.user?.id;
    const errors = [];

    // 1. 参数校验（按 Swagger 要求）
    if (!cardUniqueId) {
      errors.push('名片唯一标识不能为空');
    } else if (!isUUID(cardUniqueId)) {
      errors.push('名片唯一标识格式错误（需为UUID）');
    }
    if (remark && remark.length > 50) {
      errors.push('备注长度不能超过50字');
    }

    if (errors.length > 0) {
      await t.rollback();
      return res.status(400).json({ code: 400, message: '参数错误', details: errors });
    }

    // 2. 授权校验
    if (!userId) {
      await t.rollback();
      return res.status(401).json({ code: 401, message: '未授权，请先登录', details: [] });
    }

    // 3. 查询被收藏的名片是否存在（通过 cardUniqueId）
    const targetCard = await Card.findOne({
      where: { cardUniqueId },
      transaction: t
    });

    if (!targetCard) {
      await t.rollback();
      return res.status(404).json({
        code: 404,
        message: '资源不存在',
        details: ['该名片不存在或已被删除']
      });
    }

    // 4. 禁止保存自己的名片
    if (targetCard.userId === userId) {
      await t.rollback();
      return res.status(403).json({
        code: 403,
        message: '禁止访问',
        details: ['不可保存自己的名片']
      });
    }

    // 5. 检查是否已保存（防止重复收藏）
    const existingSaved = await SavedCard.findOne({
      where: { userId, targetCardId: targetCard.id },
      transaction: t
    });

    if (existingSaved) {
      await t.rollback();
      return res.status(409).json({
        code: 409,
        message: '资源冲突',
        details: ['你已保存过该名片，无需重复保存']
      });
    }

    // 6. 保存收藏记录
    const savedCard = await SavedCard.create({
      userId,
      targetCardId: targetCard.id,
      cardUniqueId,
      remark: remark || '',
      saveTime: new Date()
    }, { transaction: t });

    // 7. 整理返回数据（符合 Swagger 格式）
    const responseData = {
      savedCardId: savedCard.savedCardId,
      targetCardInfo: {
        cardId: targetCard.id,
        userName: targetCard.name,
        department: targetCard.department,
        contactPhone: targetCard.contact, // 假设 contact 字段存储手机号
        company: targetCard.company || '未填写' // 假设 Card 模型有 company 字段，无则默认
      }
    };

    await t.commit();
    res.json({
      code: 200,
      message: '名片保存成功',
      data: responseData
    });

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
exports.getSavedCards = async (req, res) => {
  try {
    const { page = 1, pageSize = 10, keyword = '' } = req.query;
    const userId = req.user?.id;
    const errors = [];

    // 1. 参数校验（按 Swagger 要求）
    const parsedPage = parseInt(page, 10);
    const parsedPageSize = parseInt(pageSize, 10);

    if (isNaN(parsedPage) || parsedPage < 1) {
      errors.push('页码必须为正整数');
    }
    if (isNaN(parsedPageSize) || parsedPageSize < 1 || parsedPageSize > 50) {
      errors.push('每页条数不能超过50');
    }

    if (errors.length > 0) {
      return res.status(400).json({ code: 400, message: '参数错误', details: errors });
    }

    // 2. 授权校验
    if (!userId) {
      return res.status(401).json({ code: 401, message: '未授权，请先登录', details: [] });
    }

    // 3. 构建查询条件（关联 SavedCard 和 Card，筛选当前用户收藏）
    const whereCondition = {
      userId // 只查当前用户的收藏记录
    };

    // 关键词搜索：匹配姓名、公司、备注（模糊查询）
    const cardWhere = {};
    if (keyword.trim()) {
      const likeKeyword = `%${keyword.trim()}%`;
      whereCondition[Op.or] = [
        { remark: { [Op.like]: likeKeyword } }, // 匹配备注
        sequelize.literal(`\`targetCard\`.name LIKE '${likeKeyword}'`), // 匹配姓名
        sequelize.literal(`\`targetCard\`.company LIKE '${likeKeyword}'`) // 匹配公司
      ];
    }

    // 4. 分页计算
    const offset = (parsedPage - 1) * parsedPageSize;

    // 5. 关联查询（收藏记录 + 被收藏的名片信息）
    const { count, rows: savedCards } = await SavedCard.findAndCountAll({
      where: whereCondition,
      include: [
        {
          model: Card,
          as: 'targetCard', // 对应模型关联的 as 名称
          attributes: [
            'cardUniqueId',
            'name',
            'department',
            'contact', // 手机号
          ],
          where: cardWhere
        }
      ],
      limit: parsedPageSize,
      offset: offset,
      order: [['saveTime', 'DESC']], // 按收藏时间倒序
      distinct: true // 确保 count 计数正确（关联查询时必填）
    });

    // 6. 整理返回数据（符合 Swagger 格式）
    const list = savedCards.map(item => ({
      savedCardId: item.savedCardId,
      remark: item.remark || '',
      saveTime: item.saveTime.toISOString(),
      cardInfo: {
        cardUniqueId: item.targetCard.cardUniqueId,
        userName: item.targetCard.name,
        department: item.targetCard.department || '未填写',
        contact: {
          phone: item.targetCard.contact || '未填写',
          email: item.targetCard.email || '未填写'
        },
        company: item.targetCard.company || '未填写'
      }
    }));

    const totalPage = Math.ceil(count / parsedPageSize);

    res.json({
      code: 200,
      message: '查询成功',
      data: {
        list,
        total: count,
        page: parsedPage,
        pageSize: parsedPageSize,
        totalPage
      }
    });

  } catch (error) {
    console.error('查询收藏名片列表失败：', error);
    res.status(500).json({
      code: 500,
      message: '服务器内部错误',
      details: process.env.NODE_ENV === 'development' ? [error.message] : []
    });
  }
};