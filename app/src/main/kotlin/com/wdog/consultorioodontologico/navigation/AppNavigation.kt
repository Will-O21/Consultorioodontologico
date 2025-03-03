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
import com.wdog.consultorioodontologico.ui.citas.PantallaDetalleCita
import com.wdog.consultorioodontologico.ui.gestionconsultorio.PantallaGestionConsultorio
import com.wdog.consultorioodontologico.ui.inicio.PantallaInicio
import com.wdog.consultorioodontologico.ui.pacientes.PantallaEditarPaciente
import com.wdog.consultorioodontologico.ui.pacientes.PantallaPacientes
import com.wdog.consultorioodontologico.ui.pagos.PantallaEditarPago
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
    const val EDITAR_PACIENTE = "editar_paciente/{id}"
    const val DETALLE_CITA = "detalle_cita/{id}"
    const val EDITAR_PAGO = "editar_pago/{id}"
    const val GESTION_CONSULTORIO = "gestion_consultorio"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val pacienteViewModel: PacienteViewModel = viewModel(
        factory = PacienteViewModelFactory(context.applicationContext as Application)
    )
    val citaViewModel: CitaViewModel = viewModel()

    val pacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())
    val citas by citaViewModel.obtenerCitas().collectAsState(initial = emptyList())

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
                navController = navController,
                viewModel = pacienteViewModel
            )
        }
        composable(AppNavigation.CITAS) {
            PantallaCitas(
                navController = navController,
                citaViewModel = citaViewModel,
                pacienteViewModel = pacienteViewModel
            )
        }
        composable(AppNavigation.PAGOS) {
            PantallaPagos(
                pacientes = pacientes,
                navController = navController,
                viewModel = pacienteViewModel
            )
        }
        composable(AppNavigation.AGREGAR_CITA) {
            PantallaAgregarCita(
                navController = navController,
                citaViewModel = citaViewModel,
                pacienteViewModel = pacienteViewModel
            )
        }
        composable(AppNavigation.EDITAR_PACIENTE) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            val paciente = pacientes.find { it.id == pacienteId }
            if (paciente != null) {
                PantallaEditarPaciente(
                    navController = navController,
                    paciente = paciente,
                    viewModel = pacienteViewModel
                )
            }
        }
        composable(AppNavigation.DETALLE_CITA) { backStackEntry ->
            val citaId = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            val cita = citas.find { it.id == citaId }
            if (cita != null) {
                PantallaDetalleCita(
                    navController = navController,
                    cita = cita,
                    viewModel = citaViewModel
                )
            }
        }
        composable(AppNavigation.EDITAR_PAGO) { backStackEntry ->
            val pacienteId = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            val paciente = pacientes.find { it.id == pacienteId }
            if (paciente != null) {
                PantallaEditarPago(
                    navController = navController,
                    paciente = paciente,
                    viewModel = pacienteViewModel
                )
            }
        }
        // Nueva pantalla de gestión del consultorio
        composable(AppNavigation.GESTION_CONSULTORIO) {
            PantallaGestionConsultorio()
        }
    }
}