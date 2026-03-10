package com.wdog.consultorioodontologico.ui.laboratorios

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.wdog.consultorioodontologico.entities.Laboratorio
import com.wdog.consultorioodontologico.viewmodels.LaboratorioViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLaboratorios(viewModel: LaboratorioViewModel) {
    val laboratorios by viewModel.todosLosLaboratorios.collectAsState(initial = emptyList())

    // --- ESTADO DE PESTAÑAS ---
    var tabIndex by remember { mutableIntStateOf(0) }
    val categorias = listOf("Técnico Dental", "Técnico Mantenimiento", "Proveedor", "Laboratorio")

    var mostrarDialog by remember { mutableStateOf(false) }
    var labAEditar by remember { mutableStateOf<Laboratorio?>(null) }
    var labAEliminar by remember { mutableStateOf<Laboratorio?>(null) }
    val context = LocalContext.current

    // --- MODO SELECCIÓN ---
    var seleccionados by remember { mutableStateOf(setOf<Long>()) }
    val modoSeleccionActivo = seleccionados.isNotEmpty()
    var mostrarDialogoBorrarVarios by remember { mutableStateOf(false) }

    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorRojoSeleccion = Color(0xFF530E0E)

    // --- FILTRADO Y ORDENACIÓN ---
    // Filtramos por categoría (especialidad)
    // Ordenamos: 1. Los que tienen "PENDIENTE" en notas. 2. Por nombre alfabéticamente.
    val listaFiltradaYOrdenada = laboratorios
        .filter { it.especialidad == categorias[tabIndex] }
        .sortedWith(
            compareByDescending<Laboratorio> { it.estado == "Pendiente" } // 1. Pendientes primero
                .thenBy { it.nombre } // 2. Alfabético por nombre
        )

    androidx.activity.compose.BackHandler(enabled = modoSeleccionActivo) {
        seleccionados = emptySet()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(if (modoSeleccionActivo) colorRojoSeleccion else colorAzulOscuro)
                    .fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = if (modoSeleccionActivo) "${seleccionados.size} Seleccionados" else "Gestión Externa",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Pestañas Material 3
                PrimaryTabRow(
                    selectedTabIndex = tabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabIndex),
                        color = Color.White
                    )}
                ) {
                    categorias.forEachIndexed { index, titulo ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = { Text(titulo, fontSize = 12.sp, maxLines = 1) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (modoSeleccionActivo) mostrarDialogoBorrarVarios = true
                    else { labAEditar = null; mostrarDialog = true }
                },
                containerColor = if (modoSeleccionActivo) colorRojoSeleccion else colorAzulBotones,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(if (modoSeleccionActivo) Icons.Default.Delete else Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA))) {
            if (listaFiltradaYOrdenada.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros en esta categoría", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaFiltradaYOrdenada, key = { it.id }) { lab ->
                        ItemLaboratorio(
                            lab = lab,
                            estaSeleccionado = seleccionados.contains(lab.id),
                            modoSeleccionActivo = modoSeleccionActivo,
                            onClick = {
                                if (modoSeleccionActivo) {
                                    seleccionados = if (seleccionados.contains(lab.id)) seleccionados - lab.id else seleccionados + lab.id
                                } else { labAEditar = lab }
                            },
                            onLongClick = { if (!modoSeleccionActivo) seleccionados = seleccionados + lab.id },
                            onDelete = { labAEliminar = lab },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, "tel:${lab.telefono}".toUri())
                                context.startActivity(intent)
                            },
                            onWhatsApp = {
                                val intent = Intent(Intent.ACTION_VIEW, "https://api.whatsapp.com/send?phone=${lab.telefono}".toUri())
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialog || labAEditar != null) {
        DialogAgregarLaboratorio(
            labExistente = labAEditar,
            onDismiss = { mostrarDialog = false; labAEditar = null },
            onConfirm = { nuevoLab ->
                viewModel.guardarLaboratorio(nuevoLab)
                mostrarDialog = false
                labAEditar = null
            }
        )
    }

    if (labAEliminar != null) {
        AlertDialog(
            onDismissRequest = { labAEliminar = null },
            title = { Text("Eliminar Contacto", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar a ${labAEliminar?.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    labAEliminar?.let { viewModel.eliminarLaboratorio(it) }
                    labAEliminar = null
                }) { Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { labAEliminar = null }) { Text("CANCELAR") } }
        )
    }

    if (mostrarDialogoBorrarVarios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrarVarios = false },
            title = { Text("Eliminar Laboratorios", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar los ${seleccionados.size} seleccionados?") },
            confirmButton = {
                TextButton(onClick = {
                    seleccionados.forEach { id ->
                        laboratorios.find { it.id == id }?.let { viewModel.eliminarLaboratorio(it) }
                    }
                    seleccionados = emptySet()
                    mostrarDialogoBorrarVarios = false
                }) { Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoBorrarVarios = false }) { Text("CANCELAR") } }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ItemLaboratorio(
    lab: Laboratorio,
    estaSeleccionado: Boolean,
    modoSeleccionActivo: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val colorRojoVino = Color(0xFF530E0E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (estaSeleccionado) 3.dp else 0.dp,
                color = if (estaSeleccionado) colorRojoVino else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(if (estaSeleccionado) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ETIQUETA PENDIENTE (Usando el campo directo de la entidad)
                if (lab.estado == "Pendiente") {
                    Surface(
                        color = Color(0xFFAD1D1D),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("PENDIENTE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Column(Modifier.weight(1f)) {
                    Text(lab.contacto, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
                    Text(lab.nombre, color = Color.Gray, fontSize = 13.sp)
                }

                if (!modoSeleccionActivo) {
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray) }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Subcategoría y Fecha (Usando campos directos)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(lab.notas, color = Color(0xFF101084), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (lab.fechaEntrega.isNotBlank() && lab.estado == "Pendiente") {
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.Event, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(" Entrega: ${lab.fechaEntrega}", fontSize = 12.sp, color = Color.DarkGray)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray.copy(0.5f))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCall, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101084)),
                    shape = RoundedCornerShape(8.dp), enabled = !modoSeleccionActivo
                ) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Llamar", fontSize = 12.sp)
                }
                Button(
                    onClick = onWhatsApp, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp), enabled = !modoSeleccionActivo
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("WhatsApp", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarLaboratorio(
    labExistente: Laboratorio?,
    onDismiss: () -> Unit,
    onConfirm: (Laboratorio) -> Unit
) {
    var contacto by remember { mutableStateOf(labExistente?.contacto ?: "") }
    var telefono by remember { mutableStateOf(labExistente?.telefono ?: "") }
    var nombre by remember { mutableStateOf(labExistente?.nombre ?: "") }

    // Lógica de Categorías y Subcategorías
    val categorias = listOf("Técnico Dental", "Técnico Mantenimiento", "Proveedor", "Laboratorio")
    var catSel by remember { mutableStateOf(labExistente?.especialidad ?: "Técnico Dental") }
    var expCat by remember { mutableStateOf(false) }

    val subcategorias = when (catSel) {
        "Técnico Dental" -> listOf("Prótesis", "Férula")
        "Técnico Mantenimiento" -> listOf("Aire", "Unidades", "Plomería")
        "Proveedor" -> listOf("Insumos", "Instrumentales", "Equipos")
        "Laboratorio" -> listOf("Exámenes", "Historia")
        else -> listOf("Otros")
    }

    // Inicialización limpia usando los nuevos campos de la Entidad
    var subSel by remember { mutableStateOf(labExistente?.notas ?: (subcategorias.firstOrNull() ?: "")) }
    var expSub by remember { mutableStateOf(false) }

    // Estados
    val estados = listOf("Solo Registro", "Pendiente", "Recibido")
    var estadoSel by remember { mutableStateOf(labExistente?.estado ?: "Solo Registro") }
    var expEst by remember { mutableStateOf(false) }

    // Fecha
    var fechaEntrega by remember { mutableStateOf(labExistente?.fechaEntrega ?: "") }

    // Lógica para resetear subcategoría si cambia la categoría superior
    LaunchedEffect(catSel) {
        if (labExistente == null || catSel != labExistente.especialidad) {
            subSel = subcategorias.firstOrNull() ?: ""
        }
    }

    // --- LÓGICA DE CONTACTOS (SOLO TELÉFONO) ---
    val context = LocalContext.current
    val contactLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            // Solo pedimos el ID para buscar el teléfono asociado
            val projection = arrayOf(android.provider.ContactsContract.Contacts._ID)
            context.contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                    val id = cursor.getString(idIndex)

                    context.contentResolver.query(
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val numIndex = phoneCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                            // Extraemos el número y limpiamos espacios o guiones
                            telefono = phoneCursor.getString(numIndex).replace(Regex("[^0-9+]"), "")
                        }
                    }
                }
            }
        }
    }

    // --- LÓGICA DE DATE PICKER ---
    var mostrarDatePicker by remember { mutableStateOf(false) }

// Intentamos convertir la fecha existente a milisegundos para el calendario
    val fechaInicialMillis = remember(labExistente) {
        if (!labExistente?.fechaEntrega.isNullOrBlank()) {
            try {
                val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale("es", "ES")).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                // Usamos el año actual para la conversión
                val cal = java.util.Calendar.getInstance()
                val fecha = sdf.parse(labExistente!!.fechaEntrega)
                val fechaCal = java.util.Calendar.getInstance().apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                fechaCal.time = fecha
                fechaCal.set(java.util.Calendar.YEAR, cal.get(java.util.Calendar.YEAR))
                fechaCal.timeInMillis
            } catch (e: Exception) { null }
        } else null
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fechaInicialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Solo permite fechas desde hoy en adelante (UTC)
                val hoy = java.time.LocalDate.now()
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
                return utcTimeMillis >= hoy
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (labExistente == null) "Nuevo Registro" else "Editar Registro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = contacto, onValueChange = { contacto = it }, label = { Text("Nombre del contacto") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    trailingIcon = {
                        IconButton(onClick = { contactLauncher.launch(null) }) {
                            Icon(Icons.Default.ContactPage, contentDescription = "Buscar en contactos", tint = Color(0xFF101084))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Empresa / Laboratorio") }, leadingIcon = { Icon(Icons.Default.Business, null) }, modifier = Modifier.fillMaxWidth())

                // Selector Categoría
                ExposedDropdownMenuBox(expanded = expCat, onExpandedChange = { expCat = it }) {
                    OutlinedTextField(value = catSel, onValueChange = {}, readOnly = true, label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expCat) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                    ExposedDropdownMenu(expanded = expCat, onDismissRequest = { expCat = false }) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    catSel = cat
                                    // Al cambiar categoría, forzamos a que subSel tome la primera opción de la nueva lista
                                    expCat = false
                                }
                            )
                        }
                    }
                }

                // Selector Subcategoría (Condicional)
                ExposedDropdownMenuBox(expanded = expSub, onExpandedChange = { expSub = it }) {
                    OutlinedTextField(value = subSel, onValueChange = {}, readOnly = true, label = { Text("Propósito / Subcategoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expSub) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                    ExposedDropdownMenu(expanded = expSub, onDismissRequest = { expSub = false }) {
                        subcategorias.forEach { sub ->
                            DropdownMenuItem(text = { Text(sub) }, onClick = { subSel = sub; expSub = false })
                        }
                    }
                }

                // Selector Estado
                ExposedDropdownMenuBox(expanded = expEst, onExpandedChange = { expEst = it }) {
                    OutlinedTextField(value = estadoSel, onValueChange = {}, readOnly = true, label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expEst) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth())
                    ExposedDropdownMenu(expanded = expEst, onDismissRequest = { expEst = false }) {
                        estados.forEach { est ->
                            DropdownMenuItem(text = { Text(est) }, onClick = { estadoSel = est; expEst = false })
                        }
                    }
                }

                // Fecha de Entrega (Solo si es Pendiente)
                if (estadoSel == "Pendiente") {
                    // Usamos una Box para que toda el área sea clickable
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = fechaEntrega,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Fecha de Entrega") },
                            leadingIcon = {
                                IconButton(onClick = { mostrarDatePicker = true }) {
                                    Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF101084))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Superficie transparente encima para capturar el click sin activar el teclado
                        Box(
                            Modifier
                                .matchParentSize()
                                .combinedClickable(onClick = { mostrarDatePicker = true })
                        )
                    }

                    if (mostrarDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { mostrarDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        // Usamos UTC para que la fecha seleccionada sea exacta
                                        val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale("es", "ES")).apply {
                                            // Esto asegura que si el calendario dice 26, el texto diga 26.
                                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                                        }
                                        fechaEntrega = sdf.format(java.util.Date(millis))
                                    }
                                    mostrarDatePicker = false
                                }) { Text("ACEPTAR", fontWeight = FontWeight.Bold) }
                            },
                            dismissButton = {
                                TextButton(onClick = { mostrarDatePicker = false }) { Text("CANCELAR") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nombre.isNotBlank() && telefono.isNotBlank()) {
                    onConfirm(Laboratorio(
                        id = labExistente?.id ?: 0L,
                        nombre = nombre,
                        contacto = contacto,
                        telefono = telefono,
                        especialidad = catSel,
                        notas = subSel,        // Ahora 'notas' solo guarda la subcategoría
                        estado = estadoSel,    // Campo directo
                        // Si no es pendiente, forzamos que la fecha sea vacía aunque hubiera algo escrito antes
                        fechaEntrega = if (estadoSel == "Pendiente") fechaEntrega else ""
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}