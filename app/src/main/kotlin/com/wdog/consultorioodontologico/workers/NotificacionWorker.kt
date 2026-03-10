package com.wdog.consultorioodontologico.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wdog.consultorioodontologico.MainActivity
import com.wdog.consultorioodontologico.R

class NotificacionWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val titulo = inputData.getString("titulo") ?: "Recordatorio de Cita"
        val mensaje = inputData.getString("mensaje") ?: "Tienes una cita programada"

        // 1. Extraemos el ID como Long para que el Deep Link sea exacto
        val citaIdLong = inputData.getLong("citaId", 0L)

        // 2. Convertimos a Int solo para el ID visual de la notificación en la barra
        val notificationId = citaIdLong.toInt()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Configuración del canal (API 26+)
        val channel = NotificationChannel(
            "citas_channel",
            "Citas Odontológicas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Recordatorios de citas médicas"
            enableVibration(true)
            setBypassDnd(true) // Permitir en No Molestar
        }
        notificationManager.createNotificationChannel(channel)

        // 3. Crear el Deep Link URI con el Long original
        val deepLinkUri = "app://consultorio.com/detalle/$citaIdLong".toUri()

        // 4. Crear el Intent para abrir la app en la pantalla de detalle
        val intent = Intent(
            Intent.ACTION_VIEW,
            deepLinkUri,
            applicationContext,
            MainActivity::class.java
        ).apply {
            // Asegura que la app se comporte correctamente al abrirse desde afuera
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId, // Usamos el ID de notificación como request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, "citas_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Se borra al tocarla
            .build()

        // 5. Mostrar la notificación usando el ID entero
        notificationManager.notify(notificationId, notification)

        return Result.success()
    }
}