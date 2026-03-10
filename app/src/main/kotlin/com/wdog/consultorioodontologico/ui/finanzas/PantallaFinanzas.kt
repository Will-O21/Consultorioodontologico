package com.wdog.consultorioodontologico.ui.finanzas

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wdog.consultorioodontologico.entities.Gasto
import com.wdog.consultorioodontologico.viewmodels.FinanzasViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFinanzas(viewModel: FinanzasViewModel) {
    val ingresos by viewModel.ingresosTotales.collectAsState(initial = 0.0)
    val gastosTotales by viewModel.gastosTotales.collectAsState(initial = 0.0)
    val ganancia by viewModel.gananciaNeta.collectAsState(initial = 0.0)
    val listaGastos by viewModel.todosLosGastos.collectAsState(initial = emptyList())

    // --- NUEVOS ESTADOS DE UI ---
    var queryBusqueda by remember { mutableStateOf("") }
    var filtroTemporal by remember { mutableStateOf("Mes") } // Hoy, Semana, Mes, Año
    var mostrarDesgloseIngresos by remember { mutableStateOf(false) }
    var mostrarDialogGasto by remember { mutableStateOf(false) }

    // Simulación de Deuda
    val totalDeudaPacientes by viewModel.totalDeudaPacientes.collectAsState(initial = 0.0)
    val ingresosPorMetodo by viewModel.ingresosPorMetodo.collectAsState(initial = emptyMap())
    val recordatorios by viewModel.recordatorios.collectAsState(initial = emptyList())

    // --- CONFIGURACIÓN DE COLORES INSTITUCIONALES ---
    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorRojoSeleccion = Color(0xFF530E0E)
    val colorFondo = Color(0xFFF8F9FA)

    // --- ESTADOS DE SELECCIÓN Y BORRADO ---
    var seleccionados by remember { mutableStateOf(setOf<Long>()) }
    val modoSeleccionActivo = seleccionados.isNotEmpty()
    var gastoABorrar by remember { mutableStateOf<Gasto?>(null) }
    var mostrarDialogoBorrarVarios by remember { mutableStateOf(false) }

    // --- LÓGICA DE FILTRADO COMBINADA (FECHA + BÚSQUEDA) ---
    // --- LÓGICA DE FILTRADO OPTIMIZADA ---
    val gastosFiltrados by remember(listaGastos, queryBusqueda, filtroTemporal) {
        derivedStateOf {
            listaGastos.filter { gasto ->
                val coincideBusqueda = gasto.concepto.contains(queryBusqueda, ignoreCase = true)

                val ahora = System.currentTimeMillis()
                val calendar = java.util.Calendar.getInstance()

                val coincideFecha = when (filtroTemporal) {
                    "Hoy" -> {
                        val hoy = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(ahora)
                        val fechaGasto = java.text.SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(gasto.fecha)
                        hoy == fechaGasto
                    }
                    "Semana" -> gasto.fecha >= ahora - (7 * 24 * 60 * 60 * 1000L)
                    "Mes" -> {
                        calendar.timeInMillis = ahora
                        val mesActual = calendar.get(java.util.Calendar.MONTH)
                        val anoActual = calendar.get(java.util.Calendar.YEAR)
                        calendar.timeInMillis = gasto.fecha
                        calendar.get(java.util.Calendar.MONTH) == mesActual && calendar.get(java.util.Calendar.YEAR) == anoActual
                    }
                    "Año" -> {
                        calendar.timeInMillis = ahora
                        val anoActual = calendar.get(java.util.Calendar.YEAR)
                        calendar.timeInMillis = gasto.fecha
                        calendar.get(java.util.Calendar.YEAR) == anoActual
                    }
                    else -> true
                }
                coincideBusqueda && coincideFecha
            }
        }
    }

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
                    text = if (modoSeleccionActivo) "${seleccionados.size} Seleccionados" else "Gestión de Finanzas",
                    color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold
                )

                // --- IDEA 1: FILTRO POR RANGO ---
                if (!modoSeleccionActivo) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Hoy", "Semana", "Mes", "Año").forEach { filtro ->
                            FilterChip(
                                selected = filtroTemporal == filtro,
                                onClick = { filtroTemporal = filtro },
                                label = { Text(filtro, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = colorAzulOscuro,
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    labelColor = Color.White
                                ),
                                border = null
                            )
                        }
                    }
                } else {
                    Text("Toca para sumar o 'Atrás' para cancelar", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (modoSeleccionActivo) mostrarDialogoBorrarVarios = true
                    else mostrarDialogGasto = true
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
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(colorFondo)
        ) {
            // --- DASHBOARD DE TARJETAS ---
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- IDEA 4: BUSCADOR ---
                OutlinedTextField(
                    value = queryBusqueda,
                    onValueChange = { queryBusqueda = it },
                    placeholder = { Text("Buscar concepto...") },
                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { if(queryBusqueda.isNotEmpty()) IconButton(onClick = { queryBusqueda = "" }) { Icon(Icons.Default.Close, null) } },
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // --- IDEA 2: DESGLOSE INGRESOS (Clickable) ---
                    TarjetaBalance(
                        titulo = "Ingresos",
                        monto = ingresos,
                        color = Color(0xFF2E7D32),
                        icono = Icons.Default.ArrowUpward,
                        modifier = Modifier.weight(1f).clickable { mostrarDesgloseIngresos = true }
                    )
                    TarjetaBalance(
                        titulo = "Gastos",
                        monto = gastosTotales,
                        color = Color(0xFFC62828),
                        icono = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaBalance(
                        titulo = "Ganancia Neta",
                        monto = ganancia,
                        color = colorAzulOscuro,
                        icono = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f)
                    )
                    // --- IDEA 5: ACCESO DIRECTO DEUDORES ---
                    TarjetaBalance(
                        titulo = "Deuda Pacientes",
                        monto = totalDeudaPacientes,
                        color = Color(0xFFE65100),
                        icono = Icons.Default.PersonSearch,
                        modifier = Modifier.weight(1f)
                    )
                }

                // --- IDEA 3: RECORDATORIO DE PAGOS (Banner) ---
                if (recordatorios.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFE65100))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Pendiente: ${recordatorios.first().concepto} ($${recordatorios.first().monto})",
                                fontSize = 13.sp, color = Color(0xFF5D4037), fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = if(queryBusqueda.isEmpty()) "Historial de Gastos" else "Resultados de búsqueda",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray
            )

            // --- IDEA 7: EMPTY STATE ILUSTRADO ---
            if (gastosFiltrados.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if(queryBusqueda.isEmpty()) Icons.AutoMirrored.Filled.ListAlt else Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.LightGray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if(queryBusqueda.isEmpty()) "No hay gastos registrados en este periodo" else "No se encontraron resultados para '$queryBusqueda'",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(gastosFiltrados) { gasto ->
                        ItemGasto(
                            gasto = gasto,
                            estaSeleccionado = seleccionados.contains(gasto.id),
                            modoSeleccionActivo = modoSeleccionActivo,
                            onLongClick = { if (!modoSeleccionActivo) seleccionados = seleccionados + gasto.id },
                            onClick = {
                                if (modoSeleccionActivo) {
                                    seleccionados = if (seleccionados.contains(gasto.id)) seleccionados - gasto.id else seleccionados + gasto.id
                                }
                            },
                            onDelete = { gastoABorrar = gasto }
                        )
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---

    if (mostrarDesgloseIngresos) {
        AlertDialog(
            onDismissRequest = { mostrarDesgloseIngresos = false },
            title = { Text("Cuadre de Caja - Desglose", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DesgloseFila("Efectivo", ingresosPorMetodo["Efectivo"] ?: 0.0, Color(0xFF2E7D32))
                    DesgloseFila("Binance", ingresosPorMetodo["Binance"] ?: 0.0, Color(0xFFF3BA2F))
                    DesgloseFila("Transferencia Bs.", ingresosPorMetodo["Transferencia Bs"] ?: 0.0, Color(0xFF101084))
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Recibido:", fontWeight = FontWeight.Bold)
                        Text("$${String.format(Locale.US, "%.2f", ingresos)}", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarDesgloseIngresos = false }) { Text("Cerrar") } }
        )
    }

    if (mostrarDialogGasto) {
        DialogAgregarGasto(
            onDismiss = { mostrarDialogGasto = false },
            onConfirm = { concepto, monto, cat, esProximo ->
                viewModel.agregarGasto(concepto, monto, cat, esProximo)
                mostrarDialogGasto = false
            }
        )
    }

    if (gastoABorrar != null) {
        AlertDialog(
            onDismissRequest = { gastoABorrar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas borrar '${gastoABorrar?.concepto}'? \n\nEsta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { gastoABorrar?.let { viewModel.eliminarGasto(it) }; gastoABorrar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { gastoABorrar = null }) { Text("Cancelar") } }
        )
    }

    if (mostrarDialogoBorrarVarios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrarVarios = false },
            title = { Text("Eliminar Seleccionados", fontWeight = FontWeight.Bold) },
            text = { Text("¿Deseas eliminar los ${seleccionados.size} registros?\n\nEsta acción es permanente.") },
            confirmButton = {
                TextButton(onClick = {
                    seleccionados.forEach { id -> listaGastos.find { it.id == id }?.let { viewModel.eliminarGasto(it) } }
                    seleccionados = emptySet()
                    mostrarDialogoBorrarVarios = false
                }) { Text("BORRAR", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoBorrarVarios = false }) { Text("CANCELAR") } }
        )
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun DesgloseFila(metodo: String, monto: Double, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(8.dp))
            Text(metodo, fontSize = 14.sp)
        }
        Text("$${String.format(Locale.US, "%.2f", monto)}", fontWeight = FontWeight.Bold)
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TarjetaBalance(titulo: String, monto: Double, color: Color, icono: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(titulo, fontSize = 10.sp, color = Color.Gray, lineHeight = 12.sp)
                Text("$${String.format(Locale.US, "%.2f", monto)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("DefaultLocale")
@Composable
fun ItemGasto(
    gasto: Gasto,
    estaSeleccionado: Boolean,
    modoSeleccionActivo: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val colorRojoVino = Color(0xFF530E0E)
    val interactionSource = remember { MutableInteractionSource() }

    // --- IDEA 6: COLORES SEMAFÓRICOS ---
    val colorMonto = when {
        gasto.monto >= 501 -> Color(0xFFB71C1C) // Muy Alto - Rojo Oscuro
        gasto.monto >= 301 -> Color(0xFFD32F2F) // Alto - Rojo
        gasto.monto >= 101 -> Color(0xFFF57C00) // Medio - Naranja
        else -> Color(0xFF388E3C)               // Bajo - Verde
    }

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
                onClick = onClick,
                onLongClick = onLongClick,
                indication = ripple(),
                interactionSource = interactionSource
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(if (estaSeleccionado) 0.dp else 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(45.dp).clip(CircleShape)
                    .background(if (estaSeleccionado) colorRojoVino else colorMonto.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (estaSeleccionado) Icons.Default.Check else Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = if (estaSeleccionado) Color.White else colorMonto
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(gasto.concepto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(gasto.categoria, fontSize = 12.sp, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "-$${String.format(Locale.US, "%.2f", gasto.monto)}",
                    color = colorMonto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                // Simulación de fecha
                val fechaFormateada = java.text.SimpleDateFormat("dd MMM", Locale.getDefault()).format(gasto.fecha)
                Text(fechaFormateada, fontSize = 10.sp, color = Color.LightGray)
            }

            if (!modoSeleccionActivo) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun DialogAgregarGasto(onDismiss: () -> Unit, onConfirm: (String, Double, String, Boolean) -> Unit) {
    var concepto by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("Materiales") }
    var esGastoProximo by remember { mutableStateOf(false) } // Para Idea Recordatorios
    val categorias = listOf("Materiales", "Renta", "Servicios", "Otros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = concepto, onValueChange = { concepto = it }, label = { Text("Concepto") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = monto, onValueChange = { monto = it }, label = { Text("Monto ($)") }, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { esGastoProximo = !esGastoProximo }) {
                    Checkbox(checked = esGastoProximo, onCheckedChange = { esGastoProximo = it })
                    Text("Es un gasto próximo (Recordatorio)", fontSize = 14.sp)
                }

                Text("Categoría:", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categorias.forEach { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val m = monto.toDoubleOrNull() ?: 0.0
                if (concepto.isNotBlank() && m > 0) onConfirm(concepto, m, categoria, esGastoProximo)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}