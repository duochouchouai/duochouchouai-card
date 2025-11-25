const express = require('express');
const router = express.Router();
const { createCard, getMyCards, generateQrCode, displayQrCode, saveOtherCard, getSavedCards} = require('../router_handler/card_handler.js');
const { protect } = require('../middleware/authMiddleware');

// 创建名片（需认证）
router.post('/', protect, createCard);
// 获取个人名片列表（需认证）
router.get('/my', protect, getMyCards);
// 生成名片二维码（需认证）
router.post('/:cardId/qr_generate', protect, generateQrCode);
// 展示个人名片二维码（需认证）
router.get('/qr_display', protect, displayQrCode);
//保存他人名片（需认证）
router.post('/qr_save', protect, saveOtherCard);
//查询已保存的他人名片列表（需认证）
router.get('/saved', protect, getSavedCards);

// 更新头像及标签相关路由

const { 
  updateAvatar,
  addTagToCard, 
  removeTagFromCard,
  addTagToSavedCard, 
  removeTagFromSavedCard
} = require('../router_handler/card_handler');

router.patch('/avatar', protect, updateAvatar);
router.post('/:cardUniqueId/tags', protect, addTagToCard);
router.delete('/:cardUniqueId/tags/:tag', protect, removeTagFromCard);
router.post('/saved/:savedCardId/tags', protect, addTagToSavedCard);
router.delete('/saved/:savedCardId/tags/:tag', protect, removeTagFromSavedCard); 

module.exports = router;