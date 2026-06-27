plugins {
    alias(libs.plugins.android.application)
}

// Disable AAR metadata check (compileSdk 37 not supported by AGP 8.5.2)
tasks.whenTaskAdded {
    if (name.contains("checkReleaseAarMetadata")) {
        enabled = false
    }
}

android {
    namespace = "com.raincat.dolby_beta"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.raincat.dolby_beta"
        minSdk = 26
        targetSdk = 34
        versionCode = 400
        versionName = "4.0.0"

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    lint { abortOnError = false }

    packaging {
        resources {
            merges += "META-INF/xposed/*"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

configurations.all {
    // kotlin-stdlib 1.8.22 already bundles jdk7/jdk8 extensions
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
}

dependencies {
    // Libxposed API (framework-provided, compileOnly)
    compileOnly(libs.libxposed.api)
    // Libxposed Service (shipped inside APK for module-app communication)
    implementation(libs.libxposed.service)

    // Legacy Xposed API stub (included in source tree for migration compat)
    // No external dependency needed

    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.localbroadcastmanager)

    // Utilities
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.dexlib2)
    implementation(libs.annimon.stream)

    // Local JARs
    implementation(fileTree(mapOf("dir" to "libs", "include" to "*.jar")))
}
