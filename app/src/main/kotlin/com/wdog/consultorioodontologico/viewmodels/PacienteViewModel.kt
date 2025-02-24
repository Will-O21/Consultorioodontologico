package com.wdog.consultorioodontologico.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.database.AppDatabase
import com.wdog.consultorioodontologico.entities.Paciente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PacienteViewModel(application: Application) : AndroidViewModel(application) {
    private val pacienteDao = AppDatabase.getDatabase(application).pacienteDao()

    // Usar Flow directamente
    val todosLosPacientes: Flow<List<Paciente>> = pacienteDao.getAllPacientes()

    fun insertarPaciente(paciente: Paciente) {
        viewModelScope.launch {
            pacienteDao.insert(paciente)
        }
    }
}