plugins {
    id("com.android.library")
}

android {
    namespace = "net.polyv.android.player.scenes.feed"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    api(project(":common"))
}