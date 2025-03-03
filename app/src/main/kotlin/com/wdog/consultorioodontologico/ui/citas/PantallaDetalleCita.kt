package com.wdog.consultorioodontologico.ui.citas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleCita(
    navController: NavController,
    cita: Cita,
    viewModel: CitaViewModel
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | hh:mm a")
    var observaciones by remember { mutableStateOf(cita.observaciones) }
    var fechaSeleccionada by remember { mutableStateOf(cita.fechaHora) }
    var isEditing by remember { mutableStateOf(false) } // Estado para controlar el modo de edición
    var showSaveButton by remember { mutableStateOf(false) } // Estado para mostrar el botón "Guardar Cambios"

    val colorAzulTitulo = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorFondoCard = colorAzulBotones.copy(alpha = 0.1f)

    // Estados para los diálogos de fecha y hora
    val calendarState = remember { UseCaseState() }
    val clockState = remember { UseCaseState() }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Detalles de Cita",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorAzulTitulo
            )
        )

        // Card con los detalles de la cita
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorFondoCard
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Paciente: ${cita.pacienteNombre}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Fecha y Hora: ${fechaSeleccionada.format(formatter)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Observaciones:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                BasicTextField(
                    value = observaciones,
                    onValueChange = {
                        observaciones = it
                        showSaveButton = true // Mostrar el botón "Guardar Cambios" al editar las observaciones
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp,fontWeight = FontWeight.Bold) // Aumentar el tamaño del texto en 3 puntos
                )
            }
        }

        // Botones "Modificar Cita", "Guardar Cambios" y "Eliminar Cita" fuera del Card, en la parte inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showSaveButton) {
                Button(
                    onClick = {
                        // Guardar los cambios cuando se hace clic en "Guardar Cambios"
                        val citaActualizada = cita.copy(
                            fechaHora = fechaSeleccionada,
                            observaciones = observaciones
                        )
                        viewModel.actualizarCita(citaActualizada)
                        showSaveButton = false // Ocultar el botón "Guardar Cambios" después de guardar
                        isEditing = false // Desactivar el modo de edición
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorAzulBotones
                    )
                ) {
                    Text("Guardar Cambios", color = Color.White)
                }
            }

            Button(
                onClick = {
                    isEditing = true // Activar el modo de edición
                    calendarState.show() // Mostrar el diálogo de fecha
                    showSaveButton = true // Mostrar el botón "Guardar Cambios" al editar
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorAzulBotones
                )
            ) {
                Text("Modificar Cita", color = Color.White)
            }

            Button(
                onClick = {
                    viewModel.eliminarCita(cita)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorAzulBotones
                )
            ) {
                Text("Eliminar Cita", color = Color.White)
            }
        }

        // Botón "Atrás" en la esquina inferior derecha
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(56.dp)
                    .background(colorAzulBotones, shape = MaterialTheme.shapes.medium),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás"
                )
            }
        }
    }

    // Diálogo de Fecha
    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(monthSelection = true),
        selection = CalendarSelection.Date { newDate ->
            fechaSeleccionada = newDate.atTime(fechaSeleccionada.toLocalTime())
            clockState.show() // Mostrar el diálogo de hora después de seleccionar la fecha
            calendarState.hide()
        }
    )

    // Diálogo de Hora
    ClockDialog(
        state = clockState,
        selection = ClockSelection.HoursMinutes { horas, minutos ->
            fechaSeleccionada = fechaSeleccionada.toLocalDate().atTime(horas, minutos)
            clockState.hide()
            showSaveButton = true // Mostrar el botón "Guardar Cambios" después de editar la hora
        }
    )
}