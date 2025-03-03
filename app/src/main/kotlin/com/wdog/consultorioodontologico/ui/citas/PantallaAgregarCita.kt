package com.wdog.consultorioodontologico.ui.citas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
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
import com.wdog.consultorioodontologico.workers.NotificacionWorker
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarCita(
    navController: NavController,
    citaViewModel: CitaViewModel,
    pacienteViewModel: PacienteViewModel
) {
    var fechaSeleccionada by remember { mutableStateOf(LocalDateTime.now()) }
    val pacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())
    var pacienteSeleccionado by remember { mutableStateOf<Paciente?>(null) }
    var observaciones by remember { mutableStateOf("") }
    var mostrarBusqueda by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }

    // Formateadores
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // Solo fecha
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a") // Solo hora
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | hh:mm a") // Fecha y hora

    // Estados para errores de validación
    var errorFecha by remember { mutableStateOf(false) }
    var errorPaciente by remember { mutableStateOf(false) }
    var errorObservaciones by remember { mutableStateOf(false) }

    // Estados personalizados para los diálogos
    val calendarState = remember { UseCaseState() }
    val clockState = remember { UseCaseState() }

    // Contexto
    val context = LocalContext.current

    val colorAzulTitulo = Color(0xFF101084) // Color azul para la barra de título
    val colorAzulBotones = Color(0xFF094293) // Color azul para los botones

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Barra de título superior
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho
                    contentAlignment = Alignment.Center // Centra el texto horizontalmente
                ) {
                    Text(
                        text = "Agendar Cita",
                        color = Color.White, // Texto en color blanco
                        fontWeight = FontWeight.Bold // Texto en negritas
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorAzulTitulo, // Color de fondo azul #101084
                titleContentColor = Color.White // Texto en color blanco
            ),
            modifier = Modifier.fillMaxWidth() // Asegura que el TopAppBar ocupe todo el ancho
        )

        // Botón "Atrás" debajo de la barra de título
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorAzulBotones // Usamos el color #094293
            )
        ) {
            Text("Atrás", color = Color.White)
        }

        // Botón para seleccionar fecha
        Button(
            onClick = { calendarState.show() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorAzulBotones // Usamos el color #094293
            )
        ) {
            Text("Seleccionar  Fecha:  ${fechaSeleccionada.format(dateFormatter)}", color = Color.White) // Solo fecha
        }
        if (errorFecha) {
            Text("La fecha no puede ser en el pasado", color = Color.Red)
        }

        // Botón para seleccionar hora
        Button(
            onClick = { clockState.show() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorAzulBotones // Usamos el color #094293
            )
        ) {
            Text("Seleccionar  Hora:  ${fechaSeleccionada.format(timeFormatter)}", color = Color.White) // Solo hora
        }

        // Diálogo de Fecha
        CalendarDialog(
            state = calendarState,
            config = CalendarConfig(monthSelection = true),
            selection = CalendarSelection.Date { newDate ->
                fechaSeleccionada = newDate.atTime(fechaSeleccionada.toLocalTime())
                errorFecha = fechaSeleccionada.isBefore(LocalDateTime.now()) // Validación de fecha
                calendarState.hide()
            }
        )

        // Diálogo de Hora
        ClockDialog(
            state = clockState,
            selection = ClockSelection.HoursMinutes { horas, minutos ->
                fechaSeleccionada = fechaSeleccionada.toLocalDate().atTime(horas, minutos)
                errorFecha = fechaSeleccionada.isBefore(LocalDateTime.now()) // Validación de fecha
                clockState.hide()
            }
        )

        // Botón de lupa para activar la búsqueda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Seleccionar Paciente:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { mostrarBusqueda = !mostrarBusqueda },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar paciente",
                    tint = colorAzulBotones
                )
            }
        }

        // Casilla de búsqueda (solo visible si mostrarBusqueda es true)
        if (mostrarBusqueda) {
            TextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar paciente por nombre", color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorAzulBotones.copy(alpha = 0.1f),
                    unfocusedContainerColor = colorAzulBotones.copy(alpha = 0.1f),
                    focusedIndicatorColor = colorAzulBotones,
                    unfocusedIndicatorColor = colorAzulBotones,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { mostrarBusqueda = false })
            )
        }

        // Lista de Pacientes (con scroll)
        LazyColumn(
            modifier = Modifier
                .height(200.dp)
                .padding(vertical = 8.dp)
        ) {
            val pacientesFiltrados = pacientes
                .filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
                .sortedBy { it.nombre }

            items(pacientesFiltrados) { paciente ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pacienteSeleccionado = if (pacienteSeleccionado == paciente) null else paciente
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${paciente.nombre} ${paciente.apellido}",
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = pacienteSeleccionado == paciente,
                        onCheckedChange = { isChecked ->
                            pacienteSeleccionado = if (isChecked) paciente else null
                        }
                    )
                }
            }
        }
        if (errorPaciente) {
            Text("Debe seleccionar un paciente", color = Color.Red)
        }

        // Campo de Observaciones
        TextField(
            value = observaciones,
            onValueChange = {
                observaciones = it
                errorObservaciones = it.length > 500 // Validación de longitud
            },
            label = { Text("Observaciones", color = Color.Black) }, // Texto del label en color negro
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(vertical = 8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorAzulBotones.copy(alpha = 0.1f), // Fondo cuando está enfocado
                unfocusedContainerColor = colorAzulBotones.copy(alpha = 0.1f), // Fondo cuando no está enfocado
                focusedIndicatorColor = colorAzulBotones, // Color del indicador cuando está enfocado
                unfocusedIndicatorColor = colorAzulBotones, // Color del indicador cuando no está enfocado
                focusedLabelColor = Color.Black, // Color del label cuando está enfocado
                unfocusedLabelColor = Color.Black // Color del label cuando no está enfocado
            ),
            isError = errorObservaciones
        )
        if (errorObservaciones) {
            Text("Las observaciones no pueden exceder los 500 caracteres", color = Color.Red)
        }

        // Botón Guardar
        Button(
            onClick = {
                errorFecha = fechaSeleccionada.isBefore(LocalDateTime.now())
                errorPaciente = pacienteSeleccionado == null
                errorObservaciones = observaciones.length > 500

                if (!errorFecha && !errorPaciente && !errorObservaciones) {
                    pacienteSeleccionado?.let { paciente ->
                        val cita = Cita(
                            pacienteId = paciente.id,
                            pacienteNombre = "${paciente.nombre} ${paciente.apellido}",
                            fechaHora = fechaSeleccionada,
                            observaciones = observaciones
                        )
                        citaViewModel.insertarCita(cita)

                        // Programar notificación (pasando dateTimeFormatter como parámetro)
                        programarNotificacion(cita, context, dateTimeFormatter)

                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorAzulBotones // Usamos el color #094293
            )
        ) {
            Text("Guardar Cita", color = Color.White)
        }
    }
}

// Función para programar una notificación
// Función para programar una notificación
private fun programarNotificacion(cita: Cita, context: Context, formatter: DateTimeFormatter) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Crear canal de notificación
    val channel = NotificationChannel(
        "citas_channel",
        "Recordatorios de Citas",
        NotificationManager.IMPORTANCE_HIGH
    )
    notificationManager.createNotificationChannel(channel)

    // Calcular el tiempo hasta la cita
    val tiempoHastaCita = cita.fechaHora.toEpochSecond(java.time.ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC)

    // Crear la solicitud de trabajo
    val workRequest = OneTimeWorkRequestBuilder<NotificacionWorker>()
        .setInitialDelay(tiempoHastaCita, TimeUnit.SECONDS)
        .setInputData(
            workDataOf(
                "titulo" to "Recordatorio de Cita",
                "mensaje" to "Tienes una cita programada para ${cita.fechaHora.format(formatter)}" // Usar el formateador pasado como parámetro
            )
        )
        .build()

    // Programar la notificación
    WorkManager.getInstance(context).enqueue(workRequest)
}