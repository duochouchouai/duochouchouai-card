# duochouchouai-card
考核云端代码仓库

# Android 名片管理（Jetpack Compose · MVI）

## 项目介绍
本项目是一个基于 Jetpack Compose 的名片管理与分享应用，采用 MVI（Model-View-Intent）架构实现全响应式 UI。支持本地注册/登录、我的名片管理、二维码分享与扫码添加、收藏管理（含批量管理）。项目不依赖网络，登录状态本地持久化，适合离线场景演示与学习。

## 功能清单
- 登录/注册
  - 手机号或验证码登录，登录状态本地加密持久化
- 我的名片
  - 创建、编辑、删除、查看
  - 搜索（姓名/公司/职位）与排序（时间/姓名/公司/分类）
  - 详情页支持“分享”（跳转二维码分享页）与右下角浮动按钮进入编辑
- 二维码分享与扫码添加
  - 详情页生成名片二维码并保存到相册
  - 列表/收藏页右下角浮动按钮可“手动添加”“扫码添加”
  - 从二维码扫码添加时自动收入收藏
- 收藏
  - 收藏列表样式与我的名片一致
  - 搜索（姓名/公司/职位）与排序（时间/姓名/公司/分类）
  - 批量管理：全选、清空、取消收藏（已取消批量删除）
  - 单项操作：编辑、删除、收藏切换

## 架构设计（MVI）
- Model
  - 数据实体：`Card`（data class，`@Immutable`）
  - 本地数据源：Room 数据库（`AppDatabase`/`CardDao`）
  - 仓库：`CardRepository` 封装 DAO 访问与协程调度
- View（Compose）
  - UI 仅根据 `ViewModel` 暴露的 `StateFlow<State>` 渲染
  - 页面：登录、我的名片列表、名片编辑、名片详情、二维码、收藏
- Intent
  - 用户操作事件使用 `sealed class` 定义并派发到 `ViewModel`
  - `ViewModel` 根据 Intent 变更 State 或调用 Repository
- 数据流
  - 列表/收藏搜索与排序均在 `ViewModel` 控制，`distinctUntilChanged()` 避免无效重组
  - 新建时可选择“保存到我的”（仅列表）或“保存到收藏”（仅收藏），编辑不改动现有收藏状态

## 主要技术栈
- UI：Jetpack Compose（Material 3），Navigation
- 数据：Room（Flow）、StateFlow（Kotlin Coroutines）
- 图片：Coil
- 工具：Gradle、AGP 8.x、JDK 17

## 环境配置要求
- Android Studio（Koala+ 或更高）
- JDK：17（确保 `JAVA_HOME` 指向 JDK 17）
- Android Gradle Plugin：8.x
- Compose/Material3：建议使用 Compose BOM 2024.09.00+、Material3 1.3.0+
- SDK：推荐 targetSdk 34，minSdk 24+

## 权限与兼容
- 媒体权限
  - Android 33+ 使用 `READ_MEDIA_IMAGES`
  - Android 29–32 建议使用分区存储/Photo Picker
  - Android 28- 使用 `READ_EXTERNAL_STORAGE`
- 项目当前保存二维码到相册时会按版本动态申请对应权限

## 运行步骤
- 使用 Android Studio
  1. 打开项目并等待 Gradle Sync 完成
  2. 选择模拟器或真机，点击 Run 运行应用
- 使用命令行
  1. Windows PowerShell 进入项目根目录
  2. 构建：`./gradlew clean build --no-daemon`
  3. 仅调试包：`./gradlew assembleDebug -x test`
  4. 安装到设备：`./gradlew :app:installDebug`（需设备通过 `adb devices` 识别）

## 目录结构（核心）
- `app/src/main/java/com/example/androidprogram`
  - `feature/cards/list/CardListScreen.kt` 我的名片列表
  - `feature/cards/edit/CardEditScreen.kt` 名片添加/编辑
  - `feature/cards/detail/CardDetailScreen.kt` 名片详情（分享入口）
  - `feature/qr/QrScreen.kt` 二维码分享/扫码添加
  - `feature/favorites/FavoritesScreen.kt` 收藏列表（批量管理）
  - `feature/login/LoginScreen.kt` 登录/注册
  - `model/AppDatabase.kt`、`model/CardDao.kt`、`model/Card.kt`
  - `repository/CardRepository.kt`
  - `auth/AuthManager.kt` 登录状态持久化
  - `ui/components/CustomFloatingActionButton.kt` 可展开浮动按钮组件
  - `MainActivity.kt` 路由与页面动画（统一左右滑动）

## 发布与优化
- Release 构建已开启 R8 压缩与资源收缩（`minifyEnabled/shrinkResources`）
- Lint 报告路径：`app/build/reports/lint-results-debug.html`
- 统一页面切换动画：进入左滑、退出右滑

## 常见问题
- Kapt 语言版本回退到 1.9 的提示，为信息性警告，不影响运行
- 部分 Icons 显示“使用 AutoMirrored 版本”的弃用提示，功能不受影响，可按需替换为 `Icons.AutoMirrored.*`

## 说明
- 本项目为离线演示应用，无联网要求；登录与数据操作均基于本地存储
- 数据删除采用软删除（Room 层标记），UI 中已去除“批量删除”入口，仅保留单项删除