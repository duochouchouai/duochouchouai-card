require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const path = require('path');
const fs=require('fs');

// 1. 数据库相关（替换原 pool，适配 Sequelize）
const sequelize = require('./config/db'); // Sequelize 连接实例（原 config/db.js）
const initDatabase = require('./config/dbInit'); // 数据库初始化函数（原 config/dbInit.js）
const User = require('./models/User'); // 导入模型确保关联生效
const Card = require('./models/Card');

// 2. 路由与 Swagger 相关（保留用户配置）
const mainRoutes = require('./routes/index'); // 总路由
const swaggerUi = require('swagger-ui-express');
const swagger = require('./swagger'); // 确保 swagger.js 存在于项目根目录

// 3. 创建 Express 应用
const app = express();
const PORT = process.env.PORT || 3000; // 优先读取环境变量，默认 3000


// 4. 中间件配置（完全保留用户原配置）
app.use(helmet()); // 安全头：防止常见 Web 漏洞
app.use(cors({ 
  origin: process.env.CORS_ORIGIN || '*', // 允许跨域，默认允许所有
  credentials: true // 可选：如需携带 Cookie 可开启
}));
app.use(express.json()); // 解析 JSON 请求体（支持名片、用户信息提交）
app.use(express.urlencoded({ extended: true })); // 解析表单提交（如后续扩展文件上传）
app.use(morgan('dev')); // 日志中间件：开发环境打印请求日志

// 5. 静态文件服务（保留，支持后续文件上传/预览）
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));
const qrcodeDir = path.join(__dirname, 'uploads', 'qrcode');
if (!fs.existsSync(qrcodeDir)) {
  fs.mkdirSync(qrcodeDir, { recursive: true }); // recursive: true 支持多级目录
  console.log(`二维码目录创建成功：${qrcodeDir}`);
}

// 6. 路由挂载（适配你的路由结构）
app.use(mainRoutes);

// 7. Swagger API 文档（完全保留动态加载逻辑）
const swaggerPrefix = `/api/${process.env.API_VERSION || 'v1'}`;
app.use(swaggerPrefix, swaggerUi.serve, (req, res, next) => {
  try {
    // 每次请求重新加载 Swagger 文档（支持热更新）
    const swaggerDocs = swagger.loadOpenAPIDoc(); 
    swaggerUi.setup(swaggerDocs)(req, res, next);
  } catch (error) {
    next(new Error(`Swagger 文档加载失败：${error.message}`));
  }
});

// 8. 健康检查接口（用户原配置保留）
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    database: sequelize.connectionManager.state === 'connected' ? 'connected' : 'disconnected'
  });
});

// 9. 错误处理中间件（保留用户原逻辑，优化错误提示）
app.use((err, req, res, next) => {
  console.error('❌ 服务器错误：', err.stack);

  // 文件上传错误（Multer，如需后续扩展可保留）
  if (err.name === 'MulterError') {
    return res.status(400).json({ 
      success: false,
      message: '文件上传失败', 
      error: err.message 
    });
  }

  // Swagger 相关错误
  if (err.message.includes('Swagger')) {
    return res.status(500).json({
      success: false,
      message: err.message,
      stack: process.env.NODE_ENV === 'development' ? err.stack : undefined
    });
  }

  // 通用错误
  const statusCode = err.statusCode || 500;
  const message = err.message || '服务器内部错误';
  res.status(statusCode).json({
    success: false,
    message,
    stack: process.env.NODE_ENV === 'development' ? err.stack : undefined
  });
});

// 10. 404 路由处理（保留用户原逻辑）
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `请求的资源不存在：${req.method} ${req.originalUrl}`
  });
});

// 11. 启动服务器（修复原错误，适配 Sequelize）
async function startServer() {
  let serverInstance = null;
  try {
    // 步骤 1：初始化数据库（创建库、表、测试数据）
    await initDatabase(); 
    console.log('✅ 数据库初始化完成');

    // 步骤 2：启动 HTTP 服务器
    serverInstance = app.listen(PORT, () => {
      console.log(`🎉 服务器运行在 http://localhost:${PORT}/`);
      console.log(`🩺 健康检查：http://localhost:${PORT}/health`);
      console.log(`📚 API 文档：http://localhost:${PORT}${swaggerPrefix}`);
    });

    // 步骤 3：处理端口占用错误
    serverInstance.on('error', (err) => {
      if (err.code === 'EADDRINUSE') {
        console.error(`❌ 端口 ${PORT} 已被占用！`);
        console.error(`💡 解决方案：PORT=3001 npm run dev（替换为未占用端口）`);
        process.exit(1);
      }
      throw err;
    });

    // 步骤 4：关闭服务器（释放资源）
    const Shutdown = async () => {
      console.log('\n🛑 正在关闭服务器...');

      // 关闭 HTTP 服务器
      if (serverInstance) {
        serverInstance.close(() => console.log('✅ HTTP 服务器已关闭'));
      }

      // 关闭 Sequelize 数据库连接
      await sequelize.close().catch(err => {
        console.error('⚠️  关闭数据库连接失败：', err.message);
      });
      console.log('✅ 数据库连接已关闭');

      console.log('✅ 服务器已安全关闭');
      process.exit(0);
    };

    // 监听终止信号（Ctrl+C 等）
    process.on('SIGINT', Shutdown);
    process.on('SIGTERM', Shutdown);

  } catch (error) {
    console.error('❌ 启动服务器失败：', error.message);

    // 异常时释放资源
    try {
      if (serverInstance) serverInstance.close();
      await sequelize.close();
    } catch (closeError) {
      console.error('⚠️  关闭资源时出错：', closeError.message);
    }

    process.exit(1);
  }
}

// 执行启动函数
startServer();