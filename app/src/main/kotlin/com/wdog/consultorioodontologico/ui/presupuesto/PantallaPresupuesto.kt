package com.wdog.consultorioodontologico.ui.presupuesto

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wdog.consultorioodontologico.entities.ServicioPresupuesto
import com.wdog.consultorioodontologico.viewmodels.PresupuestoViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.graphics.toColorInt
import com.wdog.consultorioodontologico.ui.components.OdontogramaInteractivo
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PantallaPresupuesto(viewModel: PresupuestoViewModel, pacienteViewModel: PacienteViewModel,
                        odontogramaViewModel: com.wdog.consultorioodontologico.viewmodels.OdontogramaViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val servicios by viewModel.listaServiciosBase.collectAsState(initial = emptyList())
    val seleccionados by viewModel.itemsSeleccionados.collectAsState()
    val tasaBCV = pacienteViewModel.tasaBCV.doubleValue
    val estaCargandoTasa by pacienteViewModel.cargandoTasa
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Usamos un ID negativo (-99) para indicar que es un presupuesto temporal
        // y no se confunda con IDs reales de pacientes
        odontogramaViewModel.cargarOdontograma(id = -99L, esPaciente = false)
    }

    // 1. Navegación por Gestos (Swipe Tabs)
    val pagerState = rememberPagerState(pageCount = { 2 })

    // --- NUEVO: Estado del Odontograma ---
    val dientesMarcados by odontogramaViewModel.dientesMarcados.collectAsState()
    val estadosEspeciales by odontogramaViewModel.estadosEspeciales.collectAsState()
    val esPediatrico by odontogramaViewModel.esModoPediatrico.collectAsState()
    val puentes by odontogramaViewModel.puentes.collectAsState()
    val notasDientes by odontogramaViewModel.notasDientes.collectAsState() // <--- NUEVO
    val dientesTratados by odontogramaViewModel.dientesTratados.collectAsState() // <--- NUEVO

    var mostrarOdontograma by remember { mutableStateOf(false) }
    // Estados de Diálogos
    var mostrarDialogServicio by remember { mutableStateOf<ServicioPresupuesto?>(null) }
    var esEdicion by remember { mutableStateOf(false) }
    var mostrarDialogCompartir by remember { mutableStateOf(false) }

    // --- NUEVO: Gestión de Nombre Temporal ---
    var mostrarDialogNombre by remember { mutableStateOf(false) }
    var nombreTemporalPaciente by remember { mutableStateOf("") }
    var esParaPdf by remember { mutableStateOf(false) } // Para saber qué disparar después del nombre
    var mostrarVistaPreviaPdf by remember { mutableStateOf(false) }

    // --- MODO SELECCIÓN ---
    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorRojoSeleccion = Color(0xFF530E0E)
    val colorFondo = Color(0xFFF8F9FA)

    var seleccionadosCatalogo by remember { mutableStateOf(setOf<Long>()) }
    val modoSeleccionActivo = seleccionadosCatalogo.isNotEmpty()
    var mostrarDialogoBorrarVarios by remember { mutableStateOf(false) }
    var servicioAEliminar by remember { mutableStateOf<ServicioPresupuesto?>(null) }

    BackHandler(enabled = modoSeleccionActivo) { seleccionadosCatalogo = emptySet() }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
                    .background(if (modoSeleccionActivo) colorRojoSeleccion else colorAzulOscuro)
                    .padding(top = 2.dp)
            ) {

                Text(
                    text = if (modoSeleccionActivo) "${seleccionadosCatalogo.size} Seleccionados" else "Presupuestos",
                    color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))
                // Tabs sincronizados con el Pager
                // Tabs sincronizados con el Pager usando la API moderna de M3
                SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(pagerState.currentPage),
                            color = Color.White
                        )
                    },
                    divider = {}
                ){
                    Tab(selected = pagerState.currentPage == 0, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("Calculadora", fontWeight = FontWeight.Bold) })
                    Tab(selected = pagerState.currentPage == 1, onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("Catálogo", fontWeight = FontWeight.Bold) })
                }
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 1) {
                FloatingActionButton(
                    onClick = {
                        if (modoSeleccionActivo) mostrarDialogoBorrarVarios = true
                        else { esEdicion = false; mostrarDialogServicio = ServicioPresupuesto(nombreServicio = "", precioSugerido = 0.0) }
                    },
                    containerColor = if (modoSeleccionActivo) colorRojoSeleccion else colorAzulBotones,
                    contentColor = Color.White, shape = CircleShape
                ) {
                    Icon(if (modoSeleccionActivo) Icons.Default.Delete else Icons.Default.Add, null)
                }
            }
        }
    ) { padding ->
        // 1. Implementación de HorizontalPager para Swipe
        HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) { page ->
            if (page == 0) {
                // --- VISTA 1: CALCULADORA ---
                Column(Modifier.fillMaxSize().background(colorFondo)) {
                    // Botón para mostrar/ocultar odontograma
                    TextButton(
                        onClick = { mostrarOdontograma = !mostrarOdontograma },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (mostrarOdontograma) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                        Text(if (mostrarOdontograma) "OCULTAR MAPA DENTAL" else "VER MAPA DENTAL (ODONTOGRAMA)")
                    }

                    AnimatedVisibility(visible = mostrarOdontograma) {
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            // Cambiamos el nombre a tu componente: OdontogramaInteractivo
                            OdontogramaInteractivo(
                                dientesSeleccionados = dientesMarcados,
                                estadosEspeciales = estadosEspeciales,
                                esModoPediatrico = esPediatrico,
                                puentes = puentes,
                                notasDientes = notasDientes,        // <--- PASAMOS NOTAS
                                dientesTratados = dientesTratados,  // <--- PASAMOS TRATADOS
                                onToggleCara = { diente, cara ->
                                    odontogramaViewModel.actualizarCaraDiente(diente, cara)
                                },
                                onLongClickDiente = { diente ->
                                    odontogramaViewModel.rotarEstadoEspecial(diente)
                                },
                                onLimpiar = { odontogramaViewModel.limpiarOdontograma() },

                                // --- CALLBACKS DE ACCIÓN ---
                                onCambiarEstadoEspecial = { diente, estado ->
                                    odontogramaViewModel.setEstadoEspecial(diente, estado)
                                },
                                onConfigurarPuente = { diente ->
                                    odontogramaViewModel.iniciarOCompletarPuente(diente)
                                },
                                onGuardarNota = { diente, nota ->
                                    odontogramaViewModel.guardarNotaDiente(diente, nota)
                                },
                                onLimpiarDiente = { diente ->
                                    odontogramaViewModel.limpiarDienteIndividual(diente)
                                },
                                onMarcarTratado = { diente, estaTratado ->
                                    odontogramaViewModel.marcarComoTratado(diente, estaTratado)
                                },
                                onToggleModoPediatrico = {
                                    odontogramaViewModel.toggleModoPediatrico()
                                }
                            )
                        }
                    }

                    if (servicios.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Agrega servicios en el Catálogo", color = Color.Gray) }
                    } else {
                        LazyColumn(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(servicios) { servicio ->
                                // 4. Contador de Cantidad (+/-) integrado
                                ItemCalculadoraAvanzado(
                                    servicio = servicio,
                                    cantidad = seleccionados[servicio.id] ?: 0,
                                    tasaBCV = tasaBCV,
                                    onUpdateCant = { nueva -> viewModel.actualizarCantidad(servicio.id, nueva) }
                                )
                            }
                        }

                        // 2. Comparativa de Tasa (Total en $ y Bs)
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp),
                            thickness = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White // Fondo sólido para que no se transparente la lista
                        ) {
                            val totalUSD = servicios.sumOf { (seleccionados[it.id] ?: 0) * it.precioSugerido }

                            Column(
                                Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Text("Total Estimado:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$${String.format(java.util.Locale.US, "%.2f", totalUSD)}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = colorAzulOscuro
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "≈ Bs. ${String.format(java.util.Locale.US, "%.2f", totalUSD * tasaBCV)}",
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            if (estaCargandoTasa) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colorAzulOscuro)
                                            } else {
                                                IconButton(
                                                    onClick = { pacienteViewModel.obtenerTasaBCV(forzar = true) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Refresh, null, tint = colorAzulOscuro, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { esParaPdf = true; mostrarDialogNombre = true },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101084)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Presupuesto Completo PDF", fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = { esParaPdf = false; mostrarDialogNombre = true },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = seleccionados.isNotEmpty()
                                    ) {
                                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Presupuesto Simple WhatsApp", fontSize = 13.sp)
                                    }
                                }

                                TextButton(onClick = { viewModel.limpiarCalculadora() }) {
                                    Text("Limpiar selección", color = Color.Gray)
                                }
                            } // Cierre de la Column principal
                        }
                    }
                }
            } else {
                // --- VISTA 2: CATÁLOGO (Agrupación y Acordeón) ---
                // 6. Agrupación por Categorías
                val serviciosOrdenados = servicios.sortedBy { it.categoria }
                val categorias = serviciosOrdenados.groupBy { it.categoria }
                LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    categorias.forEach { (nombreCat, listaServicios) ->
                        item {
                            // 6. Vista en Acordeón (Header de categoría)
                            Text(
                                text = nombreCat.uppercase(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = colorAzulOscuro.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            )
                        }
                        items(listaServicios) { servicio ->
                            ItemCatalogoAvanzado(
                                servicio = servicio,
                                estaSeleccionado = seleccionadosCatalogo.contains(servicio.id),
                                modoSeleccionActivo = modoSeleccionActivo,
                                onClick = {
                                    if (modoSeleccionActivo) {
                                        seleccionadosCatalogo = if (seleccionadosCatalogo.contains(servicio.id))
                                            seleccionadosCatalogo - servicio.id else seleccionadosCatalogo + servicio.id
                                    } else {
                                        // 12. Edición directa al tocar
                                        esEdicion = true
                                        mostrarDialogServicio = servicio
                                    }
                                },
                                onLongClick = { if (!modoSeleccionActivo) seleccionadosCatalogo = setOf(servicio.id) },
                                onDelete = { servicioAEliminar = servicio }
                            )
                        }
                    }
                }
            }
        }
    }
    }

    // 12. Diálogo único para Crear y Editar
    if (mostrarDialogServicio != null) {
        DialogGestionServicio(
            servicioActual = mostrarDialogServicio!!,
            esEdicion = esEdicion,
            onDismiss = { mostrarDialogServicio = null },
            onConfirm = { editado ->
                if (esEdicion) viewModel.actualizarServicioEnCatalogo(editado)
                else viewModel.agregarServicioAlCatalogo(editado.nombreServicio, editado.precioSugerido, editado.categoria, editado.colorHex)
                mostrarDialogServicio = null
            }
        )
    }

    // 7 y 11. Diálogo de Compartir con Notas Personalizadas
    if (mostrarDialogCompartir) {
        DialogCompartirPresupuesto(
            totalUSD = servicios.sumOf { (seleccionados[it.id] ?: 0) * it.precioSugerido },
            tasa = tasaBCV, // Ya usa la variable local que definimos arriba
            onDismiss = { mostrarDialogCompartir = false },
            onConfirm = { notaFinal ->
                val encabezado = if(nombreTemporalPaciente.isNotBlank()) "Presupuesto para: $nombreTemporalPaciente\n" else ""
                // Usamos la tasa centralizada para el texto de WhatsApp
                val texto = viewModel.generarTextoPresupuesto(servicios, encabezado + notaFinal, tasaBCV)

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, texto)
                }
                context.startActivity(Intent.createChooser(intent, "Enviar presupuesto"))
                mostrarDialogCompartir = false
            }
        )
    }

    // Diálogos de eliminación (Lógica original mantenida)
    if (mostrarDialogoBorrarVarios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrarVarios = false },
            title = { Text("Eliminar Servicios") },
            text = { Text("¿Eliminar los ${seleccionadosCatalogo.size} seleccionados?") },
            confirmButton = {
                TextButton(onClick = {
                    seleccionadosCatalogo.forEach { id -> servicios.find { it.id == id }?.let { viewModel.eliminarServicioDelCatalogo(it) } }
                    seleccionadosCatalogo = emptySet()
                    mostrarDialogoBorrarVarios = false
                }) { Text("ELIMINAR", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoBorrarVarios = false }) { Text("CANCELAR") } }
        )
    }
    if (servicioAEliminar != null) {
        AlertDialog(
            onDismissRequest = { servicioAEliminar = null },
            title = { Text("Eliminar Servicio") },
            text = { Text("¿Eliminar '${servicioAEliminar?.nombreServicio}'?") },
            confirmButton = {
                TextButton(onClick = {
                    servicioAEliminar?.let { viewModel.eliminarServicioDelCatalogo(it) }
                    servicioAEliminar = null
                }) { Text("ELIMINAR", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { servicioAEliminar = null }) { Text("CANCELAR") } }
        )
    }

    // --- DIÁLOGO PARA PEDIR EL NOMBRE (NUEVO) ---
    if (mostrarDialogNombre) {
        AlertDialog(
            onDismissRequest = { mostrarDialogNombre = false },
            title = { Text("Personalizar Presupuesto", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nombreTemporalPaciente,
                    onValueChange = { nombreTemporalPaciente = it },
                    label = { Text("Nombre del Paciente") },
                    placeholder = { Text("Ej: Juan Pérez") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    mostrarDialogNombre = false
                    if (esParaPdf) {
                        // En lugar de generar, vamos a la vista previa
                        mostrarVistaPreviaPdf = true
                    } else {
                        mostrarDialogCompartir = true
                    }
                }) { Text("Continuar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogNombre = false }) { Text("Omitir") } }
        )
    }

    // --- DIÁLOGO DE VISTA PREVIA PDF ---
    if (mostrarVistaPreviaPdf) {
        AlertDialog(
            onDismissRequest = { mostrarVistaPreviaPdf = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text("Vista Previa del Reporte", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se generará un documento técnico con:", fontSize = 14.sp)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(0.2f))
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("👤 Paciente: ${if(nombreTemporalPaciente.isBlank()) "No especificado" else nombreTemporalPaciente}", fontWeight = FontWeight.Medium)
                            Text("🦷 Mapa Dental: Incluido")
                            Text("📝 Notas Clínicas: ${notasDientes.size} registradas")
                            Text("💰 Presupuesto: ${seleccionados.size} servicios")
                        }
                    }
                    Text("¿Desea generar y compartir este documento?", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarVistaPreviaPdf = false
                        // AQUÍ SÍ GENERAMOS EL PDF REAL
                        val pdfGenerator = com.wdog.consultorioodontologico.util.PdfOdontogramaGenerator(context)
                        val prefs = context.getSharedPreferences("config_consultorio", android.content.Context.MODE_PRIVATE)

                        pdfGenerator.generarPdf(
                            nombrePaciente = if(nombreTemporalPaciente.isBlank()) "Paciente" else nombreTemporalPaciente,
                            nombreDoctor = prefs.getString("nombre_doc", "Doc") ?: "Doc",
                            nombreConsultorio = prefs.getString("nombre_cons", "Consultorio") ?: "Consultorio",
                            logoPath = prefs.getString("logo_path", null),
                            dientesMarcados = dientesMarcados,
                            estadosEspeciales = estadosEspeciales,
                            notasDientes = notasDientes,
                            dientesTratados = emptyMap(),
                            puentes = puentes,
                            esModoPediatrico = esPediatrico
                        )
                        // Limpiamos el nombre para la próxima vez
                        nombreTemporalPaciente = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101084))
                ) {
                    Text("Generar y Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarVistaPreviaPdf = false }) {
                    Text("Cancelar", color = Color.Red)
                }
            }
        )
    }


}

@Composable
fun ItemCalculadoraAvanzado(servicio: ServicioPresupuesto, cantidad: Int, tasaBCV: Double, onUpdateCant: (Int) -> Unit) {
    val estaSeleccionado = cantidad > 0
    val colorBase = Color(servicio.colorHex.toColorInt())

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            // Agregamos borde solo si está seleccionado, igual que en el catálogo
            .border(
                width = if (estaSeleccionado) 2.dp else 0.dp,
                color = if (estaSeleccionado) colorBase else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        // Quitamos el fondo coloreado para que sea blanco limpio
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(colorBase.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = obtenerIconoServicio(servicio.nombreServicio, servicio.categoria),
                    contentDescription = null,
                    tint = colorBase,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(servicio.nombreServicio, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "$${String.format(java.util.Locale.US, "%.2f", servicio.precioSugerido)} | Bs. ${String.format(java.util.Locale.US, "%.2f", servicio.precioSugerido * tasaBCV)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Selector de Cantidad (Sin sombra gris "feita")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(colorBase.copy(0.05f), RoundedCornerShape(25.dp))
                    .border(1.dp, colorBase.copy(0.2f), RoundedCornerShape(25.dp))
                    .padding(horizontal = 4.dp)
            ) {
                IconButton(
                    onClick = { if (cantidad > 0) onUpdateCant(cantidad - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Remove, null, Modifier.size(16.dp), tint = if(cantidad > 0) Color.Black else Color.Gray)
                }

                Text(
                    text = "$cantidad",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = if (estaSeleccionado) colorBase else Color.Black
                )

                IconButton(
                    onClick = { onUpdateCant(cantidad + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = colorBase)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ItemCatalogoAvanzado(
    servicio: ServicioPresupuesto,
    estaSeleccionado: Boolean,
    modoSeleccionActivo: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    // 5. Filtro Cromático Dinámico (Gradiente sutil)
    val colorCat = Color(servicio.colorHex.toColorInt())

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (estaSeleccionado) Modifier.border(2.dp, colorCat, RoundedCornerShape(18.dp))
                else Modifier
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Box(Modifier.background(Brush.horizontalGradient(listOf(colorCat.copy(0.1f), Color.Transparent)))) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                // Icono con fondo circular tenue
                Box(
                    Modifier.size(40.dp).background(colorCat.copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = obtenerIconoServicio(servicio.nombreServicio, servicio.categoria),
                        contentDescription = null,
                        tint = colorCat,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // --- NUEVO: TEXTOS DE NOMBRE Y PRECIO ---
                Column(Modifier.weight(1f)) {
                    Text(
                        text = servicio.nombreServicio,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "$${String.format(java.util.Locale.US, "%.2f", servicio.precioSugerido)}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                if (!modoSeleccionActivo) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color.LightGray.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DialogGestionServicio(
    servicioActual: ServicioPresupuesto,
    esEdicion: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ServicioPresupuesto) -> Unit
) {
    var nombre by remember { mutableStateOf(servicioActual.nombreServicio) }
    var precio by remember { mutableStateOf(servicioActual.precioSugerido.toString()) }
    var categoria by remember { mutableStateOf(servicioActual.categoria) }
    var colorHex by remember { mutableStateOf(servicioActual.colorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (esEdicion) "Editar Servicio" else "Nuevo Servicio", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio ($)") })
                OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría (ej: Cirugía)") })

                Text("Color de Categoría:", fontSize = 12.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#101084", "#25D366", "#E91E63", "#FF9800", "#9C27B0").forEach { color ->
                        Box(
                            Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color(color.toColorInt()))
                                .border(
                                    width = if (colorHex == color) 2.dp else 0.dp,
                                    color = if (colorHex == color) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .combinedClickable { colorHex = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = precio.toDoubleOrNull() ?: 0.0
                onConfirm(servicioActual.copy(nombreServicio = nombre, precioSugerido = p, categoria = categoria, colorHex = colorHex))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun DialogCompartirPresupuesto(
    totalUSD: Double,
    tasa: Double,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var notaPersonalizada by remember { mutableStateOf("") }
    // 7. Banco de Cláusulas Rápidas
    val clausulas = listOf("Sujeto a cambios de tasa", "Válido por 15 días", "Requiere 50% abono")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar Presupuesto", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Total: $${String.format(java.util.Locale.US, "%.2f", totalUSD)} / Bs. ${String.format(java.util.Locale.US, "%.2f", totalUSD * tasa)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101084)
                )

                OutlinedTextField(
                    value = notaPersonalizada,
                    onValueChange = { notaPersonalizada = it },
                    label = { Text("Nota o Mensaje") },
                    modifier = Modifier.height(100.dp)
                )

                Text("Notas rápidas:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    clausulas.forEach { clausula ->
                        AssistChip(
                            onClick = { notaPersonalizada += if (notaPersonalizada.isEmpty()) clausula else "\n$clausula" },
                            label = { Text(clausula, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(notaPersonalizada) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) {
                Text("ENVIAR WHATSAPP")
            }
        }
    )
}

// Helper para diseño de chips de notas
@Composable
fun FlowRow(
    mainAxisSpacing: androidx.compose.ui.unit.Dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            var yPosition = 0
            var xPosition = 0
            var maxYInRow = 0
            placeables.forEach { placeable ->
                if (xPosition + placeable.width > constraints.maxWidth) {
                    yPosition += maxYInRow + crossAxisSpacing.roundToPx()
                    xPosition = 0
                    maxYInRow = 0
                }
                placeable.placeRelative(xPosition, yPosition)
                xPosition += placeable.width + mainAxisSpacing.roundToPx()
                maxYInRow = maxOf(maxYInRow, placeable.height)
            }
        }
    }
}

@Composable
fun obtenerIconoServicio(nombre: String, categoria: String): androidx.compose.ui.graphics.vector.ImageVector {
    val textoBusqueda = "$nombre $categoria".lowercase()

    return when {
        textoBusqueda.contains("limpieza") || textoBusqueda.contains("profilaxis") -> Icons.Default.AutoAwesome
        textoBusqueda.contains("extraccion") || textoBusqueda.contains("cirugia") || textoBusqueda.contains("cordal") -> Icons.Default.Vaccines // Representa intervención
        textoBusqueda.contains("resina") || textoBusqueda.contains("carilla") || textoBusqueda.contains("estetica") -> Icons.Default.AutoFixHigh
        textoBusqueda.contains("conducto") || textoBusqueda.contains("endodoncia") -> Icons.Default.Medication
        textoBusqueda.contains("ortodoncia") || textoBusqueda.contains("brackets") -> Icons.Default.GridView
        textoBusqueda.contains("protesis") || textoBusqueda.contains("implante") -> Icons.Default.Build
        else -> Icons.Default.MedicalServices // Icono genérico por defecto
    }
}