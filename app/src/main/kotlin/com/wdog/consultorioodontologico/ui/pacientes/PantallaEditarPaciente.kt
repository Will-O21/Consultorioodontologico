package com.wdog.consultorioodontologico.ui.pacientes

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.wdog.consultorioodontologico.R
import com.wdog.consultorioodontologico.entities.Afeccion
import com.wdog.consultorioodontologico.entities.Cuadruple
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import java.util.Locale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarPaciente(
    navController: NavController,
    paciente: Paciente,
    viewModel: PacienteViewModel
) {
    // --- ESTADOS INICIALES ---
    var nombre by remember { mutableStateOf(paciente.nombre) }
    var cedula by remember { mutableStateOf(paciente.cedula) }
    var edad by remember { mutableStateOf(paciente.edad.toString()) }
    var telefono by remember { mutableStateOf(paciente.telefono) }
    var direccion by remember { mutableStateOf(paciente.direccion) }
    var enfermedadesSistemicas by remember { mutableStateOf(paciente.enfermedadesSistemicas) }
    var alergias by remember { mutableStateOf(paciente.alergias) }
    var habitos by remember { mutableStateOf(paciente.habitos) }
    var medicamentos by remember { mutableStateOf(paciente.medicamentos) }
    var motivoConsulta by remember { mutableStateOf(paciente.motivoConsulta) }
    var planTratamiento by remember { mutableStateOf(paciente.planTratamiento) }

    // Parseo inicial del estado de pago para extraer el método si existe
    val estadoLimpio = paciente.estadoPago.substringBefore(" (")
    val metodoInicial = if (paciente.estadoPago.contains("(")) {
        paciente.estadoPago.substringAfter("(").substringBefore(")")
    } else "Efectivo"

    var estadoPago by remember { mutableStateOf(estadoLimpio) }
    var metodoPagoSeleccionado by remember { mutableStateOf(metodoInicial) }
    var expandidoMetodo by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tasaBCV by viewModel.tasaBCV

    LaunchedEffect(Unit) {
        // Al estar dentro de LaunchedEffect, ya estamos en una corrutina.
        // Usamos 'this.launch' (o simplemente 'launch') para iniciar el colector sin bloquear
        // la ejecución de lo que sigue (obtenerTasaBCV).
        launch {
            viewModel.mensajeToast.collect { mensaje ->
                android.widget.Toast.makeText(context, mensaje, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // Esta función de suspensión se ejecutará inmediatamente después de lanzar el collect
        viewModel.obtenerTasaBCV(forzar = false)
    }
    var monto by remember { mutableStateOf(if(paciente.monto % 1.0 == 0.0) paciente.monto.toInt().toString() else paciente.monto.toString()) }
    var abono by remember { mutableStateOf(if(paciente.abono % 1.0 == 0.0) paciente.abono.toInt().toString() else paciente.abono.toString()) }

    var errorMonto by remember { mutableStateOf(false) }
    var errorAbono by remember { mutableStateOf(false) }

    // FECHA NACIMIENTO
    var fechaNacimiento by remember { mutableStateOf(paciente.fechaNacimiento) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // FOTOS (Lógica original intacta)
    var nuevaFotoPerfilUri by remember { mutableStateOf<Uri?>(null) }
    var borrarPerfilAnterior by remember { mutableStateOf(false) }
    val fotoPerfilActual = paciente.fotoPerfil

    var nuevasPlacasUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var placasABorrar by remember { mutableStateOf<List<String>>(emptyList()) }

    val placasActuales = paciente.fotosPlacas.filter { it !in placasABorrar }
    val totalPlacasVisibles = placasActuales.size + nuevasPlacasUris.size

    val launcherPerfil = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            nuevaFotoPerfilUri = uri
            borrarPerfilAnterior = true
        }
    }

    val launcherPlacas = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val espacioDisponible = 2 - placasActuales.size
        nuevasPlacasUris = uris.take(espacioDisponible)
    }

    // --- LÓGICA DE SELECCIÓN DE CONTACTOS ---
    val contactLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        uri?.let {
            val projection = arrayOf(
                android.provider.ContactsContract.Contacts._ID,
                android.provider.ContactsContract.Contacts.DISPLAY_NAME
            )
            context.contentResolver.query(it, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                    val nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)

                    val id = cursor.getString(idIndex)
                    nombre = cursor.getString(nameIndex)

                    context.contentResolver.query(
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val numIndex = phoneCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                            telefono = phoneCursor.getString(numIndex).replace(Regex("[^0-9+]"), "")
                        }
                    }
                }
            }
        }
    }

    // DIBUJO (Lógica original intacta)
    var colorSeleccionado by remember { mutableStateOf<Color?>(null) }
    val puntosAfeccion = remember { mutableStateListOf<Cuadruple<Offset, Color, String, Boolean>>() }
    var modoBorrador by remember { mutableStateOf(false) }
    var tamanoPincel by remember { mutableFloatStateOf(8f) }

    LaunchedEffect(paciente.id) {
        val afeccionesList = viewModel.obtenerAfeccionesPorPacienteId(paciente.id)
        puntosAfeccion.clear()
        val puntosCargados = afeccionesList.map { afeccion ->
            Cuadruple(Offset(afeccion.offsetX, afeccion.offsetY), Color(afeccion.color.toULong()), afeccion.tipoFigura, false)
        }
        puntosAfeccion.addAll(puntosCargados)
    }

    val colorAzul = Color(0xFF094293)
    val colorFondo = Color(0xFFF3F4F6)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha Médica", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101084))
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        val montoValido = monto.toDoubleOrNull() ?: 0.0
                        val abonoValido = abono.toDoubleOrNull() ?: 0.0
                        errorMonto = montoValido < 0
                        errorAbono = abonoValido < 0

                        if (!errorMonto && !errorAbono) {
                            val afecciones = puntosAfeccion.map { (offset, color, tipo, _) ->
                                Afeccion(
                                    offsetX = offset.x, offsetY = offset.y,
                                    color = color.value.toLong(), tipoFigura = tipo, pacienteId = paciente.id, id = 0
                                )
                            }

                            val estadoFinal = if(estadoPago == "Completo") "Al día ($metodoPagoSeleccionado)" else "$estadoPago ($metodoPagoSeleccionado)"

                            val pacienteActualizado = paciente.copy(
                                nombre = nombre, cedula = cedula, edad = edad.toIntOrNull() ?: 0,
                                fechaNacimiento = fechaNacimiento,
                                telefono = telefono, direccion = direccion, enfermedadesSistemicas = enfermedadesSistemicas,
                                alergias = alergias, habitos = habitos, medicamentos = medicamentos,
                                motivoConsulta = motivoConsulta, planTratamiento = planTratamiento,
                                estadoPago = estadoFinal, monto = montoValido, abono = abonoValido,
                                fechaUltimoPago = System.currentTimeMillis() // <--- Registro de fecha para Finanzas
                            )

                            viewModel.actualizarPaciente(
                                paciente = pacienteActualizado,
                                afecciones = afecciones,
                                nuevaFotoPerfil = nuevaFotoPerfilUri,
                                nuevasPlacas = nuevasPlacasUris,
                                borrarPerfilAnterior = borrarPerfilAnterior,
                                placasABorrar = placasABorrar
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorAzul),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Cambios", color = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().background(colorFondo).padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECCIÓN 1: DATOS PERSONALES ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Datos del Paciente", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                    HorizontalDivider()

                    Box(modifier = Modifier.size(120.dp).border(3.dp, colorAzul, CircleShape).clickable { launcherPerfil.launch("image/*") }, contentAlignment = Alignment.Center) {
                        val modeloImagen = nuevaFotoPerfilUri ?: (if (!borrarPerfilAnterior) fotoPerfilActual else null)
                        if (modeloImagen != null) {
                            AsyncImage(model = modeloImagen, contentDescription = "Foto perfil", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Sin perfil", modifier = Modifier.size(50.dp), tint = Color.Gray)
                        }
                    }
                    if (nuevaFotoPerfilUri != null || (!borrarPerfilAnterior && fotoPerfilActual != null)) {
                        Text(text = "Eliminar foto", color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.clickable { borrarPerfilAnterior = true; nuevaFotoPerfilUri = null })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { contactLauncher.launch(null) }) {
                                Icon(Icons.Default.ContactPage, contentDescription = "Importar contacto", tint = colorAzul)
                            }
                        }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cedula, onValueChange = { if (it.all { c -> c.isDigit() }) cedula = it }, label = { Text("Cédula") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = edad, onValueChange = { if (it.all { c -> c.isDigit() }) edad = it }, label = { Text("Edad") }, modifier = Modifier.weight(0.6f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }

                    OutlinedTextField(
                        value = fechaNacimiento, onValueChange = { }, readOnly = true, label = { Text("Cumpleaños") },
                        modifier = Modifier.fillMaxWidth().clickable { mostrarDatePicker = true }, enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray),
                        trailingIcon = { Icon(Icons.Default.Cake, null, tint = colorAzul) }
                    )

                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '+' }) telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            IconButton(onClick = { contactLauncher.launch(null) }) {
                                Icon(Icons.Default.ContactPage, contentDescription = "Importar teléfono", tint = colorAzul)
                            }
                        }
                    )
                    OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            }

            // --- SECCIÓN 2 y 3: HISTORIAL Y CONSULTA (Lógica original intacta) ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Historial Médico", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    OutlinedTextField(value = enfermedadesSistemicas, onValueChange = { enfermedadesSistemicas = it }, label = { Text("Enfermedades Sistémicas") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = alergias, onValueChange = { alergias = it }, label = { Text("Alergias") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = habitos, onValueChange = { habitos = it }, label = { Text("Hábitos") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = medicamentos, onValueChange = { medicamentos = it }, label = { Text("Medicamentos") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Detalles de la Consulta", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    OutlinedTextField(value = motivoConsulta, onValueChange = { motivoConsulta = it }, label = { Text("Motivo de Consulta") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = planTratamiento, onValueChange = { planTratamiento = it }, label = { Text("Plan de Tratamiento") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            }

            // --- SECCIÓN 4: ODONTOGRAMA (Lógica original intacta) ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Odontograma", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    SelectorDibujo(colorSeleccionado, tamanoPincel, modoBorrador, { colorSeleccionado = it; modoBorrador = false }, { modoBorrador = !modoBorrador; if (modoBorrador) colorSeleccionado = null }, { tamanoPincel = it })
                    ModeloDental(puntosAfeccion, { puntosAfeccion.add(Cuadruple(it, colorSeleccionado ?: Color.Red, "pincel_$tamanoPincel", false)) }, { offset -> puntosAfeccion.removeAll { (punto, _, _, _) -> (punto - offset).getDistance() < tamanoPincel * 2f } }, modoBorrador, tamanoPincel, colorSeleccionado)
                }
            }

            // --- SECCIÓN 5: PLACAS (Corrigiendo advertencias) ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Placas y Radiografías", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        placasActuales.forEach { ruta ->
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.5f)) {
                                AsyncImage(model = ruta, contentDescription = "Placa", modifier = Modifier.fillMaxSize().border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                IconButton(onClick = { placasABorrar = placasABorrar + ruta }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            }
                        }
                        nuevasPlacasUris.forEach { uri ->
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.5f)) {
                                AsyncImage(model = uri, contentDescription = "Placa Nueva", modifier = Modifier.fillMaxSize().border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                IconButton(onClick = { nuevasPlacasUris = nuevasPlacasUris.filterNot { it == uri } }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                            }
                        }
                        if (totalPlacasVisibles < 2) {
                            Button(onClick = { launcherPlacas.launch("image/*") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = colorAzul), border = ButtonDefaults.outlinedButtonBorder(enabled = true)) { Text("+ Añadir Placa ($totalPlacasVisibles/2)") }
                        }
                    }
                }
            }

            // --- SECCIÓN 6: FACTURACIÓN (Nuevas validaciones y BCV) ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Estado de Cuenta", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    OutlinedTextField(
                        value = monto, onValueChange = { monto = it; val num = it.toDoubleOrNull(); errorMonto = num == null || num < 0 },
                        label = { Text("Monto Total ($)") }, modifier = Modifier.fillMaxWidth(), isError = errorMonto, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (errorMonto) Text("Monto inválido", color = Color.Red, style = MaterialTheme.typography.bodySmall)

                    SelectorEstadoPago(estadoPago, { estadoPago = it })

                    ExposedDropdownMenuBox(expanded = expandidoMetodo, onExpandedChange = { expandidoMetodo = !expandidoMetodo }) {
                        OutlinedTextField(
                            value = metodoPagoSeleccionado, onValueChange = {}, readOnly = true, label = { Text("Método de Pago") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandidoMetodo) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandidoMetodo, onDismissRequest = { expandidoMetodo = false }) {
                            listOf("Efectivo", "Binance", "Transferencia Bs").forEach { DropdownMenuItem(text = { Text(it) }, onClick = { metodoPagoSeleccionado = it; expandidoMetodo = false }) }
                        }
                    }

                    if (metodoPagoSeleccionado == "Transferencia Bs") {
                        // 1. Decidimos qué monto usar: si el estado es "Abonó", usamos la variable 'abono', sino usamos 'monto'
                        val montoParaCalcular = if (estadoPago == "Abonó") {
                            abono.toDoubleOrNull() ?: 0.0
                        } else {
                            monto.toDoubleOrNull() ?: 0.0
                        }

                        val totalBs = montoParaCalcular * tasaBCV

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Etiqueta y Valor de la Tasa
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Tasa BCV: ${String.format(Locale.getDefault(), "%.2f", tasaBCV)} Bs",
                                            fontSize = 11.sp,
                                            color = colorAzul,
                                            fontWeight = FontWeight.Bold
                                        )
                                        // Botón pequeño para refrescar manualmente
                                        IconButton(
                                            // Al ser acción manual del usuario, forzamos la descarga
                                            onClick = { viewModel.obtenerTasaBCV(forzar = true) },
                                            modifier = Modifier.size(24.dp).padding(start = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Refrescar",
                                                tint = colorAzul,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    // Indicador visual de qué se está calculando
                                    Text(
                                        text = if (estadoPago == "Abonó") "Calculando sobre Abono" else "Calculando Total",
                                        fontSize = 9.sp,
                                        color = Color.DarkGray,
                                        fontStyle = FontStyle.Italic
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Monto: Bs. ${String.format(Locale.getDefault(), "%.2f", totalBs)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF101084)
                                )
                            }
                        }
                    }

                    if (estadoPago == "Abonó") {
                        OutlinedTextField(
                            value = abono, onValueChange = { abono = it; val num = it.toDoubleOrNull(); errorAbono = num == null || num < 0 },
                            label = { Text("Abono Inicial ($)") }, modifier = Modifier.fillMaxWidth(), isError = errorAbono, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (errorAbono) Text("Abono inválido", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    // DIÁLOGO DATE PICKER
    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // 1. Usamos Instant.ofEpochMilli con ZoneOffset.UTC para evitar el desfase de un día
                        val fecha = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC) // Cambiado para que no reste un día
                            .toLocalDate()

                        // 2. Formateamos la fecha directamente desde lo que el usuario eligió
                        // Quitamos la lógica de "anoCalculado" para que respete el año del calendario
                        fechaNacimiento = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%d",
                            fecha.dayOfMonth,
                            fecha.monthValue,
                            fecha.year
                        )

                        // 3. Opcional: Si quieres que la Edad se actualice sola al elegir fecha:
                        val edadCalculada = java.time.Period.between(fecha, java.time.LocalDate.now()).years
                        if (edadCalculada >= 0) {
                            edad = edadCalculada.toString()
                        }
                    }
                    mostrarDatePicker = false
                }) { Text("Confirmar") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

// ... (Las funciones auxiliares ModeloDental, SelectorDibujo y SelectorEstadoPago se mantienen igual a tu código original con modificador private) ...

// -----------------------------------------------------------------------
// FUNCIONES AUXILIARES
// -----------------------------------------------------------------------

@Composable
private fun ModeloDental(
    puntosAfeccion: MutableList<Cuadruple<Offset, Color, String, Boolean>>,
    onPuntoAgregado: (Offset) -> Unit,
    onPuntoEliminado: (Offset) -> Unit,
    modoBorrador: Boolean,
    tamanoPincel: Float,
    colorSeleccionado: Color?
) {
    Box(modifier = Modifier.fillMaxWidth().height(250.dp).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))) {
        val context = LocalContext.current
        val imageBitmap = remember { BitmapFactory.decodeResource(context.resources, R.drawable.modelo_dental2).asImageBitmap() }
        var imageScale by remember { mutableFloatStateOf(1f) }
        var imageOffset by remember { mutableStateOf(Offset.Zero) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(modoBorrador, colorSeleccionado, tamanoPincel) {
                    if (modoBorrador) {
                        // Borrado fluido al deslizar
                        detectDragGestures { change, _ ->
                            change.consume()
                            val adjusted = Offset((change.position.x - imageOffset.x) / imageScale, (change.position.y - imageOffset.y) / imageScale)
                            onPuntoEliminado(adjusted)
                        }
                    } else if (colorSeleccionado != null) {
                        // Dibujado fluido al deslizar
                        detectDragGestures(
                            onDragStart = { start ->
                                onPuntoAgregado(Offset((start.x - imageOffset.x) / imageScale, (start.y - imageOffset.y) / imageScale))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                onPuntoAgregado(Offset((change.position.x - imageOffset.x) / imageScale, (change.position.y - imageOffset.y) / imageScale))
                            }
                        )
                    }
                }
                .onSizeChanged { size ->
                    val scale = minOf(size.width / imageBitmap.width.toFloat(), size.height / imageBitmap.height.toFloat())
                    imageScale = scale
                    imageOffset = Offset((size.width - (imageBitmap.width * scale)) / 2, (size.height - (imageBitmap.height * scale)) / 2)
                }
        ) {
            drawImage(image = imageBitmap, dstOffset = IntOffset(imageOffset.x.toInt(), imageOffset.y.toInt()), dstSize = IntSize((imageBitmap.width * imageScale).toInt(), (imageBitmap.height * imageScale).toInt()))

            puntosAfeccion.forEach { (offset, color, tipo, _) ->
                val radio = tipo.substringAfter("_").toFloatOrNull() ?: 8f
                val scaledOffset = Offset(offset.x * imageScale + imageOffset.x, offset.y * imageScale + imageOffset.y)
                drawCircle(color = color, radius = radio / 2, center = scaledOffset)
            }
        }
    }
}

