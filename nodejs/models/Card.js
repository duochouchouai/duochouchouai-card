const { Model, DataTypes } = require('sequelize');
const sequelize = require('../config/db');
const User = require('./User');

class Card extends Model {}

Card.init(
  {
    name: {
      type: DataTypes.STRING,
      allowNull: false,
    },
    department: {
      type: DataTypes.STRING,
    },
    contact: {
      type: DataTypes.JSONB, // 存储电话、邮箱等JSON结构
      allowNull: false,
    },
    qrCode: {
      type: DataTypes.STRING, // 存储二维码DataURL
    },
    cardUniqueId: {
      type: DataTypes.UUID, // 字段类型为 UUID
      allowNull: false, // 非空约束（和 SavedCard 一致）
      unique: true, // 唯一约束（避免重复）
      defaultValue: DataTypes.UUIDV4, // 自动生成 UUID V4（关键！）
      comment: '名片唯一标识（UUID）'
    }
  },
  {
    sequelize,
    tableName: 'cards', // 强制表名小写
    freezeTableName: true,
    timestamps: false
  }
);

// 建立关联：User 与 Card 是一对多关系
User.hasMany(Card, {
  foreignKey: 'userId',
  onDelete: 'CASCADE',
});
Card.belongsTo(User, {
  foreignKey: 'userId',
});

module.exports = Card;