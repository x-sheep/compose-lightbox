plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.x-sheep"
version = "1.0.0"

android {
    namespace = "io.github.xsheep.composelightbox"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 16

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.activity.compose)
    implementation(libs.glide)
    implementation(libs.glide.compose)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
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
