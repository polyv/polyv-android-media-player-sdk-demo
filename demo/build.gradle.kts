plugins {
    id("com.android.application")
}

android {
    namespace = "net.polyv.android.player.demo"
    compileSdk = compile_sdk_version

    defaultConfig {
        applicationId = "net.polyv.android.player.demo"
        minSdk = min_sdk_version
        targetSdk = target_sdk_version
        versionCode = version_code
        versionName = version_name
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
    implementation("com.android.support:multidex:$deps_multidex_version")

    implementation(project(":common"))

    annotationProcessor("com.github.bumptech.glide:compiler:$deps_glide_version")
}