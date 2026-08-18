pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ComposeLightbox"
include(":lib")
include(":sample:androidApp")
include(":sample:iosApp")
include(":sample:shared")
