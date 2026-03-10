package com.wdog.consultorioodontologico

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.wdog.consultorioodontologico.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Configuración de Firebase
        FirebaseApp.initializeApp(this)
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        }

        // Configuración de WorkManager
        val syncRequest = PeriodicWorkRequestBuilder<SyncManager>(
            repeatInterval = 12,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "syncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        // Solo para debug - monitoreo del estado
        if (BuildConfig.DEBUG) {
            WorkManager.getInstance(this)
                .getWorkInfosForUniqueWorkLiveData("syncWork")
                .observeForever { workInfos ->
                    Log.d("SyncDebug", "Estado: ${workInfos?.firstOrNull()?.state}")
                }
        }
    }
}