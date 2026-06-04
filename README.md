# 星空茶苑 (StarrySkyTeaHouse)

星空茶苑是星空数独的外置登录与战绩查看应用。它负责本地注册/登录，并通过 `ContentProvider` 向星空数独提供当前登录状态；同时它会读取星空数独公开的战绩 Provider，展示当前登录用户的通关记录。

## 版本

- 当前版本：2.0
- 包名：`com.bird.starryskyteahouse`
- 应用名：星空茶苑
- 最低系统版本：Android 7.0 / API 24
- 本次维护：完善 release 签名构建与内置星空数独 APK 同步；细化模块技术栈说明；继续保持跨应用共享契约、登录态 Provider 和战绩 Provider 的一致性。

## 功能

- 本地用户注册、登录、退出登录
- 作为游戏中转站，支持进入已安装的星空数独
- 内置星空数独 APK，未安装时可调起系统安装器
- 内置星空数独 APK 使用相邻星空数独仓库的签名 Release 构建产物
- 背景音乐与按钮点击音效，音频体验参考星空数独
- 通过 `SessionProvider` 暴露当前登录用户
- 查询星空数独战绩 Provider，显示当前用户通关记录
- 未登录时提示先登录，不展示战绩
- 对跨应用 Provider 访问使用签名权限和 Android 11+ 包可见性声明

## 项目结构

- `MainActivity.kt`：主页面入口，负责生命周期、事件收集和页面级导航
- `RegisterActivity.kt`：注册页面入口，负责生命周期、注册事件收集和页面结束
- `TeaHouseAppContainer.kt`：集中创建 Session、游戏入口和战绩读取依赖
- `main/MainScreenRenderer.kt`：主页面输入读取、登录态渲染、游戏入口渲染和战绩列表渲染
- `main/RegisterScreenController.kt`：注册页表单读取和按钮绑定
- `main/RecordsAdapter.kt`：使用 `RecyclerView + ListAdapter` 渲染战绩空状态、汇总和记录卡片
- `main/MainViewModel.kt`、`main/RegisterViewModel.kt`：页面状态与事件管理
- `game/GameEntryManager.kt`：检测、启动和安装星空数独
- `records/GameRecordRepository.kt`：读取星空数独战绩 Provider
- `session/SessionProvider.kt`、`session/SessionStore.kt`：本地登录态存储与跨应用暴露
- `../StarrySkySudoku/shared-contracts/`：与星空数独共用的 Provider/包名契约源码

## 本地目录要求

星空茶苑 2.0 会直接引用星空数独仓库中的共享契约源码，因此建议两个仓库保持同级目录：

```text
projects/
  StarrySkySudoku/
  StarrySkyTeaHouse/
```

如果只单独克隆 `StarrySkyTeaHouse`，Gradle 会找不到 `../StarrySkySudoku/shared-contracts/`，需要先把 `StarrySkySudoku` 克隆到同级目录。

## 内置星空数独 APK

- 文件路径：`app/src/main/assets/starry_sky_sudoku.apk`
- 来源：星空数独签名 Release 构建产物 `../StarrySkySudoku/app/build/outputs/apk/release/app-release.apk`
- 安装入口：`GameEntryManager` 从 assets 复制到缓存目录后，通过 `FileProvider` 调起系统安装器
- FileProvider Authority：`com.bird.starryskyteahouse.fileprovider`
- 同步命令：`./gradlew syncBundledSudokuApk`

`syncBundledSudokuApk` 会先执行相邻 `StarrySkySudoku` 仓库的 `assembleRelease`，再把最新 release APK 复制为茶苑 assets 中的 `starry_sky_sudoku.apk`，用于避免内置安装包落后于源码。

星空数独读取茶苑登录态依赖 `signature` 级别权限，茶苑和数独必须使用同一个 release keystore 签名。调试时请安装茶苑 release 包 `app/build/outputs/apk/release/app-release.apk`，不要混用 debug 茶苑和 release 数独。

## 与星空数独的通信

### 星空茶苑提供登录状态

- Authority：`com.bird.starryskyteahouse.provider`
- URI：`content://com.bird.starryskyteahouse.provider/session`
- 权限：`com.bird.starryskyteahouse.permission.READ_SESSION`
- 字段：
  - `username`
  - `logged_in`

星空数独通过这个 Provider 判断当前用户。如果没有登录，则星空数独使用游客账户。

### 星空茶苑读取星空数独战绩

- Authority：`com.bird.starryskysudoku.provider`
- URI：`content://com.bird.starryskysudoku.provider/results`
- 权限：`com.bird.starryskysudoku.permission.READ_RESULTS`
- 查询方式：使用 `username=?` 按当前登录用户过滤战绩。
- 默认排序：`created_at DESC`

## 技术栈与模块划分

### 构建与平台

- Android Gradle Plugin 9.2.1，Gradle Version Catalog 管理插件和依赖版本。
- Kotlin + Android 原生 XML Layout，使用 ViewBinding 连接页面和控件。
- compileSdk：API 36，minorApiLevel 1；targetSdk：API 36；minSdk：Android 7.0 / API 24。
- Java 11 编译目标。
- release 构建从 `local.properties` 读取 keystore 参数；茶苑和星空数独必须使用同一 keystore，才能通过 `signature` 级 Provider 权限互访。

