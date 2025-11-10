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