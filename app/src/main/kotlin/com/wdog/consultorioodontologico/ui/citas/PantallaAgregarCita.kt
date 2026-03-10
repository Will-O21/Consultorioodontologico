package com.wdog.consultorioodontologico.ui.citas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.compose.AsyncImage
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
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAgregarCita(
    navController: NavController,
    citaViewModel: CitaViewModel,
    pacienteViewModel: PacienteViewModel,
    presupuestoViewModel: com.wdog.consultorioodontologico.viewmodels.PresupuestoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    // --- ESTADOS ---
    var fechaSeleccionada by remember { mutableStateOf(LocalDateTime.now()) }
    val pacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())
    var pacienteSeleccionado by remember { mutableStateOf<Paciente?>(null) }
    val catalogoServicios by presupuestoViewModel.listaServiciosBase.collectAsState(initial = emptyList())
    var servicioSeleccionado by remember { mutableStateOf("") }
    var expandedServicios by remember { mutableStateOf(false) }
    var comentarios by remember { mutableStateOf("") }
    var textoBusqueda by remember { mutableStateOf("") }
    val scrollState = rememberScrollState() // <--- AGREGAR ESTA LÍNEA
    val calendarState = remember { UseCaseState() }
    val clockState = remember { UseCaseState() }
    val context = LocalContext.current

    // Formateadores
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | hh:mm a")

    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorFondo = Color(0xFFF8F9FA)

    // --- DIÁLOGOS (LÓGICA) ---
    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            monthSelection = true,
            // Restringimos: Desde hoy hasta 2 años en el futuro
            boundary = java.time.LocalDate.now()..java.time.LocalDate.now().plusYears(2)
        ),
        selection = CalendarSelection.Date { newDate ->
            fechaSeleccionada = newDate.atTime(fechaSeleccionada.toLocalTime())
        }
    )
    ClockDialog(
        state = clockState,
        selection = ClockSelection.HoursMinutes { horas, minutos ->
            fechaSeleccionada = fechaSeleccionada.toLocalDate().atTime(horas, minutos)
        }
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(colorAzulOscuro)
                    .padding(top = 0.dp, bottom = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Agendar Cita", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondo)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // --- 1. SELECCIÓN DE FECHA Y HORA (FILA MODERNA) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BotonSeleccionRapida(
                    label = "Fecha",
                    valor = fechaSeleccionada.format(dateFormatter),
                    icono = Icons.Default.CalendarMonth,
                    color = colorAzulBotones,
                    modifier = Modifier.weight(1f),
                    onClick = { calendarState.show() }
                )
                BotonSeleccionRapida(
                    label = "Hora",
                    valor = fechaSeleccionada.format(timeFormatter),
                    icono = Icons.Default.Schedule,
                    color = colorAzulBotones,
                    modifier = Modifier.weight(1f),
                    onClick = { clockState.show() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- 2. BÚSQUEDA DE PACIENTE ---
            Text("Seleccionar Paciente", fontWeight = FontWeight.Bold, color = colorAzulOscuro)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar paciente...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorAzulBotones) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // --- 3. LISTA DE PACIENTES (CARDS CON FOTO) ---
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filtrados = pacientes.filter { it.nombre.contains(textoBusqueda, true) }.sortedBy { it.nombre }
                items(filtrados) { paciente ->
                    ItemSeleccionPaciente(
                        paciente = paciente,
                        estaSeleccionado = pacienteSeleccionado == paciente,
                        colorAzul = colorAzulBotones,
                        onSelect = { pacienteSeleccionado = if (pacienteSeleccionado == paciente) null else paciente }
                    )
                }
            }

            // --- BLOQUE ANIMADO Y RESALTADO ---
            AnimatedVisibility(
                visible = pacienteSeleccionado != null,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Detalles de la Cita",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorAzulOscuro
                        )

                        // --- PROCEDIMIENTO ---
                        Column {
                            Text("Procedimiento", fontWeight = FontWeight.Bold, color = colorAzulOscuro, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))

                            if (catalogoServicios.isEmpty()) {
                                Surface(
                                    color = Color.Yellow.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = "⚠️ Cargue Procedimiento en el Catálogo del Presupuesto",
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 12.sp,
                                        color = Color(0xFF856404),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = expandedServicios,
                                onExpandedChange = { expandedServicios = !expandedServicios }
                            ) {
                                OutlinedTextField(
                                    value = servicioSeleccionado,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Seleccione un procedimiento...") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedServicios) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(
                                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                        enabled = true
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = colorFondo,
                                        unfocusedContainerColor = colorFondo
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedServicios,
                                    onDismissRequest = { expandedServicios = false }
                                ) {
                                    catalogoServicios.forEach { servicio ->
                                        DropdownMenuItem(
                                            text = { Text(servicio.nombreServicio) },
                                            onClick = {
                                                servicioSeleccionado = servicio.nombreServicio
                                                expandedServicios = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // --- COMENTARIOS ---
                        OutlinedTextField(
                            value = comentarios,
                            onValueChange = { comentarios = it },
                            label = { Text("Comentarios / Observaciones") },
                            modifier = Modifier.fillMaxWidth().height(90.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorFondo,
                                unfocusedContainerColor = colorFondo
                            )
                        )

                        // --- BOTÓN GUARDAR ---
                        Button(
                            onClick = {
                                val cita = Cita(
                                    pacienteId = pacienteSeleccionado!!.id,
                                    pacienteNombre = pacienteSeleccionado!!.nombre,
                                    fechaHora = fechaSeleccionada,
                                    observaciones = comentarios,
                                    procedimiento = servicioSeleccionado
                                )
                                citaViewModel.insertarCita(cita)
                                programarNotificacion(cita, context, dateTimeFormatter)
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorAzulBotones),
                            enabled = servicioSeleccionado.isNotEmpty()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar y Guardar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
// --- COMPONENTES AUXILIARES ---

@Composable
fun BotonSeleccionRapida(label: String, valor: String, icono: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun ItemSeleccionPaciente(
    paciente: Paciente,
    estaSeleccionado: Boolean,
    colorAzul: Color,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            // Usamos un clickable totalmente limpio sin efectos visuales
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSelect()
            },
        shape = RoundedCornerShape(16.dp),
        // Mantenemos el fondo siempre blanco para evitar el "remarcado"
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- AVATAR ---
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(colorAzul.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!paciente.fotoPerfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = File(paciente.fotoPerfil!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colorAzul
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = paciente.nombre,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                color = Color(0xFF333333) // Un gris muy oscuro para elegancia
            )

            // --- EL BOTÓN (ÚNICO INDICADOR) ---
            RadioButton(
                selected = estaSeleccionado,
                onClick = null, // null aquí porque el clic lo maneja la tarjeta completa
                colors = RadioButtonDefaults.colors(
                    selectedColor = colorAzul,
                    unselectedColor = Color.LightGray
                )
            )
        }
    }
}
private fun programarNotificacion(cita: Cita, context: Context, formatter: DateTimeFormatter) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Crear canal de notificación
    val channel = NotificationChannel(
        "citas_channel",
        "Recordatorios de Citas",
        NotificationManager.IMPORTANCE_HIGH
    )
    notificationManager.createNotificationChannel(channel)

    // Calcular el tiempo hasta la cita (en segundos)
    val tiempoHastaCita = cita.fechaHora.toEpochSecond(java.time.ZoneOffset.UTC) -
            LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC)

    // Solo programar si la cita es en el futuro
    if (tiempoHastaCita > 0) {
        val workRequest = OneTimeWorkRequestBuilder<NotificacionWorker>()
            .setInitialDelay(tiempoHastaCita, TimeUnit.SECONDS)
            .setInputData(
                workDataOf(
                    "titulo" to "Recordatorio de Cita",
                    "mensaje" to "Cita con ${cita.pacienteNombre} el ${cita.fechaHora.format(formatter)}"
                )
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
