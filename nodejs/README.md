# 名片管理系统后端（duochouchouai-card）

一个基于 **Node.js + PostgreSQL** 开发的名片管理后端系统。

仓库地址：https://github.com/duochouchouai/duochouchouai-card

## 已实现功能

- 用户注册 / 登录（支持用户名或手机号任选其一登录）
- 修改密码
- 注销账户（自动删除所有名片与收藏记录）
- 上传个人头像（DataURL 存储）
- 创建多张个人名片
- 为自己的名片添加/删除分类标签（如“个人名片”、“工作名片”、“社团名片”）
- 按标签筛选查看名片
- 关键词模糊搜索（支持姓名、部门、公司等）
- 生成个人名片二维码
- 扫描他人二维码一键保存名片
- Swagger 在线文档

## 技术栈

- Node.js
- PostgreSQL + Sequelize
- JWT 身份认证
- Swagger UI 在线文档
- qrcode 二维码生成

## 环境要求

- Node.js ≥ 18.x
- PostgreSQL ≥ 13

## 项目根目录

```
nodejs/
├── config/           # 数据库配置
├── docs/             # Swagger 文档
├── middleware/       # 认证中间件（protect）
├── migrations/       # Sequelize 迁移文件
├── models/           # 数据模型（User、Card、SavedCard）
├── node_modules/     # 依赖包
├── routes/           # 路由定义
├── router_handler/   # 业务逻辑处理
├── uploads/
│   └── qrcode/       # 二维码图片存储目录（自动创建）
├── utils/            # 工具函数
├── .env              # 环境变量
├── .gitignore
├── .sequelizerc      # Sequelize CLI 配置
├── LICENSE
├── package-lock.json
├── package.json
├── README.md
├── server.js         # 项目入口
└── swagger.js        # Swagger 配置与挂载
```

## 快速开始

```bash
# 1. 克隆项目
git clone https://github.com/duochouchouai/duochouchouai-card.git

cd duochouchouai-card/nodejs

# 2. 安装依赖
npm install

# 3. 配置环境变量
cp .env.example .env
# 编辑 .env，填写你的数据库连接信息
# DB_URI=postgres://用户名:密码@localhost:5432/数据库名

# 4. 补充数据库字段
psql -d 你的数据库名 -c "
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS \"deletedAt\" TIMESTAMP;
ALTER TABLE cards ADD COLUMN IF NOT EXISTS tags TEXT[] DEFAULT '{}';
ALTER TABLE saved_cards ADD COLUMN IF NOT EXISTS tags TEXT[] DEFAULT '{}';
"

# 5. 启动项目
npm run dev
```

## 启动成功后访问
- API 文档：[http://localhost:3000/api-docs](http://localhost:3000/api-docs)
- 健康检查：http://localhost:3000/health
- 项目接口：[http://localhost:3000](http://localhost:3000)
