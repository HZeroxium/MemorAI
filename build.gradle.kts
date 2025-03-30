// build.gradle.kts (Project)
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2") // Android Gradle plugin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // Kotlin plugin
        classpath("com.google.gms:google-services:4.4.2") // Google Services plugin
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.1")

    }
}
