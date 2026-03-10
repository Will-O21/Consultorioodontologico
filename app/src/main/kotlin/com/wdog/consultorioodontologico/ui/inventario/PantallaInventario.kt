package com.wdog.consultorioodontologico.ui.inventario

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wdog.consultorioodontologico.entities.MaterialInventario
import com.wdog.consultorioodontologico.entities.KitProcedimiento
import com.wdog.consultorioodontologico.viewmodels.InventarioViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import com.wdog.consultorioodontologico.ui.components.EstadoVacioConsultorio

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaInventarioPrincipal(viewModel: InventarioViewModel) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // Cerrar teclado automáticamente cuando el usuario cambia de pestaña
    LaunchedEffect(pagerState.currentPage) {
        focusManager.clearFocus()
    }
    val colorAzul = Color(0xFF101084)

    val materiales by viewModel.materiales.collectAsState(initial = emptyList())
    val seleccionados by viewModel.seleccionados.collectAsState()
    val modoSeleccionActivo = seleccionados.isNotEmpty()

    val hoy = System.currentTimeMillis()
    val quinceDias = 15L * 24 * 60 * 60 * 1000

    // Si hay AL MENOS UNO vencido, la alerta es Roja (Prioridad máxima)
    val hayVencidos = materiales.any { it.fechaVencimiento != null && it.fechaVencimiento <= hoy }

    // Si no hay vencidos, pero hay stock bajo o próximos a vencer, la alerta es Naranja
    val hayStockBajoOProxVencer = materiales.any { mat ->
        val esStockCritico = mat.cantidadActual <= mat.cantidadMinima
        val proximoAVencer = mat.fechaVencimiento != null && (mat.fechaVencimiento - hoy) <= quinceDias && mat.fechaVencimiento > hoy
        esStockCritico || proximoAVencer
    }

    var mostrarDialogoOpciones by remember { mutableStateOf(false) }
    var mostrarDialogoNuevoMaterial by remember { mutableStateOf(false) }
    var mostrarDialogoNuevoKit by remember { mutableStateOf(false) }

    var mostrarDialogoBorrarVarios by remember { mutableStateOf(false) }
    var materialAEditar by remember { mutableStateOf<MaterialInventario?>(null) }
    var kitAEditar by remember { mutableStateOf<KitProcedimiento?>(null) }

    BackHandler(enabled = modoSeleccionActivo) {
        viewModel.limpiarSeleccion()
    }


    val colorRojoSeleccion = Color(0xFF530E0E) // Tu color de Pacientes

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(if (modoSeleccionActivo) colorRojoSeleccion else colorAzul).padding(top = 40.dp)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (modoSeleccionActivo) "${seleccionados.size} Seleccionados" else "Control de Insumos",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                            color = Color.White
                        )
                    }
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("CONSUMO", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        text = {
                            BadgedBox(
                                badge = {
                                    if (hayVencidos) {
                                        // Rojo: Peligro/Vencido
                                        Badge(containerColor = Color.Red, modifier = Modifier.offset(x = 8.dp, y = (-4).dp))
                                    } else if (hayStockBajoOProxVencer) {
                                        // Naranja: Advertencia/Stock Bajo
                                        Badge(containerColor = Color(0xFFFF8C00), modifier = Modifier.offset(x = 8.dp, y = (-4).dp))
                                    }
                                }
                            ) {
                                Text("INVENTARIO", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (modoSeleccionActivo) mostrarDialogoBorrarVarios = true
                    else mostrarDialogoOpciones = true
                },
                containerColor = if (modoSeleccionActivo) Color(0xFF530E0E) else Color(0xFF25D366),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (modoSeleccionActivo) Icons.Default.Delete else Icons.Default.Add,
                    contentDescription = null
                )
            }
        }

    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) { page ->
            if (page == 0) {
                VistaConsumo(viewModel)
            } else {
                VistaInventario(
                    viewModel = viewModel,
                    onEditarMaterial = { materialAEditar = it },
                    onEditarKit = { kitAEditar = it } // Le pasamos la lógica para abrir el diálogo
                )
            }
        }
    }

    // --- DIÁLOGOS DE CONTROL ---
    if (mostrarDialogoOpciones) {
        Dialog(onDismissRequest = { mostrarDialogoOpciones = false }) {
            Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Nuevo Registro", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { mostrarDialogoNuevoMaterial = true; mostrarDialogoOpciones = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAzul)
                    ) { Text("1. NUEVO MATERIAL") }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { mostrarDialogoNuevoKit = true; mostrarDialogoOpciones = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) { Text("2. KIT DE PROCEDIMIENTO") }
                }
            }
        }
    }

    if (mostrarDialogoNuevoMaterial) {
        DialogNuevoMaterial(
            onDismiss = { mostrarDialogoNuevoMaterial = false },
            onSave = { mat -> viewModel.agregarMaterial(mat); mostrarDialogoNuevoMaterial = false }
        )
    }

    if (mostrarDialogoNuevoKit) {
        DialogNuevoKit(
            materialesDisponibles = materiales,
            onDismiss = { mostrarDialogoNuevoKit = false },
            onSave = { nombre: String, composicion: Map<Long, Double> ->
                viewModel.agregarKit(nombre, composicion)
                mostrarDialogoNuevoKit = false
            }
        )
    }
    if (mostrarDialogoBorrarVarios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrarVarios = false },
            title = { Text("Eliminar Insumos", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar los ${seleccionados.size} materiales seleccionados?\n\nEsta acción es permanente.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarMaterialesSeleccionados()
                    mostrarDialogoBorrarVarios = false
                }) { Text("BORRAR", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrarVarios = false }) { Text("CANCELAR") }
            }
        )
    }
    materialAEditar?.let { mat ->
        DialogEditarStock(
            material = mat,
            onDismiss = { materialAEditar = null },
            onConfirm = { nuevaCantidad ->
                viewModel.actualizarStockManual(mat, nuevaCantidad) // Usamos tu función real
                materialAEditar = null
            }
        )
    }

    kitAEditar?.let { kit ->
        DialogNuevoKit(
            materialesDisponibles = materiales,
            nombreInicial = kit.nombreKit,
            seleccionInicial = kit.composicion,
            onDismiss = { kitAEditar = null },
            onSave = { nombre, composicion ->
                // Pasamos el kit.id para que sepa que es una actualización
                viewModel.agregarKit(nombre, composicion, idExistente = kit.id)
                kitAEditar = null
            }
        )
    }
}

