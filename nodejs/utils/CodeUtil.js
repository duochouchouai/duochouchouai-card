const fs = require('fs');
const path = require('path');

/**
 * 本地文件转换为 Base64 字符串
 * @param {string} filePath - 文件绝对路径
 * @returns {string} - Base64 字符串（含 data:image/png;base64 前缀）
 */
exports.fileToBase64 = (filePath) => {
  try {
    // 读取文件二进制数据
    const buffer = fs.readFileSync(filePath);
    // 转换为 Base64 并拼接前缀
    return `data:image/png;base64,${buffer.toString('base64')}`;
  } catch (error) {
    throw new Error(`文件转 Base64 失败：${error.message}`);
  }
};

/**
 * 生成卡片二维码存储路径（复用之前的本地存储逻辑）
 * @param {number} cardId - 卡片 ID
 * @returns {object} - { storageDir: 存储目录, filePath: 文件绝对路径, relativePath: 相对路径 }
 */
exports.getQrCodeFilePath = (cardId) => {
  const PROJECT_ROOT = path.resolve(__dirname, '../../');
  const storageDir = path.join(PROJECT_ROOT, 'public', 'qrcode');
  // 假设二维码文件名格式：card-{cardId}-xxx.png（从数据库 qrCode 字段获取，这里简化为固定格式）
  const fileName = `card-${cardId}.png`;
  const filePath = path.join(storageDir, fileName);
  const relativePath = `/qrcode/${fileName}`;

  return { storageDir, filePath, relativePath };
};