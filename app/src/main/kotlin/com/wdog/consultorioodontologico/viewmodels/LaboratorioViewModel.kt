package com.wdog.consultorioodontologico.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.dao.LaboratorioDao
import com.wdog.consultorioodontologico.entities.Laboratorio
import com.wdog.consultorioodontologico.sync.FirestoreSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaboratorioViewModel @Inject constructor(
    private val laboratorioDao: LaboratorioDao,
    private val firestoreSync: FirestoreSync
) : ViewModel() {

    // Flujo de datos para la lista
    val todosLosLaboratorios: Flow<List<Laboratorio>> = laboratorioDao.obtenerTodos()

    fun guardarLaboratorio(laboratorio: Laboratorio) {
        viewModelScope.launch(Dispatchers.IO) {
            if (laboratorio.id == 0L) {
                val id = laboratorioDao.insertar(laboratorio)
                firestoreSync.syncLaboratorioToCloud(laboratorio.copy(id = id))
            } else {
                laboratorioDao.actualizar(laboratorio)
                firestoreSync.syncLaboratorioToCloud(laboratorio)
            }
        }
    }

    fun eliminarLaboratorio(laboratorio: Laboratorio) {
        viewModelScope.launch(Dispatchers.IO) {
            laboratorioDao.eliminar(laboratorio)
            firestoreSync.deleteLaboratorioFromCloud(laboratorio.id)
        }
    }
}