@Composable
fun VistaConsumo(viewModel: InventarioViewModel) {
    val materiales by viewModel.materiales.collectAsState(initial = emptyList())
    val kits by viewModel.kits.collectAsState(initial = emptyList())
    val consumo by viewModel.consumoActual.collectAsState()

    var busqueda by remember { mutableStateOf("") }
    var catSel by remember { mutableStateOf("Bioseguridad") }
    val categorias = listOf("Bioseguridad", "Restauración", "Desechables", "Endodoncia", "Instrumental", "Impresión", "Insumos", "Otros", "Kits")

    Column(Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        SearchBarInventario(busqueda) { busqueda = it }
        FiltroCategoriasHorizontal(categorias, catSel) { catSel = it }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (catSel == "Kits") {
                val kitsFiltrados = kits.filter { it.nombreKit.contains(busqueda, true) }.sortedBy { it.nombreKit }
                items(kitsFiltrados) { kit ->
                    ItemKitConsumoCard(kit) { viewModel.agregarKitAlConsumo(kit) }
                }
            } else {
                val matFiltrados = materiales.filter {
                    it.categoria == catSel && it.nombre.contains(busqueda, true)
                }.sortedBy { it.nombre }

                items(matFiltrados) { mat ->
                    ItemConsumoCard(mat, consumo[mat.id] ?: 0.0) { delta ->
                        viewModel.ajustarConsumoTemporal(mat.id, delta)
                    }
                }
            }
        }

        AnimatedVisibility(visible = consumo.isNotEmpty()) {
            BarraAccionConsumo(
                itemCount = consumo.size,
                onCancelar = { viewModel.limpiarConsumo() },
                onConfirmar = { viewModel.procesarConsumoRealizado() }
            )
        }
    }
}

