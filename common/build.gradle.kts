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
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    api("org.jetbrains.kotlin:kotlin-stdlib:$deps_kotlin_version")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$deps_kotlin_version")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$deps_kotlin_version")

    api("net.polyv.android:media-player-full:2.2.0")

    api("de.hdodenhof:circleimageview:$deps_circle_image_view_version")
    api("com.github.yyued:SVGAPlayer-Android:$deps_svga_version")
    api("com.github.bumptech.glide:okhttp3-integration:$deps_glide_version")
    annotationProcessor("com.github.bumptech.glide:compiler:$deps_glide_version")
}