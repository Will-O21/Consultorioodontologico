package com.wdog.consultorioodontologico.navigation


import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.wdog.consultorioodontologico.ui.dashboard.PantallaDashboard
import com.wdog.consultorioodontologico.ui.citas.PantallaAgregarCita
import com.wdog.consultorioodontologico.ui.citas.PantallaCitas
import com.wdog.consultorioodontologico.ui.citas.PantallaDetalleCita
import com.wdog.consultorioodontologico.ui.finanzas.PantallaFinanzas
import com.wdog.consultorioodontologico.ui.gestionconsultorio.PantallaGestionConsultorio
import com.wdog.consultorioodontologico.ui.inicio.PantallaInicio
import com.wdog.consultorioodontologico.ui.laboratorios.PantallaLaboratorios
import com.wdog.consultorioodontologico.ui.pacientes.PantallaEditarPaciente
import com.wdog.consultorioodontologico.ui.pacientes.PantallaPacientes
import com.wdog.consultorioodontologico.ui.pagos.PantallaPagos
import com.wdog.consultorioodontologico.ui.presupuesto.PantallaPresupuesto
import com.wdog.consultorioodontologico.ui.registro.PantallaRegistro
import com.wdog.consultorioodontologico.viewmodels.*
import androidx.core.content.edit // <--- AÑADIR ESTO
import androidx.compose.ui.graphics.Color // PARA: Unresolved reference 'Color'
import androidx.compose.ui.unit.sp // PARA: Unresolved reference 'sp'
import androidx.compose.foundation.layout.width // PARA: Unresolved reference 'width'
import androidx.compose.material.icons.filled.Image // PARA: Unresolved reference 'Image'
import com.wdog.consultorioodontologico.ui.inventario.PantallaInventarioPrincipal

object AppNavigation {
    const val DASHBOARD = "dashboard"
    const val INICIO = "inicio"
    const val REGISTRO = "registro"
    const val PACIENTES = "pacientes"
    const val CITAS = "citas"
    const val PAGOS = "pagos"
    const val AGREGAR_CITA = "agregar_cita"
    const val EDITAR_PACIENTE = "editar_paciente/{id}"
    const val DETALLE_CITA = "detalle_cita/{id}"
    const val GESTION_CONSULTORIO = "gestion_consultorio"
    // NUEVAS RUTAS
    const val PRESUPUESTO = "presupuesto"
    const val FINANZAS = "finanzas"

    const val LABORATORIOS = "laboratorios"

