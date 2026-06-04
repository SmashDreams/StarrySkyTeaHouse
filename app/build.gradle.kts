plugins {
    alias(libs.plugins.android.application)
}

import java.util.Properties

val sharedContractsSourceDir = "../../StarrySkySudoku/shared-contracts/src/main/java"
val releaseSigningProperties = Properties().apply {
    val releaseSigningPropertiesFile = rootProject.file("local.properties")
    if (releaseSigningPropertiesFile.exists()) {
        releaseSigningPropertiesFile.inputStream().use { load(it) }
    }
}
val requiredReleaseSigningProperties = listOf(
    "RELEASE_STORE_FILE",
    "RELEASE_STORE_PASSWORD",
    "RELEASE_KEY_ALIAS",
    "RELEASE_KEY_PASSWORD"
)
val hasReleaseSigningProperties = requiredReleaseSigningProperties.all {
    !releaseSigningProperties.getProperty(it).isNullOrBlank()
}
val requestedReleaseBuild = gradle.startParameter.taskNames.any {
    it.lowercase().contains("release")
}

android {
    namespace = "com.bird.starryskyteahouse"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.bird.starryskyteahouse"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigningProperties) {
                storeFile = file(releaseSigningProperties.getProperty("RELEASE_STORE_FILE"))
                storePassword = releaseSigningProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = releaseSigningProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = releaseSigningProperties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            if (hasReleaseSigningProperties) {
                signingConfig = signingConfigs.getByName("release")
            } else if (requestedReleaseBuild) {
                throw GradleException("Release signing properties are missing from local.properties")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    sourceSets {
        getByName("main") {
            java.directories.add(sharedContractsSourceDir)
            kotlin.directories.add(sharedContractsSourceDir)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