### UI 与页面层

- `MainActivity` / `RegisterActivity`：Activity 生命周期、ViewBinding 初始化和页面级事件收口。
- `MainScreenRenderer` / `RegisterScreenController`：XML 页面控件读取、按钮绑定、登录态和游戏入口渲染。
- `RecordsAdapter`：`RecyclerView + ListAdapter + DiffUtil` 渲染战绩空状态、汇总面板和记录卡片。
- `ShootingStarView` 与 drawable/XML shape：实现茶苑星空背景、卡片、状态 chip 和记录面板视觉层。
- 主要 UI 依赖：AndroidX AppCompat、Material Components、AndroidX Core KTX。

### 状态与业务层

- `MainViewModel` / `RegisterViewModel`：使用 AndroidX Lifecycle ViewModel 管理页面状态、登录事件、注册事件和战绩加载结果。
- `TeaHouseAppContainer`：集中创建 Session、游戏入口、战绩读取和音乐依赖，减少 Activity 直接持有底层实现。
- `SessionStore`：基于 SharedPreferences 保存本地账号、登录状态和记住密码配置。
- `TeaMusic`：使用 MediaPlayer 播放背景音乐，使用 SoundPool 播放按钮点击音效。

### 跨应用通信与安装

- `SessionProvider`：茶苑提供登录态，使用 `ContentProvider` 暴露 `username` 和 `logged_in`。
- `GameRecordRepository`：通过 `ContentResolver` 查询星空数独战绩 Provider，并按当前登录用户过滤记录。
- `GameEntryManager`：检测星空数独安装状态；未安装时从 assets 复制内置 APK，并通过 AndroidX FileProvider 调起系统安装器。
- 共享契约直接引用 `../StarrySkySudoku/shared-contracts/src/main/java`，保证双方 Provider authority、URI、字段和权限常量一致。
- Android 11+ 包可见性通过 `<queries>` 声明星空数独包名和 Provider authority。

### 测试

- JUnit4：ViewModel、契约、结构和解析逻辑测试。
- Robolectric：本地 Android 单元测试支持。
- kotlinx-coroutines-test：协程相关 ViewModel 测试。
- AndroidX Test / Espresso：保留仪器测试依赖。

## 构建

首次构建前，请确认同级目录下已经存在 `StarrySkySudoku` 仓库。release 构建还需要在 `local.properties` 中配置签名参数：

```properties
RELEASE_STORE_FILE=/absolute/path/to/release.jks
RELEASE_STORE_PASSWORD=你的 store 密码
RELEASE_KEY_ALIAS=你的 key alias
RELEASE_KEY_PASSWORD=你的 key 密码
```

构建茶苑 release 包：

```bash
./gradlew assembleRelease
```

运行本地单元测试：

```bash
./gradlew test
```

同步并重新内置最新星空数独签名 release APK：

```bash
# 在 StarrySkyTeaHouse 目录下执行
./gradlew syncBundledSudokuApk
```

## 使用说明

1. 安装星空茶苑 release 包。
2. 在星空茶苑中注册并登录用户。
3. 如果星空数独未安装，可在星空茶苑中点击“下载安装”，系统安装器会安装 assets 中的内置签名 release APK。
4. 如果星空数独已安装，可点击“进入游戏”。
5. 星空数独会通过茶苑的 `SessionProvider` 读取当前登录用户；如果读取不到，会降级为游客账户。
6. 通关或失败后，星空数独会写入带用户名的战绩。
7. 返回星空茶苑，可查看当前用户的通关记录。

如果设备上曾安装 debug 包，建议先卸载旧包，再安装 release 包，避免签名不一致导致 `signature` 权限访问失败。

## 版本记录

- 2.0 当前维护：release 构建接入本地签名配置；`syncBundledSudokuApk` 同步签名 release 版星空数独；修正 README 技术栈、构建和安装说明。
- 2.0：接入 `shared-contracts` 共享契约；新增 `syncBundledSudokuApk` 同步任务；内置星空数独 APK 更新到 2.0；同步说明数独地图页结构拆分和跨应用契约整理。
- 1.1 维护更新：包名规范化为 `com.bird.starryskyteahouse`；接入 ViewBinding；拆分依赖容器、主页面渲染器和注册页控制器；战绩记录改为 `RecyclerView + ListAdapter` 并修复初始提示卡片宽度；删除旧 `RecordsRenderer`；重新集成当时最新的星空数独 APK。
- 1.1：新增茶苑背景音乐和按钮点击音效；优化音频焦点、点击音效封装、战绩刷新竞态、游戏启动异常处理和登录输入同步。
- 1.1：更新内置星空数独 APK 到 1.5；同步说明游戏前台倒计时通知与进入棋盘前通知权限处理。
- 1.0：完成本地注册/登录、星空数独安装/启动、登录状态 Provider 与战绩查询展示。

## 注意

- 当前账号系统为本地演示用途，不包含网络登录。
- 星空茶苑和星空数独需要使用同一签名安装，才能通过签名级 Provider 权限互相访问。