    const val INVENTARIO = "inventario"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("config_consultorio", Context.MODE_PRIVATE) }

    // --- ESTADO DEL DIÁLOGO DE CONFIGURACIÓN ---
    var mostrarDialogoConfig by remember { mutableStateOf(false) }

    if (mostrarDialogoConfig) {
        var tempNombre by remember { mutableStateOf(prefs.getString("nombre_doc", "Doc") ?: "Doc") }
        var tempConsultorio by remember { mutableStateOf(prefs.getString("nombre_cons", "Gestión Odontológica Profesional") ?: "Gestión Odontológica Profesional") }
// Launcher para seleccionar el logo
        val launcherLogo = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            uri?.let {
                prefs.edit { putString("logo_path", it.toString()) }

                // Sincronizamos con la preferencia que lee el PDF
                context.getSharedPreferences("config_odontograma", Context.MODE_PRIVATE)
                    .edit { putString("logo_path", it.toString()) }
            }
        }
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfig = false },
            title = { Text("Configuración del Perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempNombre,
                        onValueChange = { tempNombre = it },
                        label = { Text("Nombre del Doctor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempConsultorio,
                        onValueChange = { tempConsultorio = it },
                        label = { Text("Nombre del Consultorio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN PARA EL LOGO
                    Button(
                        onClick = { launcherLogo.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Seleccionar Logo PDF")
                    }

                    if (!prefs.getString("logo_path", "").isNullOrEmpty()) {
                        Text("Logo cargado correctamente ✅", fontSize = 10.sp, color = Color(0xFF22C55E), modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Guardamos en la preferencia general
                    prefs.edit().apply {
                        putString("nombre_doc", tempNombre)
                        putString("nombre_cons", tempConsultorio)
                        apply()
                    }
                    // Sincronizamos con las llaves exactas que pide el PDF
                    context.getSharedPreferences("config_odontograma", Context.MODE_PRIVATE).edit().apply {
                        putString("nombre_doctor", tempNombre)
                        putString("nombre_consultorio", tempConsultorio)
                        apply()
                    }
                    mostrarDialogoConfig = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfig = false }) { Text("Cancelar") }
            }
        )
    }
    // ViewModels persistentes para la navegación
    val pacienteViewModel: PacienteViewModel = viewModel()
    val citaViewModel: CitaViewModel = viewModel()

    val pacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())
    val citas by citaViewModel.obtenerCitasLocales().collectAsState(initial = emptyList())

    // --- MAIN LAYOUT GLOBAL ---
    MainLayout(
        navController = navController,
        onConfigClick = { mostrarDialogoConfig = true }
    ) { paddingValues ->
        // AQUÍ EMPIEZA EL NAVHOST
        NavHost(
            navController = navController,
            startDestination = AppNavigation.DASHBOARD,
            modifier = Modifier.padding(paddingValues)
        ) { // <--- ESTA LLAVE ABRE EL CONTENIDO DEL NAVHOST

            composable(AppNavigation.DASHBOARD) {
                val citaVM: CitaViewModel = hiltViewModel()
                val pacienteVM: PacienteViewModel = hiltViewModel()
                PantallaDashboard(
                    navController = navController,
                    citaViewModel = citaVM,
                    pacienteViewModel = pacienteVM
                )
            }

            composable(AppNavigation.INICIO) {
                val notaViewModel: NotaViewModel = hiltViewModel()
                PantallaInicio(
                    navController = navController,
                    citaViewModel = citaViewModel,
                    pacienteViewModel = pacienteViewModel,
                    notaViewModel = notaViewModel
                )
            }

            composable(AppNavigation.REGISTRO) {
                val registroViewModel: PacienteViewModel = hiltViewModel()
                PantallaRegistro(navController = navController, viewModel = registroViewModel)
            }

            composable(AppNavigation.PACIENTES) {
                PantallaPacientes(
                    pacientes = pacientes,
                    navController = navController,
                    viewModel = pacienteViewModel,
                    citaViewModel = citaViewModel
                )
            }

            composable(
                route = AppNavigation.CITAS,
                deepLinks = listOf(navDeepLink { uriPattern = "app://consultorio.com/citas" })
            ) {
                PantallaCitas(
                    navController = navController,
                    citaViewModel = citaViewModel,
                    pacienteViewModel = pacienteViewModel
                )
            }

            composable(AppNavigation.PAGOS) {
                PantallaPagos(pacientes = pacientes, viewModel = pacienteViewModel)
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

            composable(
                route = AppNavigation.DETALLE_CITA,
                deepLinks = listOf(navDeepLink { uriPattern = "app://consultorio.com/detalle/{id}" })
            ) { backStackEntry ->
                val citaId = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                val cita = citas.firstOrNull { it.id == citaId }
                if (cita != null) {
                    PantallaDetalleCita(
                        cita = cita,
                        viewModel = citaViewModel,
                        pacienteViewModel = pacienteViewModel,
                        // Reemplazamos navController por esta función lambda
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(AppNavigation.GESTION_CONSULTORIO) {
                val notaViewModel: NotaViewModel = hiltViewModel()
                PantallaGestionConsultorio(viewModel = notaViewModel)
            }

            composable(AppNavigation.PRESUPUESTO) {
                val presupuestoViewModel: PresupuestoViewModel = hiltViewModel()
                PantallaPresupuesto(viewModel = presupuestoViewModel,pacienteViewModel = pacienteViewModel)
            }

            composable(AppNavigation.FINANZAS) {
                val finanzasViewModel: FinanzasViewModel = hiltViewModel()
                PantallaFinanzas(viewModel = finanzasViewModel)
            }

            composable(AppNavigation.LABORATORIOS) {
                val labViewModel: LaboratorioViewModel = hiltViewModel()
                PantallaLaboratorios(viewModel = labViewModel)
            }

            composable(AppNavigation.INVENTARIO) {
                val inventarioViewModel: InventarioViewModel = hiltViewModel()
                PantallaInventarioPrincipal(viewModel = inventarioViewModel)
            }

        } // <--- ESTA LLAVE CIERRA EL NAVHOST CORRECTAMENTE
    }
}