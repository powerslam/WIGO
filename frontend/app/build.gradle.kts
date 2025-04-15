import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val arcoreLibPath = "${buildDir}/arcore-native"

configurations {
    create("natives")
}

android {
    namespace = "com.capstone.whereigo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.capstone.whereigo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++11", "-Wall")
                arguments += listOf(
                    "-DANDROID_STL=c++_static",
                    "-DARCORE_LIBPATH=$arcoreLibPath/jni",
                    "-DARCORE_INCLUDE=${project.rootDir}/app/src/main/cpp/include/arcore",
                    "-DGLM_INCLUDE=${project.rootDir}/app/src/main/cpp/include/glm"
                )
            }
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("CMakeLists.txt")
        }
    }

    viewBinding{
        enable = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("com.google.ar:core:1.48.0")
    implementation(libs.androidx.databinding.runtime)
    add("natives", "com.google.ar:core:1.48.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.preference.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.material.v1110)

    // import Kotlin API client BOM
    implementation(platform("com.aallam.openai:openai-client-bom:4.0.1"))
    // define dependencies without versions (handled by BOM)
    implementation("com.aallam.openai:openai-client")
    runtimeOnly("io.ktor:ktor-client-okhttp")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // json 형식 의존성

}

val extractNativeLibraries by tasks.registering(Copy::class) {
    // Always extract to ensure updated libs
    outputs.upToDateWhen { false }

    from({
        configurations.getByName("natives").map { zipTree(it) }
    })
    into(arcoreLibPath)
    include("jni/**/*")
}

// Ensure native libs are extracted before external native build tasks
tasks.configureEach {
    if ((name.contains("external", ignoreCase = true) || name.contains("CMake", ignoreCase = true)) &&
        !name.contains("Clean", ignoreCase = true)
    ) {
        dependsOn(extractNativeLibraries)
    }
}
