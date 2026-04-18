plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.3.20"
}

android {
    namespace = "com.dessalines.habitmaker"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.dessalines.habitmaker"
        // Add full to match the full version of the app
        applicationIdSuffix = ".full"
        versionNameSuffix = "-full"

        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"

        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    }

    // Necessary for izzyondroid releases
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    if (project.hasProperty("RELEASE_STORE_FILE")) {
        signingConfigs {
            create("release") {
                storeFile = file(project.property("RELEASE_STORE_FILE")!!)
                storePassword = project.property("RELEASE_STORE_PASSWORD") as String?
                keyAlias = project.property("RELEASE_KEY_ALIAS") as String?
                keyPassword = project.property("RELEASE_KEY_PASSWORD") as String?

                // Optional, specify signing versions used
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }


    buildTypes {
        release {
            if (project.hasProperty("RELEASE_STORE_FILE")) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " (DEBUG)"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":db"))
    implementation("androidx.wear.compose:compose-material:1.6.1")
    implementation("androidx.wear:wear-input:1.2.0")

    // Room
    ksp("androidx.room:room-compiler:2.8.4")
    // To use Kotlin annotation processing tool
    implementation("androidx.room:room-runtime:2.8.4")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // PrettyNum
    implementation("com.github.dessalines:pretty-num-kotlin:0.0.2")

    implementation("androidx.wear.compose:compose-navigation:1.6.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.runtime:runtime-livedata:1.10.6")
    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.wear.compose:compose-material3:1.6.1")
    implementation("androidx.wear.compose:compose-foundation:1.6.1")
    implementation("androidx.wear.compose:compose-ui-tooling:1.6.1")
    implementation("androidx.wear:wear-tooling-preview:1.0.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.wear.protolayout:protolayout:1.4.0")
    implementation("androidx.wear.protolayout:protolayout-material3:1.4.0")
    implementation("com.google.guava:guava:33.6.0-jre")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.03.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}