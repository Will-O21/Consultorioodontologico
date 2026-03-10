package com.wdog.consultorioodontologico.ui.pacientes

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wdog.consultorioodontologico.R
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.navigation.AppNavigation
import com.wdog.consultorioodontologico.ui.components.EstadoVacioConsultorio
import com.wdog.consultorioodontologico.ui.pagos.DialogEditarPago
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPacientes(
    pacientes: List<Paciente>,
    navController: NavController,
    viewModel: PacienteViewModel,
    citaViewModel: CitaViewModel
) {
    val context = LocalContext.current

    // Lógica de WhatsApp
    val abrirWhatsApp = { telefono: String ->
        val numeroLimpio = if (telefono.startsWith("0")) telefono.substring(1) else telefono
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://wa.me/58$numeroLimpio".toUri()
        }
        context.startActivity(intent)
    }

    var busqueda by remember { mutableStateOf("") }
    var filtroPago by remember { mutableStateOf("Todos") }
    var pacienteAEliminar by remember { mutableStateOf<Paciente?>(null) }

    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorRojoSeleccion = Color(0xFF530E0E)
    val colorFondo = Color(0xFFF8F9FA)

    // --- MODO SELECCIÓN ---
    var seleccionados by remember { mutableStateOf(setOf<Long>()) }
    val modoSeleccionActivo = seleccionados.isNotEmpty()
    var mostrarDialogoBorrarVarios by remember { mutableStateOf(false) }

    BackHandler(enabled = modoSeleccionActivo) {
        seleccionados = emptySet()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(if (modoSeleccionActivo) colorRojoSeleccion else colorAzulOscuro)
                    .padding(top = 48.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (modoSeleccionActivo) "${seleccionados.size} Seleccionados" else "Mis Pacientes",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (modoSeleccionActivo) "Toca para sumar o 'Atrás' para cancelar" else "${pacientes.size} registros encontrados",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (modoSeleccionActivo) {
                        mostrarDialogoBorrarVarios = true
                    } else {
                        navController.navigate(AppNavigation.REGISTRO)
                    }
                },
                containerColor = if (modoSeleccionActivo) colorRojoSeleccion else colorAzulBotones,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 12.dp).size(56.dp)
            ) {
                Icon(
                    imageVector = if (modoSeleccionActivo) Icons.Default.Delete else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(if (modoSeleccionActivo) 24.dp else 28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondo)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Nombre o motivo de consulta...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorAzulBotones) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = colorAzulBotones,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = colorAzulBotones
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de Filtros
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val opciones = listOf("Todos", "Pendientes", "Abonos", "Al día")
                opciones.forEach { opcion ->
                    val estaSeleccionadoChip = filtroPago == opcion
                    FilterChip(
                        selected = estaSeleccionadoChip,
                        onClick = { filtroPago = opcion },
                        label = { Text(opcion) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorAzulBotones,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Lista Filtrada
            val pacientesFiltrados = pacientes.filter { paciente ->
                val coincideBusqueda = paciente.nombre.contains(busqueda, ignoreCase = true) ||
                        paciente.motivoConsulta.contains(busqueda, ignoreCase = true)

                val coincideFiltro = when (filtroPago) {
                    "Pendientes" -> paciente.estadoPago == "Pendiente"
                    "Abonos" -> paciente.estadoPago == "Abonó"
                    "Al día" -> paciente.estadoPago == "Al día" || paciente.estadoPago == "Completo"
                    else -> true
                }
                coincideBusqueda && coincideFiltro
            }

            if (pacientesFiltrados.isEmpty()) {
                EstadoVacioConsultorio(
                    icono = Icons.Default.People,
                    mensaje = if (busqueda.isEmpty()) "No hay pacientes registrados" else "No se encontraron coincidencias",
                    colorPersonalizado = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pacientesFiltrados.sortedBy { it.nombre }, key = { it.id }) { paciente ->
                        val estaSeleccionado = seleccionados.contains(paciente.id)

                        ItemPacienteCard(
                            paciente = paciente,
                            colorAzul = colorAzulBotones,
                            estaSeleccionado = estaSeleccionado,
                            citaViewModel = citaViewModel,
                            pacienteViewModel = viewModel,
                            onWhatsAppClick = { abrirWhatsApp(paciente.telefono) },
                            onActualizarPago = { pActualizado ->
                                viewModel.actualizarPaciente(
                                    paciente = pActualizado,
                                    afecciones = emptyList(),
                                    nuevaFotoPerfil = null,
                                    nuevasPlacas = emptyList(),
                                    borrarPerfilAnterior = false,
                                    placasABorrar = emptyList()
                                )
                            },
                            onClickCard = {
                                if (modoSeleccionActivo) {
                                    seleccionados = if (estaSeleccionado) seleccionados - paciente.id else seleccionados + paciente.id
                                } else {
                                    navController.navigate("editar_paciente/${paciente.id}")
                                }
                            },
                            onLongClick = {
                                if (!modoSeleccionActivo) {
                                    seleccionados = seleccionados + paciente.id
                                }
                            }
                        )
                    }
                }
            }

            // Diálogos de eliminación
            if (pacienteAEliminar != null) {
                AlertDialog(
                    onDismissRequest = { pacienteAEliminar = null },
                    title = { Text("Eliminar Expediente", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Seguro que deseas eliminar a ${pacienteAEliminar?.nombre}?\n\nSe perderán sus datos permanentemente.") },
                    confirmButton = {
                        TextButton(onClick = {
                            pacienteAEliminar?.let { viewModel.eliminarPaciente(it) }
                            pacienteAEliminar = null
                        }) { Text("BORRAR TODO", color = Color.Red, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { pacienteAEliminar = null }) { Text("CANCELAR", color = Color.Black) }
                    }
                )
            }

            if (mostrarDialogoBorrarVarios) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoBorrarVarios = false },
                    title = { Text("Eliminar Seleccionados", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Deseas eliminar los ${seleccionados.size} expedientes seleccionados?\n\nEsta acción es permanente.") },
                    confirmButton = {
                        TextButton(onClick = {
                            seleccionados.forEach { id ->
                                pacientes.find { it.id == id }?.let { viewModel.eliminarPaciente(it) }
                            }
                            seleccionados = emptySet()
                            mostrarDialogoBorrarVarios = false
                        }) { Text("BORRAR SELECCIONADOS", color = Color.Red, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoBorrarVarios = false }) { Text("CANCELAR", color = Color.Black) }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemPacienteCard(
    paciente: Paciente,
    colorAzul: Color,
    estaSeleccionado: Boolean,
    citaViewModel: CitaViewModel,
    pacienteViewModel: PacienteViewModel,
    onWhatsAppClick: () -> Unit,
    onActualizarPago: (Paciente) -> Unit,
    onClickCard: () -> Unit,
    onLongClick: () -> Unit
) {
    var isEditingPago by remember { mutableStateOf(false) }
    val todasLasCitas by citaViewModel.obtenerCitasLocales().collectAsState(initial = emptyList())
    val proximaCita = remember(todasLasCitas, paciente.id) {
        todasLasCitas
            .filter { it.pacienteId == paciente.id && it.fechaHora.isAfter(java.time.LocalDateTime.now()) }
            .minByOrNull { it.fechaHora }
    }

    val colorRojoVino = Color(0xFF530E0E)
    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .border(
                width = if (estaSeleccionado) 3.dp else 0.dp,
                color = if (estaSeleccionado) colorRojoVino else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = onClickCard,
                onLongClick = onLongClick,
                indication = ripple(), // Corrección obligatoria para evitar el cierre de la app
                interactionSource = interactionSource
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(if (estaSeleccionado) 0.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(colorAzul.copy(alpha = 0.1f)),
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
                    Icon(Icons.Default.Person, contentDescription = null, tint = colorAzul)
                }

                if (estaSeleccionado) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(colorRojoVino.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paciente.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black // Se mantiene negro para limpieza visual
                )

                Text(
                    text = paciente.motivoConsulta,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                proximaCita?.let { cita ->
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM - hh:mm a")
                    Text(
                        text = "Próxima: ${cita.fechaHora.format(formatter)}",
                        fontSize = 11.sp,
                        color = colorAzul,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!estaSeleccionado) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (paciente.telefono.isNotEmpty()) {
                        // Contenedor circular sutil para que el ícono no parezca "flotando"
                        Surface(
                            onClick = onWhatsAppClick,
                            shape = CircleShape,
                            color = colorAzul.copy(alpha = 0.05f), // Fondo casi invisible pero que da estructura
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp",
                                    tint = Color.Unspecified, // Mantiene sus colores originales (verde/blanco)
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val (btnColor, btnText) = when (paciente.estadoPago) {
                            "Pendiente" -> Color(0xFFAD1D1D) to "Pendiente"
                            "Abonó" -> Color(0xFF0D47A1) to "Abonó"
                            else -> Color(0xFF155E29) to "Al día"
                        }

                        Button(
                            onClick = { isEditingPago = true },
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(btnText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        if (paciente.estadoPago != "Al día" && paciente.estadoPago != "Completo") {
                            Text(
                                text = "$${(paciente.monto - paciente.abono).toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = btnColor
                            )
                        }
                    }
                }
            }
        }
    }

    if (isEditingPago) {
        DialogEditarPago(
            paciente = paciente,
            viewModel = pacienteViewModel,
            onDismiss = { isEditingPago = false },
            onConfirm = { pacienteActualizado ->
                onActualizarPago(pacienteActualizado)
                isEditingPago = false
            }
        )
    }
}