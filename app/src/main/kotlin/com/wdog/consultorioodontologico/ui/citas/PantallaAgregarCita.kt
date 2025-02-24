package com.wdog.consultorioodontologico.ui.citas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarCita(
    navController: NavController,
    citaViewModel: CitaViewModel,
    pacienteViewModel: PacienteViewModel // Usar el pacienteViewModel pasado como parámetro
) {
    var fechaSeleccionada by remember { mutableStateOf(LocalDateTime.now()) }
    val pacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())
    var pacienteSeleccionado by remember { mutableStateOf<Paciente?>(null) }
    var observaciones by remember { mutableStateOf("") }

    // Estados personalizados para los diálogos
    val calendarState = remember { UseCaseState() } // Verificar nombre real de la clase
    val clockState = remember { UseCaseState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Botones para abrir diálogos
        Button(
            onClick = { calendarState.show() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Seleccionar Fecha: ${fechaSeleccionada.toLocalDate()}")
        }

        Button(
            onClick = { clockState.show() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Seleccionar Hora: ${fechaSeleccionada.toLocalTime()}")
        }

        // Diálogo de Fecha
            CalendarDialog(
                state = calendarState,
                config = CalendarConfig(monthSelection = true),
                selection = CalendarSelection.Date { newDate ->
                    fechaSeleccionada = newDate.atTime(fechaSeleccionada.toLocalTime())
                    calendarState.hide()
                }
            )


        // Diálogo de Hora
            ClockDialog(
                state = clockState,
                selection = ClockSelection.HoursMinutes { horas, minutos ->
                    fechaSeleccionada = fechaSeleccionada.toLocalDate().atTime(horas, minutos)
                    clockState.hide()
                }
            )


        // Lista de Pacientes
        Text(
            text = "Seleccionar Paciente:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .height(200.dp)
                .padding(vertical = 8.dp)
        ) {
            items(pacientes) { paciente ->
                ListItem(
                    headlineContent = { Text("${paciente.nombre} ${paciente.apellido}") },
                    modifier = Modifier
                        .clickable { pacienteSeleccionado = paciente }
                        .padding(8.dp)
                )
            }
        }

        // Campo de Observaciones
        TextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(vertical = 8.dp)
        )

        // Botón Guardar
        Button(
            onClick = {
                if (pacienteSeleccionado == null) return@Button
                pacienteSeleccionado?.let {
                    citaViewModel.insertarCita(
                        Cita(
                            pacienteId = it.id.toLong(),
                            fechaHora = fechaSeleccionada,
                            observaciones = observaciones
                        )
                    )
                    navController.popBackStack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Guardar Cita")
        }
    }
}