@Composable
fun VistaInventario(viewModel: InventarioViewModel, onEditarKit: (KitProcedimiento) -> Unit,onEditarMaterial: (MaterialInventario) -> Unit) {
    val materiales by viewModel.materiales.collectAsState(initial = emptyList())
    val kits by viewModel.kits.collectAsState(initial = emptyList()) // Necesitamos los kits aquí también
    var busqueda by remember { mutableStateOf("") }
    var catSel by remember { mutableStateOf("Bioseguridad") }
    val categorias = listOf("Bioseguridad", "Restauración", "Desechables", "Endodoncia", "Instrumental", "Impresión", "Insumos", "Otros", "Kits")

    Column(Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        SearchBarInventario(busqueda) { busqueda = it }
        FiltroCategoriasHorizontal(categorias, catSel) { catSel = it }

        val seleccionados by viewModel.seleccionados.collectAsState()
        val modoSeleccionActivo = seleccionados.isNotEmpty()

        // --- Lógica de Resumen y Filtrado ---
        val filtrados = if (catSel == "Kits") {
            kits.filter { it.nombreKit.contains(busqueda, true) }.sortedBy { it.nombreKit }
        } else {
            materiales.filter { it.categoria == catSel && it.nombre.contains(busqueda, true) }.sortedBy { it.nombre }
        }

        // Resumen de cantidades
        if (filtrados.isNotEmpty() && !modoSeleccionActivo) {
            Text(
                text = "${filtrados.size} ${if (catSel == "Kits") "kits registrados" else "insumos en $catSel"}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }

        if (filtrados.isEmpty()) {
            // Uso de tu componente EstadoVacioConsultorio
            EstadoVacioConsultorio(
                icono = androidx.compose.material.icons.Icons.Default.Inventory2,
                mensaje = if (busqueda.isEmpty()) "No hay elementos en $catSel" else "No se hallaron resultados",
                colorPersonalizado = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (catSel == "Kits") {
                    items(filtrados) { item ->
                        val kit = item as KitProcedimiento
                        ItemKitConsumoCard(kit) {
                            onEditarKit(kit)
                        }
                    } // Cierre correcto de items para Kits
                } else {
                    items(filtrados, key = { (it as MaterialInventario).id }) { item ->
                        val mat = item as MaterialInventario
                        val estaSeleccionado = seleccionados.contains(mat.id)
                        CardInventario(
                            material = mat,
                            estaSeleccionado = estaSeleccionado,
                            onClick = {
                                if (modoSeleccionActivo) viewModel.alternarSeleccion(mat.id)
                                else { onEditarMaterial(mat) }
                            },
                            onLongClick = {
                                if (!modoSeleccionActivo) viewModel.alternarSeleccion(mat.id)
                            }
                        )
                    } // Cierre correcto de items para Materiales
                }
            }
        }
    }
}
// --- COMPONENTES DE APOYO ---

@Composable
fun SearchBarInventario(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = { Text("Buscar materiales o kits...", fontSize = 14.sp, color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF101084)) },
        trailingIcon = {
            if(query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, null, tint = Color.Gray)
                }
            }
        },
        shape = RoundedCornerShape(16.dp), // Menos circular, más estilo "Card" moderno
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF101084),
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            cursorColor = Color(0xFF101084)
        )
    )
}

