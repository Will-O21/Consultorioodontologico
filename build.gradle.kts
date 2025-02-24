// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

}

// build.gradle (nivel del proyecto)
buildscript {
    repositories {
        google()
        mavenCentral()

    }
    dependencies {
        // Aquí está la línea que debes modificar:
        classpath (libs.gradle) // <-- Cambia esta versión
        classpath (libs.kotlin.gradle.plugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io")
    }
}}