@Composable
private fun SelectorDibujo(
    colorSeleccionado: Color?, tamanoPincel: Float, modoBorrador: Boolean,
    onColorSelected: (Color) -> Unit, onBorradorSelected: () -> Unit, onTamanoPincelChanged: (Float) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(Color.Red, Color.Blue).forEach { color ->
                Box(
                    modifier = Modifier.size(35.dp).background(color, CircleShape)
                        .border(3.dp, if (colorSeleccionado == color) Color.Black else Color.Transparent, CircleShape)
                        .clickable { onColorSelected(color) }
                )
            }
            IconButton(onClick = onBorradorSelected) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Borrador", tint = if (modoBorrador) Color.Red else Color.Gray) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onTamanoPincelChanged(tamanoPincel - 2f) }) { Text("-", fontWeight = FontWeight.Bold) }
            Text("${tamanoPincel.toInt()} px", fontWeight = FontWeight.Medium)
            IconButton(onClick = { onTamanoPincelChanged(tamanoPincel + 2f) }) { Text("+", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SelectorEstadoPago(estadoPago: String, onEstadoChanged: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val colorAzul = Color(0xFF094293)
    val colorVerde = Color(0xFF155e29)
    val colorRojo = Color(0xFFa51b0b)

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = when (estadoPago) { "Pendiente" -> colorRojo; "Abonó" -> Color.Blue; "Completo" -> colorVerde; else -> colorAzul }),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Estado: $estadoPago", color = Color.White, fontWeight = FontWeight.Bold)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Pendiente") }, onClick = { onEstadoChanged("Pendiente"); expanded = false })
            DropdownMenuItem(text = { Text("Abonó") }, onClick = { onEstadoChanged("Abonó"); expanded = false })
            DropdownMenuItem(text = { Text("Completo") }, onClick = { onEstadoChanged("Completo"); expanded = false })
        }
    }
}