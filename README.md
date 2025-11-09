名片管理系统（duochouchouai-card）
一款基于 Node.js + Express 开发的名片管理工具，支持用户认证、名片二维码生成 / 展示、他人名片保存等核心功能，集成 Swagger UI 提供可视化接口文档，便于开发和测试。
🌟 功能特点
用户认证：支持用户名 / 手机号注册、登录，基于 JWT 实现身份校验
二维码功能：生成个人名片二维码（可选包含完整联系信息）、查询历史生成记录
名片管理：保存他人名片（通过二维码解析唯一标识）、分页查询已保存名片
接口文档：集成 Swagger UI，所有接口可视化展示，支持在线调试
跨平台兼容：基于 HTTP 协议，支持前后端分离部署，适配多端调用
📋 环境要求
Node.js ≥ 14.x（推荐 16.x 或 18.x）
npm ≥ 6.x
Git（版本控制）
🚀 快速开始
克隆仓库
bash
# 克隆远程仓库到本地（替换为你的远程仓库地址）
git clone https://gitee.com/你的用户名/duochouchouai-card.git
# 进入项目根目录
cd duochouchouai-card
安装依赖
进入 nodejs 目录（服务器核心目录）安装依赖：
bash
# 进入服务器目录
cd nodejs
# 安装依赖
npm install
配置说明
静态文件：public 目录用于存放静态资源（如 code.yaml Swagger 配置文件），已通过 Express 自动暴露
端口配置：默认端口 3000，可在 server.js 中修改 app.listen(3000, ...) 自定义端口
JWT 密钥：建议在生产环境中配置自定义密钥（当前为演示密钥，需修改 server.js 中 JWT 签名逻辑）
启动服务
bash
# 开发环境启动（node 直接运行）
node server.js

📌 接口文档使用
启动服务后，访问 http://localhost:3000/swagger
无需登录即可查看所有接口，需认证的接口（如二维码生成）需先调用「登录接口」获取 Token
在 Swagger 页面顶部「Authorize」输入框中，填写 Bearer {你的 Token}（注意 Bearer 后加空格），即可调用需认证的接口

🚨 注意事项
建议在项目根目录创建 .gitignore 文件，忽略冗余文件（内容如下）：
plaintext
# 依赖包
node_modules/
# 日志文件
*.log
# 环境变量文件
.env
# 编辑器缓存
.idea/
.vscode/
*.swp
*.swo
# 操作系统文件
Thumbs.db
.DS_Store
生产环境需优化：修改 JWT 密钥、配置 HTTPS、添加日志记录、实现数据持久化（如连接 MySQL/MongoDB）
二维码 Base64 编码较大，建议生产环境改为「保存二维码图片到服务器，返回图片 URL」，减少接口响应体积
