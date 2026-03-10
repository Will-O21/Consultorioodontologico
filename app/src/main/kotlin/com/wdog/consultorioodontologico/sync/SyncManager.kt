package com.wdog.consultorioodontologico.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wdog.consultorioodontologico.dao.AfeccionDao
import com.wdog.consultorioodontologico.database.AppDatabase
import com.wdog.consultorioodontologico.entities.Afeccion
import com.wdog.consultorioodontologico.entities.DienteEstadoEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@HiltWorker
class SyncManager @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val firestoreSync: FirestoreSync,
    private val db: AppDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("SyncManager", "Iniciando sincronización completa")
            SyncState.setSincronizado(true) // <--- ASUMIMOS ÉXITO AL INICIAR
            // 1. Sincronización principal en transacción
            db.withTransaction {
                syncPacientes()
                syncCitas()
                syncNotas()
                syncGastos()
                syncCatalogo()
                syncLaboratorios()
                syncOdontogramas()
                syncInventario()
            }

            Log.d("SyncManager", "Sincronización completada exitosamente")
            Result.success()
        } catch (e: Exception) {
            SyncState.setSincronizado(false) // <--- SI FALLA, AVISAMOS A LA UI
            Log.e("SyncManager", "Error en sincronización: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun syncPacientes() {
        try {
            val pacientesCloud = firestoreSync.getPacientesFromCloud()
            val pacienteDao = db.pacienteDao()
            val afeccionDao = db.afeccionDao()

            pacientesCloud.forEach { paciente ->
                // Insertar/actualizar paciente
                pacienteDao.insertPaciente(paciente)

                // Sincronizar afecciones relacionadas
                syncAfeccionesForPaciente(paciente.id, afeccionDao)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error sincronizando pacientes", e)
            throw e
        }
    }

    private suspend fun syncAfeccionesForPaciente(pacienteId: Long, afeccionDao: AfeccionDao) {
        try {
            val afecciones = firestoreSync.afeccionesCollection
                .whereEqualTo("pacienteId", pacienteId)
                .get()
                .await()
                .documents
                .mapNotNull { Afeccion.fromMap(it.data ?: emptyMap()) }

            afeccionDao.deleteByPacienteId(pacienteId)
            afecciones.forEach { afeccionDao.insertAfeccion(it) }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error sincronizando afecciones para paciente $pacienteId", e)
        }
    }

    private suspend fun syncCitas() {
        try {
            val citas = firestoreSync.getCitasFromCloud()
            val citaDao = db.citaDao()

            citas.forEach { cita ->
                // Usar insert que devuelve ID para manejar conflictos
                citaDao.insert(cita)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error sincronizando citas", e)
        }
    }

    private suspend fun syncNotas() {
        try {
            val notas = firestoreSync.getNotasFromCloud()
            val notaDao = db.notaDao()

            notas.forEach { nota ->
                notaDao.insertarNota(nota)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error sincronizando notas", e)
        }
    }

    private suspend fun syncGastos() {
        try {
            val gastos = firestoreSync.getGastosFromCloud()
            val gastoDao = db.gastoDao()
            gastos.forEach { gastoDao.insertarGasto(it) }
        } catch (e: Exception) { Log.e("SyncManager", "Error en gastos", e) }
    }

    private suspend fun syncCatalogo() {
        try {
            val servicios = firestoreSync.getCatalogoFromCloud()
            val servicioDao = db.servicioPresupuestoDao()
            servicios.forEach { servicioDao.insertarServicio(it) }
        } catch (e: Exception) { Log.e("SyncManager", "Error en catálogo", e) }
    }

    private suspend fun syncLaboratorios() {
        try {
            val labs = firestoreSync.getLaboratoriosFromCloud()
            val labDao = db.laboratorioDao()
            labs.forEach { labDao.insertar(it) }
        } catch (e: Exception) { Log.e("SyncManager", "Error en laboratorios", e) }
    }

    private suspend fun syncOdontogramas() {
        try {
            // Nota: Aquí sincronizamos basándonos en los presupuestos existentes
            // Opcionalmente puedes bajar todos los estados directamente
            val query = firestoreSync.dientesEstadosCollection.get().await()
            val estados = query.documents.mapNotNull { it.toObject(DienteEstadoEntity::class.java) }
            val dao = db.dienteEstadoDao()

            estados.forEach { dao.insertarEstado(it) }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error sincronizando odontogramas", e)
        }
    }


    // Añade esto al final de la clase SyncManager
    companion object {
        fun enqueue(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<SyncManager>()
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                )
                .build()
            androidx.work.WorkManager.getInstance(context).enqueue(request)
        }
    }

    private suspend fun syncInventario() {
        try {
            val materiales = firestoreSync.getInventarioFromCloud()
            val kits = firestoreSync.getKitsFromCloud()
            val dao = db.inventarioDao()

            materiales.forEach { dao.insertarMaterial(it) }
            kits.forEach { dao.insertarKit(it) }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error sincronizando inventario", e)
        }
    }
}