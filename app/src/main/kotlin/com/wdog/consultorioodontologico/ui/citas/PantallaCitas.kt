package com.wdog.consultorioodontologico.ui.citas

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.navigation.AppNavigation
import com.wdog.consultorioodontologico.ui.components.EstadoVacioConsultorio
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaCitas(
    navController: NavController,
    citaViewModel: CitaViewModel,
    pacienteViewModel: PacienteViewModel
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | hh:mm a")
    var busqueda by remember { mutableStateOf("") }

    // --- ESTADOS DE NAVEGACIÓN Y FILTROS ---
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    var filtroCitas by remember { mutableStateOf("Hoy") }
    val opcionesFiltro = listOf("Todas", "Hoy", "Mañana", "Semana")

    var filtroHistorial by remember { mutableStateOf("Todas") }
    val opcionesHistorial = listOf("Todas", "Asistió", "No Asistió")

    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorFondo = Color(0xFFF8F9FA)
    val colorRojoSeleccion = Color(0xFF530E0E)

    // Corrección: Usamos collectAsState estándar para mayor compatibilidad
    val listaCitas by citaViewModel.obtenerCitasLocales().collectAsState(initial = emptyList())
    val listaPacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())

    var mostrarDialogoCompartir by remember { mutableStateOf(false) }

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
                    .padding(top = 10.dp, bottom = 20.dp), //altura y parte de abajo topbar
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (modoSeleccionActivo) "${seleccionados.size} Seleccionados" else "Agenda de Citas",
                    color = Color.White,
                    fontSize = if (modoSeleccionActivo) 24.sp else 28.sp,
                    fontWeight = FontWeight.Bold
                )
                if (modoSeleccionActivo) {
                    Text(
                        text = "Toca para seleccionar o 'atrás' para cancelar",
                        color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp
                    )
                }
            }
        },
        floatingActionButton = {
            if (modoSeleccionActivo) {
                FloatingActionButton(
                    onClick = { mostrarDialogoBorrarVarios = true },
                    containerColor = colorRojoSeleccion,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 20.dp).size(56.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar Seleccionados")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).padding(bottom = 10.dp), //el ultimo es la altura
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    FloatingActionButton(
                        onClick = { mostrarDialogoCompartir = true },
                        containerColor = Color(0xFF25D366),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(45.dp) //tamaño
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir Agenda")
                    }

                    FloatingActionButton(
                        onClick = { navController.navigate(AppNavigation.AGREGAR_CITA) },
                        containerColor = colorAzulBotones,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(45.dp) //tamaño
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Cita")
                    }
                }
            }
        },
        floatingActionButtonPosition = if (modoSeleccionActivo) FabPosition.End else FabPosition.Center,
    ) { paddingValues ->
        // TODO ESTO DEBE ESTAR DENTRO DE UNA SOLA COLUMN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondo)
                .padding(paddingValues) // Respeta el espacio de la TopBar
        ) {
            // 1. PESTAÑAS (TABS)
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = colorAzulOscuro,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                        color = colorAzulOscuro
                    )
                }
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("CITAS", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("HISTORIAL", fontWeight = FontWeight.Bold) }
                )
            }

            // 2. BUSCADOR Y FILTROS (Ahora dentro del flujo correcto)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Nombre, fecha o servicio...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorAzulBotones) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = colorAzulBotones,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                // Cápsulas de filtro (Píldoras)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val esHistorial = pagerState.currentPage == 1
                    val opciones = if (esHistorial) opcionesHistorial else opcionesFiltro
                    val seleccionado = if (esHistorial) filtroHistorial else filtroCitas

                    opciones.forEach { opcion ->
                        FilterChip(
                            selected = seleccionado == opcion,
                            onClick = { if (esHistorial) filtroHistorial = opcion else filtroCitas = opcion },
                            label = { Text(opcion, fontSize = 12.sp) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorAzulOscuro,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = seleccionado == opcion,
                                borderColor = Color.LightGray,
                                selectedBorderColor = colorAzulOscuro
                            )
                        )
                    }
                }
            }

            // 3. CONTENIDO PAGINADO (LISTA)
            // Usamos weight(1f) para que ocupe el resto de la pantalla sin empujar lo de arriba
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalAlignment = Alignment.Top
            ) { page ->
                val esHistorial = page == 1

                // Lógica de filtrado (Mantenemos tu lógica actual)
                val listaFiltrada = remember(listaCitas, busqueda, filtroCitas, filtroHistorial, esHistorial) {
                    val ahora = java.time.LocalDateTime.now()
                    val hoy = LocalDate.now()
                    val manana = hoy.plusDays(1)
                    val finSemana = hoy.plusDays(7)
                    val formatterBusqueda = DateTimeFormatter.ofPattern("dd/MM/yyyy")

                    listaCitas.filter { cita ->
                        val fechaCitaTexto = cita.fechaHora.format(formatterBusqueda)
                        val coincideBusqueda = cita.pacienteNombre.contains(busqueda, ignoreCase = true) ||
                                cita.observaciones.contains(busqueda, ignoreCase = true) ||
                                cita.procedimiento.contains(busqueda, ignoreCase = true) ||
                                fechaCitaTexto.contains(busqueda)

                        val esPasada = cita.fechaHora.isBefore(ahora)
                        val coincidePestana = if (esHistorial) esPasada else !esPasada

                        val coincideCapsula = if (esHistorial) {
                            when (filtroHistorial) {
                                "Asistió" -> cita.asistio == true
                                "No Asistió" -> cita.asistio == false
                                else -> true
                            }
                        } else {
                            val fechaCita = cita.fechaHora.toLocalDate()
                            when (filtroCitas) {
                                "Hoy" -> fechaCita.isEqual(hoy)
                                "Mañana" -> fechaCita.isEqual(manana)
                                "Semana" -> !fechaCita.isBefore(hoy) && !fechaCita.isAfter(finSemana)
                                else -> true
                            }
                        }
                        coincideBusqueda && coincidePestana && coincideCapsula
                    }.sortedWith(
                        if (esHistorial) compareByDescending { it.fechaHora }
                        else compareBy { it.fechaHora }
                    )
                }

                if (listaFiltrada.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EstadoVacioConsultorio(
                            icono = Icons.Default.DateRange,
                            mensaje = "Sin citas registradas",
                            colorPersonalizado = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listaFiltrada, key = { it.id }) { cita ->
                            val paciente = listaPacientes.find { it.id == cita.pacienteId }
                            ItemCitaCard(
                                cita = cita,
                                pacientes = listaPacientes,
                                pacienteNombre = paciente?.nombre ?: cita.pacienteNombre,
                                fotoPerfil = paciente?.fotoPerfil,
                                fechaFormateada = cita.fechaHora.format(formatter),
                                colorAzul = colorAzulBotones,
                                estaSeleccionado = seleccionados.contains(cita.id),
                                modoSeleccionActivo = modoSeleccionActivo,
                                onClick = {
                                    if (modoSeleccionActivo) {
                                        seleccionados = if (seleccionados.contains(cita.id)) seleccionados - cita.id else seleccionados + cita.id
                                    } else {
                                        navController.navigate("detalle_cita/${cita.id}")
                                    }
                                },
                                onLongClick = { if (!modoSeleccionActivo) seleccionados = seleccionados + cita.id }
                            )
                        }
                    }
                }
            }
        }
    }
    // Diálogos fuera del Scaffold
    if (mostrarDialogoBorrarVarios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrarVarios = false },
            title = { Text("Eliminar Seleccionados") },
            text = { Text("¿Eliminar las ${seleccionados.size} citas?") },
            confirmButton = {
                TextButton(onClick = {
                    listaCitas.filter { seleccionados.contains(it.id) }.forEach { citaViewModel.eliminarCita(it) }
                    seleccionados = emptySet()
                    mostrarDialogoBorrarVarios = false
                }) { Text("ELIMINAR", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoBorrarVarios = false }) { Text("CANCELAR") } }
        )
    }

    if (mostrarDialogoCompartir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCompartir = false },
            title = { Text("Compartir Agenda") },
            text = { Text("¿Deseas incluir los números de contacto?") },
            confirmButton = {
                Button(onClick = {
                    generarYEnviarAgenda(listaCitas, listaPacientes, false, navController.context)
                    mostrarDialogoCompartir = false
                }) { Text("No") }
            },
            dismissButton = {
                Button(onClick = {
                    generarYEnviarAgenda(listaCitas, listaPacientes, true, navController.context)
                    mostrarDialogoCompartir = false
                }) { Text("Sí") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCitaCard(
    cita: Cita,
    pacientes: List<Paciente>,
    pacienteNombre: String,
    fotoPerfil: String?,
    fechaFormateada: String,
    colorAzul: Color,
    estaSeleccionado: Boolean,
    modoSeleccionActivo: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val colorRojoVino = Color(0xFF530E0E)
    val esHoy = cita.fechaHora.toLocalDate() == LocalDate.now()
    val interactionSource = remember { MutableInteractionSource() }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(1f)
            .border(
                width = if (estaSeleccionado) 3.dp else if (esHoy) 2.dp else 1.dp,
                color = if (estaSeleccionado) colorRojoVino else if (esHoy) colorAzul else colorAzul.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = ripple(), // Usamos ripple() de Material3 directamente
                interactionSource = interactionSource
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (estaSeleccionado) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(55.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(colorAzul.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!fotoPerfil.isNullOrEmpty()) {
                        AsyncImage(
                            model = File(fotoPerfil),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colorAzul,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // CÍRCULO DE ASISTENCIA (Solo si ya se marcó en edición)
                if (cita.asistio != null) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (cita.asistio == true) Color(0xFF2E7D32) else Color(0xFFC62828))
                        )
                    }
                }

                if (estaSeleccionado) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(colorRojoVino.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                // El color del texto ahora se mantiene siempre igual (Negro/Azul) como pediste
                Text(text = pacienteNombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(text = fechaFormateada, fontSize = 13.sp, color = colorAzul, fontWeight = FontWeight.Medium)

                // --- NUEVO: PROCEDIMIENTO EN PANTALLA ---
                if (cita.procedimiento.isNotEmpty()) {
                    Text(
                        text = cita.procedimiento,
                        fontSize = 13.sp,
                        color = colorAzul.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (cita.observaciones.isNotEmpty()) {
                    Text(text = cita.observaciones, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }

            if (!modoSeleccionActivo && (esHoy || cita.fechaHora.toLocalDate() == LocalDate.now().plusDays(1))) {
                val pacienteAsociado = pacientes.find { it.id == cita.pacienteId }
                val telefono = pacienteAsociado?.telefono ?: ""

                // 1. DECLARAR LOS FORMATEADORES AQUÍ (Fuera del onClick)
                val formatterDia = DateTimeFormatter.ofPattern("EEEE", java.util.Locale("es", "ES"))
                val formatterFecha = DateTimeFormatter.ofPattern("dd/MM")
                val formatterHora = DateTimeFormatter.ofPattern("hh:mm a")

                if (telefono.isNotEmpty()) {
                    IconButton(onClick = {
                        val telLimpio = telefono.replace(Regex("[^0-9]"), "").let { if (it.startsWith("0")) it.substring(1) else it }

                        // 1. Lógica para determinar si es Hoy o Mañana
                        val fechaCita = cita.fechaHora.toLocalDate()
                        val fechaHoy = java.time.LocalDate.now()
                        val manana = fechaHoy.plusDays(1) // Usamos 'manana' como indicaste

                        val prefijoDia = when (fechaCita) {
                            fechaHoy -> "Hoy"
                            manana -> "Mañana"
                            else -> "" // Por si es otro día, aunque tu filtro solo permite hoy y mañana
                        }

                        val diaSemana = cita.fechaHora.format(formatterDia).replaceFirstChar { it.uppercase() }
                        val fechaMes = cita.fechaHora.format(formatterFecha)
                        val horaCita = cita.fechaHora.format(formatterHora)

                        // 2. Aplicamos el prefijo dinámico en el mensaje
                        val mensajeCita = "¡Hola! Buen día *${pacienteNombre}*!\n\n" +
                                "Te recordamos que tienes una cita odontológica programada para:\n\n" +
                                "*$prefijoDia $diaSemana, $fechaMes*\n" +
                                "a las $horaCita\n\n" +
                                (if(cita.procedimiento.isNotEmpty()) "Procedimiento: *${cita.procedimiento}*\n\n" else "\n") +
                                "Por favor confirmar tu asistencia, gracias."

                        val uri = "https://wa.me/+58$telLimpio?text=${Uri.encode(mensajeCita)}".toUri()
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }) {
                        Icon(painter = painterResource(id = R.drawable.ic_whatsapp), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                    }
                }
                }
            }
        }
    }


fun generarYEnviarAgenda(listaCitas: List<Cita>, listaPacientes: List<Paciente>, incluirTelefonos: Boolean, context: android.content.Context) {
    val hoy = LocalDate.now()
    // 1. FILTRADO ESTRICTO: Solo citas de HOY
    val citasDeHoy = listaCitas.filter {
        it.fechaHora.toLocalDate() == hoy
    }.sortedBy { it.fechaHora }

    if (citasDeHoy.isNotEmpty()) {
        val fechaTitulo = hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val builder = StringBuilder("*AGENDA DEL DÍA $fechaTitulo*\n\n")

        citasDeHoy.forEach { c ->
            val paciente = listaPacientes.find { it.id == c.pacienteId }

            // 2. USO DE PROCEDIMIENTO: Si está vacío, ponemos "Consulta General"
            val infoCita = c.procedimiento.ifBlank { "Consulta General" }

            // Formato: • *Juan Perez* _(Procedimiento)_
            builder.append("• *${c.pacienteNombre}* _($infoCita)_\n")

            // 3. HORA: Como solo es hoy, quitamos los prefijos "Mañana"
            val horaFormateada = c.fechaHora.format(DateTimeFormatter.ofPattern("hh:mm a"))
            builder.append("   *Hora:* $horaFormateada\n")

            if (incluirTelefonos && paciente != null) {
                // Mantenemos el formato de teléfono que usas (0 + número)
                builder.append("   *Tel:* 0${paciente.telefono}\n")
            }
            builder.append("\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, builder.toString())
            setPackage("com.whatsapp")
        }
        context.startActivity(Intent.createChooser(intent, "Compartir agenda"))
    }
}