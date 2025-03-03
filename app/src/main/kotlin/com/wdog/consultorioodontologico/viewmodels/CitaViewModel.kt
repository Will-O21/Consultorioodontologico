package com.wdog.consultorioodontologico.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.database.AppDatabase
import com.wdog.consultorioodontologico.entities.Cita
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CitaViewModel(application: Application) : AndroidViewModel(application) {
    private val citaDao = AppDatabase.getDatabase(application).citaDao()

    fun insertarCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.insert(cita)
        }
    }

    fun obtenerCitas(): Flow<List<Cita>> {
        return citaDao.getAllCitas()
    }

    fun actualizarCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.updateCita(cita)
        }
    }

    fun eliminarCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.deleteCita(cita)
        }
    }
}