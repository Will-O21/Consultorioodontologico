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

    // Insertar una cita
    fun insertarCita(cita: Cita) {
        viewModelScope.launch {
            citaDao.insert(cita)
        }
    }

    // Obtener todas las citas
    fun obtenerCitas(): Flow<List<Cita>> {
        return citaDao.getAllCitas() // Cambiar a getAllCitas()
    }
}
