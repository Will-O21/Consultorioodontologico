package com.wdog.consultorioodontologico.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wdog.consultorioodontologico.ui.citas.PantallaAgregarCita
import com.wdog.consultorioodontologico.ui.citas.PantallaCitas
import com.wdog.consultorioodontologico.ui.inicio.PantallaInicio
import com.wdog.consultorioodontologico.ui.pacientes.PantallaPacientes
import com.wdog.consultorioodontologico.ui.pagos.PantallaPagos
import com.wdog.consultorioodontologico.ui.registro.PantallaRegistro
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModelFactory


object AppNavigation {
    const val INICIO = "inicio"
    const val REGISTRO = "registro"
    const val PACIENTES = "pacientes"
    const val CITAS = "citas"
    const val PAGOS = "pagos"
    const val AGREGAR_CITA = "agregar_cita"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Inicializar ViewModel correctamente
    val viewModel: PacienteViewModel = viewModel(
        factory = PacienteViewModelFactory(context.applicationContext as Application)
    )

    // Colectar pacientes como estado
    val pacientes by viewModel.todosLosPacientes.collectAsState(initial = emptyList())

    NavHost(
        navController = navController,
        startDestination = AppNavigation.INICIO
    ) {
        composable(AppNavigation.INICIO) {
            PantallaInicio(navController = navController)
        }
        composable(AppNavigation.REGISTRO) {
            PantallaRegistro(navController = navController)
        }
        composable(AppNavigation.PACIENTES) {
            PantallaPacientes(
                pacientes = pacientes,
                navController = navController
            )
        }
        composable(AppNavigation.CITAS) {
            val citaViewModel: CitaViewModel = viewModel() // Obtener el ViewModel
            PantallaCitas(
                navController = navController,
                viewModel = citaViewModel // Pasar el ViewModel a PantallaCitas
            )
        }
        composable(AppNavigation.PAGOS) {
            PantallaPagos(
                pacientes = pacientes,
                navController = navController
            )
        }
        composable(AppNavigation.AGREGAR_CITA) {
            val citaViewModel: CitaViewModel = viewModel()
            val pacienteViewModel: PacienteViewModel = viewModel(
                factory = PacienteViewModelFactory(context.applicationContext as Application)
            )
            PantallaAgregarCita(
                navController = navController,
                citaViewModel = citaViewModel,
                pacienteViewModel = pacienteViewModel
            )
        }
    }}