@Composable
fun FiltroCategoriasHorizontal(categorias: List<String>, seleccionada: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categorias.forEach { cat ->
            FilterChip(
                selected = seleccionada == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat) },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogNuevoMaterial(onDismiss: () -> Unit, onSave: (MaterialInventario) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Bioseguridad") }
    var cantidad by remember { mutableStateOf("") }
    var minima by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("Unidades") }
    var fechaVencimiento by remember { mutableStateOf<Long?>(null) }

    var expCat by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Configuración del DatePicker: Solo fechas futuras
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000 // Hoy en adelante
            }
        }
    )

    val categorias = listOf("Bioseguridad", "Restauración", "Desechables", "Endodoncia", "Instrumental", "Impresión", "Insumos", "Otros", "Kits")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isNotBlank() && cantidad.isNotBlank()) {
                    onSave(MaterialInventario(
                        nombre = nombre,
                        categoria = categoria,
                        cantidadActual = cantidad.toDoubleOrNull() ?: 0.0,
                        cantidadMinima = minima.toDoubleOrNull() ?: 0.0,
                        unidad = unidad,
                        fechaVencimiento = fechaVencimiento
                    ))
                }
            }) { Text("GUARDAR", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
        title = { Text("Nuevo Material", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del Material") }, modifier = Modifier.fillMaxWidth())

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = categoria, onValueChange = {}, readOnly = true, label = { Text("Categoría") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expCat) }
                    )
                    Box(Modifier.matchParentSize().clickable { expCat = true })
                    DropdownMenu(expanded = expCat, onDismissRequest = { expCat = false }) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expCat = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Stock Actual") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = minima, onValueChange = { minima = it }, label = { Text("Mínimo") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad (ml, Cajas, etc)") }, modifier = Modifier.fillMaxWidth())

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (fechaVencimiento == null) "Fecha Vencimiento (Opcional)"
                    else "Vence: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaVencimiento!!))}")
                }
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Sumamos compensación de zona horaria para evitar que reste un día
                    fechaVencimiento = datePickerState.selectedDateMillis?.plus(86400000)
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardInventario(
    material: MaterialInventario,
    estaSeleccionado: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val hoy = System.currentTimeMillis()
    val quinceDiasEnMillis = 15L * 24 * 60 * 60 * 1000

    val esStockCritico = material.cantidadActual <= material.cantidadMinima
    val estaVencido = material.fechaVencimiento != null && material.fechaVencimiento <= hoy
    val proximoAVencer = material.fechaVencimiento != null &&
            (material.fechaVencimiento - hoy) <= quinceDiasEnMillis && !estaVencido

    val colorAlerta = when {
        estaVencido -> Color(0xFFB00020)
        proximoAVencer -> Color(0xFFFF8C00)
        esStockCritico -> Color.Red
        else -> Color(0xFF155E29)
    }

    val colorRojoVino = Color(0xFF530E0E)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                width = if (estaSeleccionado) 3.dp else 0.dp,
                color = if (estaSeleccionado) colorRojoVino else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Si está seleccionado, mostramos el circulito con el check igual que en pacientes
            if (estaSeleccionado) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colorRojoVino),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(12.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(material.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Categoría: ${material.categoria}", fontSize = 12.sp, color = Color.Gray)
                material.fechaVencimiento?.let {
                    Text(
                        "Vence: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))}",
                        fontSize = 11.sp,
                        color = if (proximoAVencer || estaVencido) colorAlerta else Color.Gray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${material.cantidadActual} ${material.unidad}",
                    color = colorAlerta,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                if (proximoAVencer) Text("VENCE PRONTO", color = colorAlerta, fontSize = 9.sp, fontWeight = FontWeight.Black)
                if (estaVencido) Text("VENCIDO", color = colorAlerta, fontSize = 9.sp, fontWeight = FontWeight.Black)
                if (esStockCritico && !estaVencido && !proximoAVencer) Text("STOCK BAJO", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ItemConsumoCard(material: MaterialInventario, cantidad: Double, onDelta: (Double) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(material.nombre, fontWeight = FontWeight.Bold)
                Text("Stock: ${material.cantidadActual}", fontSize = 11.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onDelta(-1.0) }) { Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Red) }
                Text(cantidad.toInt().toString(), fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp))
                IconButton(onClick = { onDelta(1.0) }) { Icon(Icons.Default.AddCircleOutline, null, tint = Color(0xFF101084)) }
            }
        }
    }
}

@Composable
fun ItemKitConsumoCard(kit: KitProcedimiento, onAdd: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onAdd() },
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Inventory, null, tint = Color(0xFF101084))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(kit.nombreKit, fontWeight = FontWeight.Bold, color = Color(0xFF101084))
                Text("Kit de procedimiento", fontSize = 11.sp)
            }
            Icon(Icons.Default.AddCircle, null, tint = Color(0xFF101084))
        }
    }
}

