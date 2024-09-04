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
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.multidex:multidex:2.0.0")

    implementation(project(":scenes-single-video"))
    implementation(project(":scenes-feed-video"))
    implementation(project(":common"))

    annotationProcessor("com.github.bumptech.glide:compiler:$deps_glide_version")
}