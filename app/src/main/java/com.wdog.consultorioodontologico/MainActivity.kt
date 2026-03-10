package com.wdog.consultorioodontologico

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wdog.consultorioodontologico.navigation.AppNavigation
import com.wdog.consultorioodontologico.ui.theme.ConsultorioOdontologicoTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.work.*
import com.wdog.consultorioodontologico.workers.ResumenDiarioWorker
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        programarResumenDiario()
        setContent {
            ConsultorioOdontologicoTheme {
                SolicitarPermisoNotificaciones()
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Llamamos a la navegación centralizada del archivo AppNavigation.kt
                    AppNavigation()
                }
            }
        }
    }

    private fun programarResumenDiario() {
        val workManager = WorkManager.getInstance(this)
        val ahora = Calendar.getInstance()
        val proximaEjecucion = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (proximaEjecucion.before(ahora)) proximaEjecucion.add(Calendar.DAY_OF_MONTH, 1)

        val demoraInicial = proximaEjecucion.timeInMillis - ahora.timeInMillis
        val request = PeriodicWorkRequestBuilder<ResumenDiarioWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(demoraInicial, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()

        workManager.enqueueUniquePeriodicWork(
            "ResumenDiarioCitas",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

@Composable
fun SolicitarPermisoNotificaciones() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}