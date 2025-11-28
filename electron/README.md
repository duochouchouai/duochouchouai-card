# 多瞅瞅名片运管后台管理系统（Electron 端）
基于 Vue3+Electron+Pinia 开发的桌面端运管系统，用于展示“多瞅瞅名片”项目的用户数据、功能使用情况与用户行为指标，支持运营账号登录鉴权。

## 项目关联
- 后端系统：仓库内 `Nodejs` 文件夹（Node.js+PostgreSQL，提供 API 支持）
- 移动端 App：仓库内 `Android` 文件夹（Jetpack Compose 开发，离线名片管理）

## 技术栈
- 前端框架：Vue3（Composition API + `<script setup>`）
- 桌面打包：Electron 29+
- 状态管理：Pinia
- 构建工具：Vite 5+
- API 请求：Axios
- 数据可视化：ECharts（vue-echarts）
- 路由管理：Vue Router 4+

## 环境要求
1. Node.js ≥ 18.x（与后端一致）
2. 后端服务已启动（参考 `Nodejs/README.md` 启动步骤）
3. 依赖工具：npm 或 yarn

## 快速开始
### 1. 启动后端服务（必须先执行）
```bash
# 进入后端目录
cd ../Nodejs

# 安装后端依赖
npm install

# 配置环境变量（复制示例并修改数据库信息）
cp .env.example .env

# 启动后端（默认端口3000）
npm run dev