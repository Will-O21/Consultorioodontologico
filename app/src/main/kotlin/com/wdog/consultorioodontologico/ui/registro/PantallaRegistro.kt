package com.wdog.consultorioodontologico.ui.registro

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    navController: NavController,
    viewModel: PacienteViewModel
) {
    // --- ESTADOS DE TEXTO (Pizarra en blanco) ---
    var nombre by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var enfermedadesSistemicas by remember { mutableStateOf("") }
    var alergias by remember { mutableStateOf("") }
    var habitos by remember { mutableStateOf("") }
    var medicamentos by remember { mutableStateOf("") }
    var motivoConsulta by remember { mutableStateOf("") }
    var planTratamiento by remember { mutableStateOf("") }
    var estadoPago by remember { mutableStateOf("Pendiente") }
    var monto by remember { mutableStateOf("") }
    var abono by remember { mutableStateOf("") }

    var errorMonto by remember { mutableStateOf(false) }
    var errorAbono by remember { mutableStateOf(false) }

    // --- NUEVOS ESTADOS (FECHA Y MÉTODO DE PAGO) ---
    var fechaNacimiento by remember { mutableStateOf("") }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var metodoPagoSeleccionado by remember { mutableStateOf("Efectivo") }
    var expandidoMetodo by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val tasaBCV by viewModel.tasaBCV

    LaunchedEffect(Unit) {
        // Unificamos en un solo efecto para mayor limpieza
        launch {
            viewModel.mensajeToast.collect { mensaje ->
                android.widget.Toast.makeText(context, mensaje, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        // Solo descarga si la tasa guardada en SharedPreferences tiene más de 4 horas
        viewModel.obtenerTasaBCV(forzar = false)
    }

    // --- NUEVA LÓGICA DE FOTOS (PERFIL Y PLACAS) ---
    var fotoPerfilUri by remember { mutableStateOf<Uri?>(null) }
    var placasUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val launcherPerfil = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            fotoPerfilUri = uri
        }
    }

    val launcherPlacas = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val espacioDisponible = 2 - placasUris.size
        placasUris = placasUris + uris.take(espacioDisponible)
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
                    nombre = cursor.getString(nameIndex) // Llenamos el nombre automáticamente

                    // Buscamos el teléfono asociado a ese ID
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


    // --- ESTADOS DE DIBUJO ---
    var colorSeleccionado by remember { mutableStateOf<Color?>(null) }
    val puntosAfeccion = remember { mutableStateListOf<Cuadruple<Offset, Color, String, Boolean>>() }
    var modoBorrador by remember { mutableStateOf(false) }
    var tamanoPincel by remember { mutableFloatStateOf(8f) }

    val colorAzul = Color(0xFF094293)
    val colorFondo = Color(0xFFF3F4F6)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Paciente", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101084))
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        val montoValido = monto.toIntOrNull() ?: 0
                        val abonoValido = abono.toIntOrNull() ?: 0

                        errorMonto = montoValido < 0
                        errorAbono = abonoValido < 0

                        if (!errorMonto && !errorAbono && nombre.isNotBlank()) {
                            // 1. Mapeamos los puntos del odontograma a objetos Afeccion
                            val afecciones = puntosAfeccion.map { (offset, color, tipo, _) ->
                                Afeccion(
                                    offsetX = offset.x,
                                    offsetY = offset.y,
                                    color = color.value.toLong(),
                                    tipoFigura = tipo,
                                    pacienteId = 0L // El ViewModel se encargará de poner el ID correcto
                                )
                            }

                            // Construimos el estado final integrando el método de pago
                            val estadoFinal = if(estadoPago == "Completo") "Al día ($metodoPagoSeleccionado)" else "$estadoPago ($metodoPagoSeleccionado)"

                            // 2. Creamos el objeto paciente inicial (Incluyendo fechaNacimiento nueva)
                            val nuevoPaciente = Paciente(
                                nombre = nombre,
                                cedula = cedula,
                                fechaNacimiento = fechaNacimiento, // CAMPO NUEVO AÑADIDO
                                edad = edad.toIntOrNull() ?: 0,
                                telefono = telefono,
                                direccion = direccion,
                                enfermedadesSistemicas = enfermedadesSistemicas,
                                alergias = alergias,
                                habitos = habitos,
                                medicamentos = medicamentos,
                                motivoConsulta = motivoConsulta,
                                planTratamiento = planTratamiento,
                                estadoPago = estadoFinal, // CAMPO MODIFICADO PARA INCLUIR MÉTODO
                                monto = montoValido.toDouble(), // Respetando tu casteo a Double original
                                abono = abonoValido.toDouble(),
                                fechaUltimoPago = System.currentTimeMillis()
                            )

                            // 3. LLAMADA CORRECTA SEGÚN TU VIEWMODEL
                            viewModel.insertarPaciente(
                                paciente = nuevoPaciente,
                                afecciones = afecciones,
                                fotoPerfilUri = fotoPerfilUri, // Pasamos el Uri?
                                placasUris = placasUris        // Pasamos la List<Uri>
                            )

                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorAzul),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Paciente", color = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondo)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- SECCIÓN 1: DATOS PERSONALES Y PERFIL ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                    Text("Datos del Paciente", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                    HorizontalDivider()

                    // FOTO DE PERFIL
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(3.dp, colorAzul, CircleShape)
                            .clickable { launcherPerfil.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (fotoPerfilUri != null) {
                            AsyncImage(
                                model = fotoPerfilUri,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = "Añadir foto", modifier = Modifier.size(50.dp), tint = Color.Gray)
                        }
                    }
                    if (fotoPerfilUri != null) {
                        Text(
                            text = "Quitar foto",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.clickable { fotoPerfilUri = null }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo *") },
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

                    // NUEVO: CAMPO DE FECHA DE NACIMIENTO
                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Cumpleaños") },
                        modifier = Modifier.fillMaxWidth().clickable { mostrarDatePicker = true },
                        enabled = false,
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

            // --- SECCIÓN 2: HISTORIAL MÉDICO ---
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

            // --- SECCIÓN 3: CONSULTA Y PLAN ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Detalles de la Consulta", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    OutlinedTextField(value = motivoConsulta, onValueChange = { motivoConsulta = it }, label = { Text("Motivo de Consulta") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(value = planTratamiento, onValueChange = { planTratamiento = it }, label = { Text("Plan de Tratamiento") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            }

            // --- SECCIÓN 4: ODONTOGRAMA ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Odontograma", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    SelectorDibujo(
                        colorSeleccionado = colorSeleccionado, tamanoPincel = tamanoPincel, modoBorrador = modoBorrador,
                        onColorSelected = { color -> colorSeleccionado = color; modoBorrador = false },
                        onBorradorSelected = { modoBorrador = !modoBorrador; if (modoBorrador) colorSeleccionado = null },
                        onTamanoPincelChanged = { tamanoPincel = it }
                    )

                    ModeloDental(
                        puntosAfeccion = puntosAfeccion,
                        onPuntoAgregado = { offset -> puntosAfeccion.add(Cuadruple(offset, colorSeleccionado ?: Color.Red, "pincel_$tamanoPincel", false)) },
                        onPuntoEliminado = { offsetTocado -> puntosAfeccion.removeAll { (punto, _, _, _) -> (punto - offsetTocado).getDistance() < tamanoPincel * 2f } },
                        modoBorrador = modoBorrador, tamanoPincel = tamanoPincel, colorSeleccionado = colorSeleccionado
                    )
                }
            }

            // --- SECCIÓN 5: PLACAS RADIOGRÁFICAS ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Placas y Radiografías", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Mostrar Placas Seleccionadas
                        placasUris.forEach { uri ->
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.5f)) {
                                AsyncImage(
                                    model = uri, contentDescription = "Placa Nueva",
                                    modifier = Modifier.fillMaxSize().border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { placasUris = placasUris.filterNot { it == uri } },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)
                                ) { Icon(Icons.Default.Close, contentDescription = "Quitar", tint = Color.Red) }
                            }
                        }

                        // Botón para añadir si hay espacio
                        if (placasUris.size < 2) {
                            Button(
                                onClick = { launcherPlacas.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorAzul),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                            ) {
                                Text("+ Añadir Placa (${placasUris.size}/2)")
                            }
                        }
                    }
                }
            }

            // --- SECCIÓN 6: FACTURACIÓN ---
            ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Estado de Cuenta", fontWeight = FontWeight.ExtraBold, color = colorAzul, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()

                    OutlinedTextField(
                        value = monto, onValueChange = { monto = it; errorMonto = it.toIntOrNull() == null || it.toInt() < 0 },
                        label = { Text("Monto Total ($)") }, modifier = Modifier.fillMaxWidth(), isError = errorMonto, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    if (errorMonto) Text("El monto debe ser un número positivo", color = Color.Red, style = MaterialTheme.typography.bodySmall)

                    SelectorEstadoPago(estadoPago = estadoPago, onEstadoChanged = { estadoPago = it })

                    // NUEVO: DROPDOWN DE MÉTODO DE PAGO INCRUSTADO AQUÍ
                    ExposedDropdownMenuBox(expanded = expandidoMetodo, onExpandedChange = { expandidoMetodo = !expandidoMetodo }) {
                        OutlinedTextField(
                            value = metodoPagoSeleccionado, onValueChange = {}, readOnly = true, label = { Text("Método de Pago") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoMetodo) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandidoMetodo, onDismissRequest = { expandidoMetodo = false }) {
                            listOf("Efectivo", "Binance", "Transferencia Bs").forEach { opcion ->
                                DropdownMenuItem(text = { Text(opcion) }, onClick = { metodoPagoSeleccionado = opcion; expandidoMetodo = false })
                            }
                        }
                    }

                    // NUEVO: LÓGICA TASA BCV
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
                                            text = "Tasa BCV: ${String.format(java.util.Locale.getDefault(), "%.2f", tasaBCV)} Bs",
                                            fontSize = 11.sp,
                                            color = colorAzul,
                                            fontWeight = FontWeight.Bold
                                        )
                                        // Botón pequeño para refrescar manualmente
                                        IconButton(
                                            // Al ser un click manual, usamos forzar = true
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
                                    text = "Monto: Bs. ${String.format(java.util.Locale.getDefault(), "%.2f", totalBs)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF101084)
                                )
                            }
                        }
                    }

                    if (estadoPago == "Abonó") {
                        OutlinedTextField(
                            value = abono, onValueChange = { abono = it; errorAbono = it.toIntOrNull() == null || it.toInt() < 0 },
                            label = { Text("Abono Inicial ($)") }, modifier = Modifier.fillMaxWidth(), isError = errorAbono, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (errorAbono) Text("El abono debe ser un número positivo", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DATE PICKER ---
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

// -----------------------------------------------------------------------
// FUNCIONES AUXILIARES (Con sus private originales intactos)
// -----------------------------------------------------------------------

@Composable
private fun ModeloDental(
    puntosAfeccion: MutableList<Cuadruple<Offset, Color, String, Boolean>>, // Cambiado a MutableList para facilitar la edición
    onPuntoAgregado: (Offset) -> Unit,
    onPuntoEliminado: (Offset) -> Unit,
    modoBorrador: Boolean,
    tamanoPincel: Float,
    colorSeleccionado: Color?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Un poco más alto para mayor comodidad
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        val context = LocalContext.current
        val imageBitmap = remember { BitmapFactory.decodeResource(context.resources, R.drawable.modelo_dental2).asImageBitmap() }

        var imageScale by remember { mutableFloatStateOf(1f) }
        var imageOffset by remember { mutableStateOf(Offset.Zero) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(modoBorrador, colorSeleccionado, tamanoPincel) {
                    if (modoBorrador) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val adjustedOffset = Offset(
                                (change.position.x - imageOffset.x) / imageScale,
                                (change.position.y - imageOffset.y) / imageScale
                            )
                            onPuntoEliminado(adjustedOffset)
                        }
                    } else {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                if (colorSeleccionado != null) {
                                    onPuntoAgregado(Offset(
                                        (startOffset.x - imageOffset.x) / imageScale,
                                        (startOffset.y - imageOffset.y) / imageScale
                                    ))
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                if (colorSeleccionado != null) {
                                    onPuntoAgregado(Offset(
                                        (change.position.x - imageOffset.x) / imageScale,
                                        (change.position.y - imageOffset.y) / imageScale
                                    ))
                                }
                            }
                        )
                    }
                }
                .onSizeChanged { size ->
                    val scale = minOf(size.width / imageBitmap.width.toFloat(), size.height / imageBitmap.height.toFloat())
                    imageScale = scale
                    imageOffset = Offset(
                        (size.width - (imageBitmap.width * scale)) / 2,
                        (size.height - (imageBitmap.height * scale)) / 2
                    )
                }
        ) {
            // Dibujar la imagen de fondo (Odontograma)
            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(imageOffset.x.toInt(), imageOffset.y.toInt()),
                dstSize = IntSize(
                    (imageBitmap.width * imageScale).toInt(),
                    (imageBitmap.height * imageScale).toInt()
                )
            )

            // Dibujar cada punto/afección
            puntosAfeccion.forEach { (offset, color, tipo, _) ->
                val radioGuardado = tipo.substringAfter("_").toFloatOrNull() ?: 8f
                val scaledOffset = Offset(
                    offset.x * imageScale + imageOffset.x,
                    offset.y * imageScale + imageOffset.y
                )
                drawCircle(
                    color = color,
                    radius = radioGuardado / 2, // El radio es la mitad del grosor del pincel
                    center = scaledOffset
                )
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