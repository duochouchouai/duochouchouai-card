const { Model, DataTypes } = require('sequelize');
const sequelize = require('../config/db'); // 复用你的数据库配置
const User = require('./User'); // 已存在的 User 模型
const Card = require('./Card'); // 已存在的 Card 模型

// 继承 Model 类，保持与 Card 模型一致的风格
class SavedCard extends Model {}

// 初始化 SavedCard 模型（字段+配置）
SavedCard.init(
  {
    // 主键：自增 ID（与 Card 模型主键风格一致）
    savedCardId: {
      type: DataTypes.INTEGER,
      autoIncrement: true,
      primaryKey: true,
      comment: '收藏记录ID'
    },
    // 收藏者 ID（关联 User 表的 id）
    userId: {
      type: DataTypes.INTEGER,
      allowNull: false,
      comment: '收藏者用户ID（关联users表）'
    },
    // 被收藏的名片 ID（关联 Card 表的 id）
    targetCardId: {
      type: DataTypes.INTEGER,
      allowNull: false,
      comment: '被收藏的名片ID（关联cards表）'
    },
    // 被收藏名片的唯一标识（关联 Card 表的 cardUniqueId）
    cardUniqueId: {
      type: DataTypes.UUID,
      allowNull: false,
      comment: '被收藏名片的唯一标识（关联cards表的cardUniqueId）'
    },
    // 备注（最多50字，可选）
    remark: {
      type: DataTypes.STRING(50),
      allowNull: true,
      comment: '名片备注（不超过50字）'
    },
    // 收藏时间（默认当前时间，无需手动传入）
    saveTime: {
      type: DataTypes.DATE,
      allowNull: false,
      defaultValue: DataTypes.NOW,
      comment: '收藏时间'
    },
     tags: {
      type: DataTypes.ARRAY(DataTypes.STRING),
      allowNull: false,
      defaultValue: [],
      comment: '标签分类，如 ["个人名片","工作名片"]'
    }
  },
  {
    sequelize, // 复用数据库连接
    tableName: 'saved_cards', // 表名小写（与 cards 风格一致）
    freezeTableName: true, // 强制使用 tableName，不自动复数化
    timestamps: false, // 关闭默认时间戳（与 Card 模型一致）
    indexes: [
      // 联合唯一索引：防止同一用户重复收藏同一名片（核心约束）
      {
        unique: true,
        fields: ['userId', 'targetCardId'],
        name: 'idx_user_target_unique' // 索引名称（可选，便于维护）
      },
      // 普通索引：优化按 cardUniqueId 查询（提升接口性能）
      {
        fields: ['cardUniqueId'],
        name: 'idx_card_unique'
      }
    ]
  }
);

// 建立关联关系（与 User、Card 模型的关联，外部定义风格与 Card 一致）
// 1. User ↔ SavedCard：一对多（一个用户可收藏多个名片）
User.hasMany(SavedCard, {
  foreignKey: 'userId', // SavedCard 中的 userId 关联 User 的 id
  onDelete: 'CASCADE', // 用户删除时，关联的收藏记录也删除
  as: 'savedCards' // 别名（查询时用，可选）
});
SavedCard.belongsTo(User, {
  foreignKey: 'userId', // 关联字段一致
  as: 'collector' // 别名（查询被收藏者信息时用）
});

// 2. Card ↔ SavedCard：一对多（一个名片可被多个用户收藏）
Card.hasMany(SavedCard, {
  foreignKey: 'targetCardId', // SavedCard 中的 targetCardId 关联 Card 的 id
  onDelete: 'CASCADE', // 名片删除时，关联的收藏记录也删除
  as: 'savedRecords' // 别名（查询名片被谁收藏时用）
});
SavedCard.belongsTo(Card, {
  foreignKey: 'targetCardId', // 关联字段一致
  as: 'targetCard' // 别名（查询被收藏的名片信息时用）
});

module.exports = SavedCard;