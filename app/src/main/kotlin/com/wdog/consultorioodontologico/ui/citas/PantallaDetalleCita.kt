package com.wdog.consultorioodontologico.ui.citas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleCita(
    cita: Cita,
    viewModel: CitaViewModel,
    onBack: () -> Unit,
    pacienteViewModel: PacienteViewModel,
    presupuestoViewModel: com.wdog.consultorioodontologico.viewmodels.PresupuestoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | hh:mm a")
    var observaciones by remember { mutableStateOf(cita.observaciones) }
    var fechaSeleccionada by remember { mutableStateOf(cita.fechaHora) }
    var showSaveButton by remember { mutableStateOf(false) }

    // --- NUEVOS ESTADOS ---
    var asistio by remember { mutableStateOf(cita.asistio) }
    var servicioSeleccionado by remember { mutableStateOf(cita.procedimiento) }
    var expandedServicios by remember { mutableStateOf(false) }

    // Obtenemos el catálogo del PresupuestoViewModel
    val catalogoServicios by presupuestoViewModel.listaServiciosBase.collectAsState(initial = emptyList())

    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorFondo = Color(0xFFF8F9FA)

    // Observar pacientes para obtener la foto
    val listaPacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())
    val paciente = remember(listaPacientes) { listaPacientes.find { it.id == cita.pacienteId } }

    val calendarState = remember { UseCaseState() }
    val clockState = remember { UseCaseState() }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(colorAzulOscuro)
                    .padding(top = 0.dp, bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto de Perfil circular
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val ruta = paciente?.fotoPerfil // Obtenemos la ruta una sola vez

                    if (!ruta.isNullOrEmpty()) {
                        AsyncImage(
                            model = ruta, // Aquí ya no necesitas el ? porque 'ruta' es el String
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colorAzulOscuro,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = paciente?.nombre ?: cita.pacienteNombre,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = " ", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondo)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Fila de Fecha
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = colorAzulBotones)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Fecha y Hora Programada", color = Color.Gray, fontSize = 12.sp)
                            Text(text = fechaSeleccionada.format(formatter), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
// 1. MENÚ DESPLEGABLE DE SERVICIOS
                    Text("Procedimiento", fontWeight = FontWeight.Bold, color = colorAzulOscuro)

                    if (catalogoServicios.isEmpty()) {
                        Surface(
                            color = Color.Yellow.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "⚠️ Cargue Servicio en Pestaña Catálogo en la Calculadora",
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
                            placeholder = { Text("Seleccione procedimiento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedServicios) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorAzulBotones,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        if (catalogoServicios.isNotEmpty()) {
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
                                            showSaveButton = true
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }

                    // 2. SECCIÓN DE OBSERVACIONES
                    Text("Observaciones / Detalles", fontWeight = FontWeight.Bold, color = colorAzulOscuro)
                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = {
                            observaciones = it
                            showSaveButton = true
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorAzulBotones,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                    // 3. REGISTRO DE ASISTENCIA (Con lógica de desmarcar)
                    Text("¿Asistió a la cita?", fontWeight = FontWeight.Bold, color = colorAzulOscuro)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = asistio == true,
                            onClick = {
                                // Si ya era 'true', lo ponemos en 'null' (desmarcar). Si no, lo ponemos 'true'.
                                asistio = if (asistio == true) null else true
                                showSaveButton = true
                            },
                            label = { Text("Asistió") },
                            leadingIcon = { if (asistio == true) Icon(Icons.Default.Check, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = asistio == false,
                            onClick = {
                                // Si ya era 'false', lo ponemos en 'null' (desmarcar). Si no, lo ponemos 'false'.
                                asistio = if (asistio == false) null else false
                                showSaveButton = true
                            },
                            label = { Text("No Asistió") },
                            leadingIcon = { if (asistio == false) Icon(Icons.Default.Close, null) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFC62828),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(0.dp))

            // BOTONES DE ACCIÓN
            if (showSaveButton) {
                Button(
                    onClick = {
                        viewModel.actualizarCita(cita.copy(
                            fechaHora = fechaSeleccionada,
                            observaciones = observaciones,
                            procedimiento = servicioSeleccionado, // Guardamos el servicio
                            asistio = asistio // Guardamos si asistió
                        ))
                        showSaveButton = false
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GUARDAR CAMBIOS", fontWeight = FontWeight.ExtraBold)
                }
            }

            Button(
                onClick = { calendarState.show() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulBotones)
            ) {
                Icon(Icons.Default.EditCalendar, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("MODIFICAR FECHA/HORA", fontWeight = FontWeight.ExtraBold)
            }
        }
    }

    // Diálogos de Selección
    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            monthSelection = true,
            boundary = java.time.LocalDate.now()..java.time.LocalDate.now().plusYears(2) // RESTRICCIÓN: Solo hoy y futuro
        ),
        selection = CalendarSelection.Date { newDate ->
            fechaSeleccionada = newDate.atTime(fechaSeleccionada.toLocalTime())
            clockState.show()
        }
    )

    ClockDialog(
        state = clockState,
        selection = ClockSelection.HoursMinutes { horas, minutos ->
            fechaSeleccionada = fechaSeleccionada.toLocalDate().atTime(horas, minutos)
            showSaveButton = true
        }
    )
}