@Composable
fun BarraAccionConsumo(itemCount: Int, onCancelar: () -> Unit, onConfirmar: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Insumos a descontar: $itemCount", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) { Text("CANCELAR", color = Color.Red) }
                Button(onClick = onConfirmar, modifier = Modifier.weight(1.5f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101084))) {
                    Text("CONFIRMAR CONSUMO")
                }
            }
        }
    }
}
@Composable
fun DialogNuevoKit(
    materialesDisponibles: List<MaterialInventario>,
    nombreInicial: String = "",           // Nuevo parámetro con valor por defecto
    seleccionInicial: Map<Long, Double> = emptyMap(), // Nuevo parámetro
    onDismiss: () -> Unit,
    onSave: (String, Map<Long, Double>) -> Unit
) {
    var nombreKit by remember { mutableStateOf(nombreInicial) }
    val seleccionados = remember {
        mutableStateMapOf<Long, Double>().apply {
            putAll(seleccionInicial)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (nombreKit.isNotBlank() && seleccionados.isNotEmpty()) {
                    onSave(nombreKit, seleccionados.toMap())
                }
            }) { Text(
                text = if (nombreInicial.isEmpty()) "Crear kit" else "Guardar Cambios",
                fontWeight = FontWeight.Bold
            )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } },
        title = { Text("Configurar Nuevo Kit", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = nombreKit,
                    onValueChange = { nombreKit = it },
                    label = { Text("Nombre del Kit (Ej: Resina)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Selecciona materiales y cantidad:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(materialesDisponibles) { mat ->
                        val cant = seleccionados[mat.id] ?: 0.0
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Checkbox(
                                checked = cant > 0,
                                onCheckedChange = { if (it) seleccionados[mat.id] = 1.0 else seleccionados.remove(mat.id) }
                            )
                            Text(mat.nombre, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            if (cant > 0) {
                                OutlinedTextField(
                                    value = if (cant % 1.0 == 0.0) cant.toInt().toString() else cant.toString(),
                                    onValueChange = { newVal ->
                                        val d = newVal.toDoubleOrNull() ?: 0.0
                                        seleccionados[mat.id] = d
                                    },
                                    modifier = Modifier.width(70.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
@Composable
fun DialogEditarStock(
    material: MaterialInventario,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    // Lógica para mostrar 5 en lugar de 5.0 si es entero
    val stockInicial = if (material.cantidadActual % 1.0 == 0.0)
        material.cantidadActual.toInt().toString()
    else material.cantidadActual.toString()

    var cantidad by remember { mutableStateOf(stockInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Stock: ${material.nombre}", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Unidad: ${material.unidad}", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón Menos (Validado)
                    IconButton(onClick = {
                        val actual = cantidad.toDoubleOrNull() ?: 0.0
                        if (actual >= 1.0) {
                            val nuevo = actual - 1.0
                            cantidad = if (nuevo % 1.0 == 0.0) nuevo.toInt().toString() else nuevo.toString()
                        } else {
                            cantidad = "0"
                        }
                    }) {
                        Icon(Icons.Default.RemoveCircle, null, tint = Color.Red, modifier = Modifier.size(36.dp))
                    }

                    // Campo de Texto (Permite cambio manual por teclado)
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { newValue ->
                            // Solo permite números y un punto decimal
                            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null || newValue == ".") {
                                cantidad = newValue
                            }
                        },
                        modifier = Modifier.width(110.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color(0xFF101084)
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Botón Más
                    IconButton(onClick = {
                        val actual = cantidad.toDoubleOrNull() ?: 0.0
                        val nuevo = actual + 1.0
                        cantidad = if (nuevo % 1.0 == 0.0) nuevo.toInt().toString() else nuevo.toString()
                    }) {
                        Icon(Icons.Default.AddCircle, null, tint = Color(0xFF101084), modifier = Modifier.size(36.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(cantidad.toDoubleOrNull() ?: material.cantidadActual) }) {
                Text("ACTUALIZAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR", color = Color.Gray) }
        }
    )
}