plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "net.polyv.android.player.common"
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

    api("net.polyv.android:media-player-full:2.6.0")
    api("net.polyv.android:media-player-sdk-addon-download:2.6.0")

    api("de.hdodenhof:circleimageview:$deps_circle_image_view_version")
    api("com.github.yyued:SVGAPlayer-Android:$deps_svga_version")
    api("com.github.bumptech.glide:okhttp3-integration:$deps_glide_version")
    annotationProcessor("com.github.bumptech.glide:compiler:$deps_glide_version")
    api("net.polyv.android:renderscript:$deps_render_script")
}