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
        sourceCompatibility = JavaVersion.VERSION_1_7
        targetCompatibility = JavaVersion.VERSION_1_7
    }
}

dependencies {
    implementation("com.android.support:appcompat-v7:$deps_android_ui_version")
    implementation("com.android.support:design:$deps_android_ui_version")
    implementation("com.android.support.constraint:constraint-layout:$deps_constraint_layout_version")

    api("org.jetbrains.kotlin:kotlin-stdlib:$deps_kotlin_version")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$deps_kotlin_version")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$deps_kotlin_version")

    api("net.polyv.android:media-player-full:2.2.0")

    api("de.hdodenhof:circleimageview:$deps_circle_image_view_version")
    api("com.github.yyued:SVGAPlayer-Android:$deps_svga_version")
    api("com.github.bumptech.glide:okhttp3-integration:$deps_glide_version")
    annotationProcessor("com.github.bumptech.glide:compiler:$deps_glide_version")
}