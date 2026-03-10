package com.wdog.consultorioodontologico.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wdog.consultorioodontologico.MainActivity
import com.wdog.consultorioodontologico.R
import com.wdog.consultorioodontologico.database.AppDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ResumenDiarioWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val citaDao = database.citaDao()

        // Definir el rango de "hoy" (desde las 00:00 hasta las 23:59)
        val hoyInicio = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        val hoyFin = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)

        val conteo = citaDao.contarCitasDelDia(hoyInicio, hoyFin)

        if (conteo > 0) {
            mostrarNotificacion(conteo)
        }

        return Result.success()
    }

    private fun mostrarNotificacion(cantidad: Int) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal de notificaciones
        val channel = NotificationChannel(
            "resumen_diario_channel",
            "Resumen Diario",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Resumen matutino de citas"
        }
        notificationManager.createNotificationChannel(channel)

        // Deep Link para abrir la pantalla de CITAS
        // Usamos la ruta que tienes en AppNavigation para la lista de citas
        val deepLinkUri = "app://consultorio.com/citas".toUri()
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri, applicationContext, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            999, // ID único para el resumen
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, "resumen_diario_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Agenda del día")
            .setContentText("Hoy tienes $cantidad ${if (cantidad == 1) "cita programada" else "citas programadas"}.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(999, notification)
    }
}