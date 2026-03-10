package com.wdog.consultorioodontologico.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.database.AppDatabase
import com.wdog.consultorioodontologico.entities.Nota
import com.wdog.consultorioodontologico.sync.FirestoreSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotaViewModel @Inject constructor(
    application: Application,
    private val firestoreSync: FirestoreSync,

) : AndroidViewModel(application) {
    private val notaDao = AppDatabase.getDatabase(application).notaDao()

    fun obtenerNotasLocales(): Flow<List<Nota>> = notaDao.obtenerTodasLasNotas()

    init {
        sincronizarNotas() // Así la función deja de estar "gris" (unused)
    }

    private suspend fun obtenerNotasRemotas(): List<Nota> {
        return withContext(Dispatchers.IO) {
            firestoreSync.getNotasFromCloud()
        }
    }

    fun sincronizarNotas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notasRemotas = obtenerNotasRemotas()
                val notasLocales = notaDao.obtenerTodasLasNotasSimple()
                // Creamos un set de IDs locales para búsqueda rápida
                val idsLocales = notasLocales.map { it.id }.toSet()

                notasRemotas.forEach { remota ->
                    // CAMBIO: Solo insertamos en Room si la nota NO existe localmente.
                    // Esto evita que la versión vieja de la nube sobreescriba tu cambio de "isListo"
                    if (!idsLocales.contains(remota.id)) {
                        notaDao.insertarNota(remota)
                    }
                }
            } catch (e: Exception) {
                Log.e("NotaViewModel", "Error en sincronización: ${e.message}")
            }
        }
    }

    fun insertarNota(nota: Nota) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Insertamos en Room y obtenemos el ID real generado
                val nuevoId = notaDao.insertarNota(nota)

                // 2. Creamos una copia de la nota con el ID correcto
                val notaConIdReal = nota.copy(id = nuevoId.toInt())

                // 3. Sincronizamos a la nube con el ID definitivo
                firestoreSync.syncNotaToCloud(notaConIdReal)
            } catch (e: Exception) {
                Log.e("NotaViewModel", "Error al insertar: ${e.message}")
            }
        }
    }

    fun actualizarNota(nota: Nota) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Primero local para que la UI cambie de inmediato
                notaDao.actualizarNota(nota)
                // 2. Actualizamos la nube para que cuando 'sincronizar' corra, tenga lo nuevo
                firestoreSync.syncNotaToCloud(nota)
            } catch (e: Exception) {
                Log.e("NotaViewModel", "Error al actualizar: ${e.message}")
            }
        }
    }
    fun eliminarNota(nota: Nota) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // EL ORDEN IMPORTA: Primera nube, luego local
                firestoreSync.deleteNotaFromCloud(nota.id)
                notaDao.eliminarNota(nota.id)
            } catch (e: Exception) {
                Log.e("NotaViewModel", "Error al eliminar: ${e.message}")
            }
        }
    }

    fun eliminarMultiplesNotas(notas: List<Nota>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Borramos en la nube
                notas.forEach { firestoreSync.deleteNotaFromCloud(it.id) }
                // Borramos localmente en bloque
                notaDao.eliminarMultiplesNotas(notas)
            } catch (e: Exception) {
                Log.e("NotaViewModel", "Error al eliminar múltiples: ${e.message}")
            }
        }
    }

    fun toggleChecklistItem(nota: Nota, index: Int, isChecked: Boolean) {
        val checklistActual = nota.obtenerChecklist().toMutableList()
        if (index in checklistActual.indices) {
            checklistActual[index] = checklistActual[index].copy(second = isChecked)

            // Verificamos si todos están marcados para sugerir pasarlo a "Listo"
            val todosListos = checklistActual.all { it.second }

            val notaActualizada = nota.copyWithChecklist(checklistActual).copy(
                isListo = if (todosListos) true else nota.isListo,
                fechaCompletado = if (todosListos) System.currentTimeMillis() else nota.fechaCompletado
            )
            actualizarNota(notaActualizada)
        }
    }


    val conteoNotasUrgentes: Flow<Int> = obtenerNotasLocales().map { lista ->
        // Este es el valor exacto que usas en tu lógica de ordenamiento
        val colorRojoUrgente = androidx.compose.ui.graphics.Color(0xFFa51b0b).toArgb()

        lista.count { it.colorTitulo == colorRojoUrgente }
    }

}