// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.8.2" apply false
    id("com.android.library") version "8.9.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false
    id("org.jmailen.kotlinter") version "5.0.1" apply false
    id("com.google.devtools.ksp") version "2.1.10-1.0.31" apply false
}

subprojects {
    apply(plugin = "org.jmailen.kotlinter") // Version should be inherited from parent
}
