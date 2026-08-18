import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.serialization)
}

group = "io.github.x-sheep"
version = "2.0.0"

kotlin {
    jvm()
    android {
        namespace = "io.github.xsheep.composelightbox"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources.enable = true

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.coil.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "io.github.xsheep.composelightbox.generated.resources"
    generateResClass = always
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "compose-lightbox", version.toString())

    pom {
        name = "Compose Lightbox"
        description = "Lightbox for Compose Multiplatform"
        inceptionYear = "2025"
        url = "https://github.com/x-sheep/compose-lightbox"
        licenses {
            license {
                name = "MIT License"
                url = "http://www.opensource.org/licenses/mit-license.php"
            }
        }
        developers {
            developer {
                name = "Lennard Sprong"
                email = "x-sheep-puzzles@outlook.com"
                url = "https://github.com/x-sheep"
            }
        }
        scm {
            url = "https://github.com/x-sheep/compose-lightbox"
            connection = "scm:git:git://github.com/x-sheep/compose-lightbox.git"
            developerConnection = "scm:git:ssh://github.com:x-sheep/compose-lightbox.git"
        }
    }
}
