'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    await queryInterface.sequelize.transaction(async (t) => {
      // users 表加头像 + 软删除字段
      await queryInterface.addColumn('users', 'avatar', {
        type: Sequelize.TEXT,
        allowNull: true,
        comment: '用户头像 DataURL (base64)'
      }, { transaction: t });

      await queryInterface.addColumn('users', 'deletedAt', {
        type: Sequelize.DATE,
        allowNull:true
      }, { transaction: t });

      // cards 和 saved_cards 加 tags 数组
      await queryInterface.addColumn('cards', 'tags', {
        type: Sequelize.ARRAY(Sequelize.STRING),
        defaultValue: [],
        allowNull: false
      }, { transaction: t });

      await queryInterface.addColumn('saved_cards', 'tags', {
        type: Sequelize.ARRAY(Sequelize.STRING),
        defaultValue: [],
        allowNull: false
      }, { transaction: t });
    });
  },

  down: async (queryInterface) => {
    await queryInterface.sequelize.transaction(async (t) => {
      await queryInterface.removeColumn('users', 'avatar', { transaction: t });
      await queryInterface.removeColumn('users', 'deletedAt', { transaction: t });
      await queryInterface.removeColumn('cards', 'tags', { transaction: t });
      await queryInterface.removeColumn('saved_cards', 'tags', { transaction: t });
    });
  }
};