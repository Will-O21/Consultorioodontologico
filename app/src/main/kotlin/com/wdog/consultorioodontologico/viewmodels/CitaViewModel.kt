package com.wdog.consultorioodontologico.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.wdog.consultorioodontologico.database.AppDatabase
import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.sync.FirestoreSync
import com.wdog.consultorioodontologico.workers.NotificacionWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class CitaViewModel @Inject constructor(
    application: Application,
    private val firestoreSync: FirestoreSync
) : AndroidViewModel(application) {

    private val citaDao = AppDatabase.getDatabase(application).citaDao()
    private val workManager = WorkManager.getInstance(application)

    init {
        viewModelScope.launch {
            sincronizarCitasIniciales()
        }
    }

    // --- TUS FUNCIONES ORIGINALES ---

    fun obtenerCitasLocales(): Flow<List<Cita>> = citaDao.getAllCitas()
    @Suppress("unused")
    suspend fun obtenerCitasRemotas(): List<Cita> {
        return withContext(Dispatchers.IO) {
            firestoreSync.getCitasFromCloud()
        }
    }

    suspend fun sincronizarCitasIniciales() {
        withContext(Dispatchers.IO) {
            val citasRemotas = firestoreSync.getCitasFromCloud()
            citasRemotas.forEach { citaDao.insert(it) }
        }
    }

    // --- OPERACIONES CRUD CON NOTIFICACIONES ---

    fun insertarCita(cita: Cita) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = citaDao.insert(cita)
                val citaConId = cita.copy(id = id)
                firestoreSync.syncCitaToCloud(citaConId)

                // Programar notificación para la nueva cita
                programarNotificacion(citaConId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarCita(cita: Cita) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                citaDao.updateCita(cita)
                firestoreSync.syncCitaToCloud(cita)

                // Cancelar la anterior y programar la nueva por si cambió la fecha
                cancelarNotificacion(cita.id)
                programarNotificacion(cita)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarCita(cita: Cita) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                citaDao.deleteCita(cita)
                firestoreSync.deleteCita(cita.id)

                // Cancelar notificación si la cita se borra
                cancelarNotificacion(cita.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- LÓGICA DE WORKMANAGER ---

    private fun programarNotificacion(cita: Cita) {
        val ahora = LocalDateTime.now()
        val tiempoCita = cita.fechaHora

        // 1. Calcular tiempos de espera (delays)
        val delay30Min = Duration.between(ahora, tiempoCita).minusMinutes(30).toMillis()
        val delayExacto = Duration.between(ahora, tiempoCita).toMillis()

        // 2. Programar aviso de 30 minutos antes
        if (delay30Min > 0) {
            val data30Min = workDataOf(
                "titulo" to "Cita próxima en 30 min",
                "mensaje" to "Prepárate para la consulta con ${cita.pacienteNombre}",
                "citaId" to cita.id
            )

            val request30Min = OneTimeWorkRequestBuilder<NotificacionWorker>()
                .setInitialDelay(delay30Min, TimeUnit.MILLISECONDS)
                .setInputData(data30Min)
                .addTag("cita_30m_${cita.id}") // Etiqueta específica para los 30 min
                .build()

            workManager.enqueueUniqueWork(
                "work_30m_${cita.id}",
                ExistingWorkPolicy.REPLACE,
                request30Min
            )
        }

        // 3. Programar aviso para la hora exacta
        if (delayExacto > 0) {
            val dataExacto = workDataOf(
                "titulo" to "¡Es hora de la cita!",
                "mensaje" to "La consulta de ${cita.pacienteNombre} está programada para este momento.",
                "citaId" to cita.id
            )

            val requestExacto = OneTimeWorkRequestBuilder<NotificacionWorker>()
                .setInitialDelay(delayExacto, TimeUnit.MILLISECONDS)
                .setInputData(dataExacto)
                .addTag("cita_exacta_${cita.id}") // Etiqueta específica para la hora exacta
                .build()

            workManager.enqueueUniqueWork(
                "work_exacta_${cita.id}",
                ExistingWorkPolicy.REPLACE,
                requestExacto
            )
        }
    }

    private fun cancelarNotificacion(citaId: Long) {
        // Debemos cancelar ambas etiquetas si la cita se elimina o reprograma
        workManager.cancelAllWorkByTag("cita_30m_${citaId}")
        workManager.cancelAllWorkByTag("cita_exacta_${citaId}")
    }
    // Contador reactivo para citas de hoy pendientes
    val conteoCitasHoy: Flow<Int> = obtenerCitasLocales().map { lista ->
        val ahora = LocalDateTime.now()
        lista.count { cita ->
            val esHoy = cita.fechaHora.toLocalDate() == ahora.toLocalDate()
            val esPendiente = cita.fechaHora.isAfter(ahora)
            esHoy && esPendiente
        }
    }



}