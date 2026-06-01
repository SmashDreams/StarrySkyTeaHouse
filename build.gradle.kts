// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

val sudokuProjectDir = rootProject.projectDir.resolve("../StarrySkySudoku").canonicalFile
val sudokuDebugApk = sudokuProjectDir.resolve("app/build/outputs/apk/debug/app-debug.apk")
val bundledSudokuApk = rootProject.projectDir.resolve("app/src/main/assets/starry_sky_sudoku.apk")

/*
 * 茶苑内置的数独 APK 来自相邻的 StarrySkySudoku 仓库，用独立任务显式同步，避免 assets 中的安装包落后于源码。
 */
tasks.register<Exec>("assembleBundledSudokuDebug") {
    group = "distribution"
    description = "Builds the sibling StarrySkySudoku debug APK used by the TeaHouse installer."
    workingDir = sudokuProjectDir
    commandLine("./gradlew", "assembleDebug")
}

tasks.register<Copy>("syncBundledSudokuApk") {
    group = "distribution"
    description = "Copies the latest StarrySkySudoku debug APK into TeaHouse assets."
    dependsOn("assembleBundledSudokuDebug")
    from(sudokuDebugApk)
    into(bundledSudokuApk.parentFile)
    rename { bundledSudokuApk.name }
}
