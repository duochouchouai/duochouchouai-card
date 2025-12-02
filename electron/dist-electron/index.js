import { app, BrowserWindow } from "electron";
import path from "path";
const isDev = process.env.NODE_ENV === "development";
let mainWindow = null;
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1e3,
    height: 600,
    webPreferences: {
      // 允许渲染进程使用 Node.js API（按需开启）
      nodeIntegration: true,
      contextIsolation: false,
      // 开发模式下加载 Vue 开发服务器，生产模式下加载打包后的 index.html
      preload: path.join(__dirname, "../preload/index.js")
      // 可选：预加载脚本
    }
  });
  if (isDev) {
    mainWindow.loadURL("http://127.0.0.1:3344");
    mainWindow.webContents.openDevTools();
  } else {
    mainWindow.loadFile(path.join(__dirname, "../../dist/index.html"));
  }
  mainWindow.on("closed", () => {
    mainWindow = null;
  });
}
app.whenReady().then(createWindow);
app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
app.on("activate", () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});
