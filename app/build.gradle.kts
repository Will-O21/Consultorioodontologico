plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") version "2.1.10" // Versión actualizada
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Para procesamiento de anotaciones (ej: Room)
}

dependencies {
    // ---------------------------------------------------------------
    // Sheets-Compose-Dialogs (Componentes UI personalizados)
    // ---------------------------------------------------------------
    implementation(libs.sheets.core)
    implementation(libs.sheets.calendar)
    implementation(libs.sheets.clock)
    // ---------------------------------------------------------------
    // Core & Kotlin
    // ---------------------------------------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // ---------------------------------------------------------------
    // Jetpack Compose (UI Moderna)
    // ---------------------------------------------------------------
    implementation(platform(libs.androidx.compose.bom)) // BOM para versiones alineadas
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.runtime)
    debugImplementation(libs.androidx.ui.tooling) // Preview en modo debug

    // Componentes adicionales de Compose
    implementation(libs.coil.compose) // Carga de imágenes
    implementation(libs.compose.material.dialogs.datetime) // Diálogos de fecha/hora
    // ---------------------------------------------------------------
    // Navigation & Arquitectura
    // ---------------------------------------------------------------
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel en Compose
    implementation(libs.lifecycle.livedata.ktx) // LiveData
    // ---------------------------------------------------------------
    // Datos y Persistencia
    // ---------------------------------------------------------------
    implementation(libs.gson) // Serialización JSON
    implementation(libs.androidx.room.runtime) // Base de datos local
    implementation(libs.room.ktx) // Coroutines para Room
    kapt(libs.room.compiler)// Procesador de Room
    // ---------------------------------------------------------------
    // Coroutines
    // ---------------------------------------------------------------
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime) // Manejo de fechas
    // ---------------------------------------------------------------
    // Retrofit (APIs)
    // ---------------------------------------------------------------
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // ---------------------------------------------------------------
    // CameraX (Manejo de cámara)
    // ---------------------------------------------------------------
    implementation(libs.androidx.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    // ---------------------------------------------------------------
    // Firebase (Notificaciones push)
    // ---------------------------------------------------------------
    implementation(libs.firebase.messaging)
    implementation("com.google.firebase:firebase-firestore:25.1.2")
    implementation("com.google.firebase:firebase-auth:23.2.0")

    // Testing avanzado
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("app.cash.turbine:turbine:0.12.1")
    // ---------------------------------------------------------------
    // Testing
    // ---------------------------------------------------------------
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.ui)
    implementation(libs.kotlinx.metadata.jvm)
}

android {
    namespace = "com.wdog.consultorioodontolgico"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wdog.consultorioodontolgico"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kapt {
        useBuildCache = true
        correctErrorTypes = true


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true // Habilita Jetpack Compose
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0") // Versión compatible con Room 2.7.0
    }}
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}