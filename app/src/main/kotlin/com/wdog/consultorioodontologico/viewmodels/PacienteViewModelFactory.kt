package com.wdog.consultorioodontologico.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PacienteViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PacienteViewModel::class.java)) {
            val viewModel = PacienteViewModel(application)
            modelClass.cast(viewModel) // Conversión segura
        } else {
            throw IllegalArgumentException("Clase ViewModel desconocida")
        } ?: throw IllegalArgumentException("Error inesperado al convertir ViewModel")
    }
}