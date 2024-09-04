plugins {
    id("com.android.library")
}

android {
    namespace = "net.polyv.android.player.scenes.single"
    compileSdk = compile_sdk_version

    defaultConfig {
        minSdk = min_sdk_version

        consumerProguardFiles("proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_7
        targetCompatibility = JavaVersion.VERSION_1_7
    }
}

dependencies {
    implementation("com.android.support:appcompat-v7:$deps_android_ui_version")
    implementation("com.android.support:design:$deps_android_ui_version")
    implementation("com.android.support.constraint:constraint-layout:$deps_constraint_layout_version")

    api(project(":common"))
}