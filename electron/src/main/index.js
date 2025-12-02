// src/main/index.js
import { app, BrowserWindow } from 'electron'
import path from 'path'

// 解决开发模式下的路径问题
const isDev = process.env.NODE_ENV === 'development'

// 避免重复创建窗口
let mainWindow = null

// 创建主窗口
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1000,
    height: 600,
    webPreferences: {
      // 允许渲染进程使用 Node.js API（按需开启）
      nodeIntegration: true,
      contextIsolation: false,
      // 开发模式下加载 Vue 开发服务器，生产模式下加载打包后的 index.html
      preload: path.join(__dirname, '../preload/index.js') // 可选：预加载脚本
    }
  })

  // 加载页面
  if (isDev) {
    // 开发模式：加载 Vite 开发服务器
    mainWindow.loadURL('http://127.0.0.1:3344')
    // 打开开发者工具
    mainWindow.webContents.openDevTools()
  } else {
    // 生产模式：加载打包后的静态文件
    mainWindow.loadFile(path.join(__dirname, '../../dist/index.html'))
  }

  // 窗口关闭时销毁实例
  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

// Electron 应用就绪后创建窗口
app.whenReady().then(createWindow)

// 所有窗口关闭时退出应用（macOS 除外）
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// macOS 点击 Dock 图标时重新创建窗口
app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow()
  }
})