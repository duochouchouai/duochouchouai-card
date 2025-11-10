const express = require('express');
const router = express.Router();
const { createCard, getMyCards, generateQrCode } = require('../router_handler/card_handler.js');
const { protect } = require('../middleware/authMiddleware');

// 创建名片（需认证）
router.post('/', protect, createCard);
// 获取个人名片列表（需认证）
router.get('/my', protect, getMyCards);
// 生成名片二维码（需认证）
router.post('/:cardId/qr', protect, generateQrCode);

module.exports = router;