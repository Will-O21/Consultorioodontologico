package com.wdog.consultorioodontologico.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wdog.consultorioodontolgico.R


class NotificacionWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val titulo = inputData.getString("titulo") ?: "Recordatorio de Cita"
        val mensaje = inputData.getString("mensaje") ?: "Tienes una cita programada"

        // Crear la notificación
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, "citas_channel")
            .setSmallIcon(R.drawable.ic_notification) // Cambia por tu ícono de notificación
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Mostrar la notificación
        notificationManager.notify(1, notification)

        return Result.success()
    }
}