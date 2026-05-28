# 星空茶苑 (StarrySkyTeaHouse)

星空茶苑是星空数独的外置登录与战绩查看应用。它负责本地注册/登录，并通过 `ContentProvider` 向星空数独提供当前登录状态；同时它会读取星空数独公开的战绩 Provider，展示当前登录用户的通关记录。

## 版本

- 当前版本：1.1
- 包名：`com.bird.StarrySkyTeaHouse`
- 应用名：星空茶苑
- 最低系统版本：Android 7.0 / API 24

## 功能

- 本地用户注册、登录、退出登录
- 作为游戏中转站，支持进入已安装的星空数独
- 内置星空数独 APK，未安装时可调起系统安装器
- 内置星空数独 APK 已更新到 1.5 版本
- 背景音乐与按钮点击音效，音频体验参考星空数独
- 通过 `SessionProvider` 暴露当前登录用户
- 查询星空数独战绩 Provider，显示当前用户通关记录
- 未登录时提示先登录，不展示战绩
- 对跨应用 Provider 访问使用签名权限和 Android 11+ 包可见性声明

## 与星空数独的通信

### 星空茶苑提供登录状态

- Authority：`com.bird.StarrySkyTeaHouse.provider`
- URI：`content://com.bird.StarrySkyTeaHouse.provider/session`
- 权限：`com.bird.StarrySkyTeaHouse.permission.READ_SESSION`
- 字段：
  - `username`
  - `logged_in`

星空数独通过这个 Provider 判断当前用户。如果没有登录，则星空数独使用游客账户。

### 星空茶苑读取星空数独战绩

- Authority：`com.bird.starryskysudoku.provider`
- URI：`content://com.bird.starryskysudoku.provider/results`
- 权限：`com.bird.starryskysudoku.permission.READ_RESULTS`
- 查询方式：使用 `username=?` 按当前登录用户过滤战绩。

## 技术栈

- Android 原生应用
- Kotlin + XML Layout
- AppCompat / Material Components
- FileProvider 本地 APK 安装分发
- SharedPreferences 本地账户与登录状态
- ContentProvider / ContentResolver 跨应用通信
- MediaPlayer / SoundPool 茶苑背景音乐与点击音效
- Gradle Version Catalog

## 构建

```bash
./gradlew assembleDebug
```

运行本地单元测试：

```bash
./gradlew testDebugUnitTest
```

## 使用说明

1. 安装星空数独。
2. 安装星空茶苑。
3. 在星空茶苑中注册并登录用户。
4. 如果星空数独未安装，可在星空茶苑中点击“下载安装”；如果已安装，可点击“进入游戏”。
5. 回到星空数独，主界面左上角会显示当前用户名。
6. 通关或失败后，星空数独会写入带用户名的战绩。
7. 返回星空茶苑，可查看当前用户的通关记录。

## 版本记录

- 1.1：新增茶苑背景音乐和按钮点击音效；优化音频焦点、点击音效封装、战绩刷新竞态、游戏启动异常处理和登录输入同步。
- 1.1：更新内置星空数独 APK 到 1.5；同步说明游戏前台倒计时通知与进入棋盘前通知权限处理。
- 1.0：完成本地注册/登录、星空数独安装/启动、登录状态 Provider 与战绩查询展示。

## 注意

- 当前账号系统为本地演示用途，不包含网络登录。
- 星空茶苑和星空数独需要使用同一签名安装，才能通过签名级 Provider 权限互相访问。
