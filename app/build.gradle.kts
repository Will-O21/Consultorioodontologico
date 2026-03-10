plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)// Versión actualizada
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Para procesamiento de anotaciones (ej: Room)
    id("com.google.gms.google-services")
    id ("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")// Añade este plugin
    alias(libs.plugins.ksp) // <--- AGREGAR ESTO
}

dependencies {

    implementation(libs.play.services.auth)

    // ---------------------------------------------------------------
    // Para tomar valores de internet
    implementation(libs.jsoup)
    // ---------------------------------------------------------------
    // Serialización
    // ---------------------------------------------------------------
    implementation (libs.kotlinx.serialization.json)
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.espresso.core)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.games.activity)
    testImplementation(libs.androidx.runner)
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
    ksp(libs.androidx.room.compiler)
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

    implementation(platform(libs.firebase.bom)) // Controla todas las versiones automáticamente
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation (libs.google.firebase.auth.ktx)
    implementation (libs.google.firebase.firestore.ktx)
    implementation (libs.firebase.analytics.ktx)
    implementation (libs.google.firebase.messaging.ktx)
    implementation(libs.firebase.analytics)

    // WorkManager
        implementation (libs.androidx.work.runtime.ktx)

    // Dependencias básicas de Hilt
    implementation (libs.hilt.android) // Versión más reciente
    kapt (libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    // Para @HiltViewModel
    //implementation (libs.androidx.hilt.lifecycle.viewmodel)
    //kapt (libs.androidx.hilt.compiler)



    // Testing avanzado
    testImplementation(libs.mockk)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)

        // Dependencias para pruebas de UI
    androidTestImplementation(libs.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.turbine)
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
    //Iconos
    implementation (libs.androidx.material.icons.extended)
}

android {
    namespace = "com.wdog.consultorioodontologico"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wdog.consultorioodontologico"
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
        arguments {

        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "2.0"  // ¡Clave! Forzar versión compatible
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    buildFeatures {
        compose = true // Habilita Jetpack Compose
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlinx" && requested.name == "kotlinx-metadata-jvm") {
                useVersion("0.9.0") // Esta versión es compatible con Kotlin 2.x
            }
        }
    }
}



allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}