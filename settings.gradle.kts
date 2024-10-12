pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        maven {
            credentials {
                username = "609cc5623a10edbf36da9615"
                password = "EbkbzTNHRJ=P"
            }
            url = uri("https://packages.aliyun.com/maven/repository/2102846-release-8EVsoM/")
        }
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://maven.aliyun.com/nexus/content/repositories/releases/")
        }
    }
}

rootProject.name = "polyv-android-media-player-sdk-demo"
include(":demo")
include(":scenes-single-video")
include(":scenes-feed-video")
include(":scenes-download-center")
include(":common")
