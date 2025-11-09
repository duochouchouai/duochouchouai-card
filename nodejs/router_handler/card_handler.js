const Card = require('../models/Card');
const qrcode = require('qrcode');

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

// 生成名片二维码处理函数
exports.generateQrCode = async (req, res) => {
  try {
    const card = await Card.findOne({
      where: { id: req.params.cardId, userId: req.user.id }
    });
    if (!card) return res.status(404).json({ message: '名片不存在' });
    // 生成二维码DataURL
    const qrData = JSON.stringify({
      id: card.id,
      name: card.name,
      department: card.department,
      contact: card.contact
    });
    const qrImage = await qrcode.toDataURL(qrData);
    // 更新名片二维码字段
    await card.update({ qrCode: qrImage });
    res.json({ qrCode: qrImage });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};