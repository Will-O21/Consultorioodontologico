package com.wdog.consultorioodontologico.ui.gestionconsultorio

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wdog.consultorioodontologico.entities.Nota
import com.wdog.consultorioodontologico.ui.components.EstadoVacioConsultorio
import com.wdog.consultorioodontologico.viewmodels.NotaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaGestionConsultorio(viewModel: NotaViewModel) {
    val colorAzulOscuro = Color(0xFF101084)
    val colorRojoSeleccion = Color(0xFF530E0E)
    val colorFondo = Color(0xFFF8F9FA)

    var showDialog by remember { mutableStateOf(false) }
    var notaSeleccionada by remember { mutableStateOf<Nota?>(null) }
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pendientes", "Historial")
    val categoriasFiltro = listOf("Todas", "Mantenimiento", "Compras", "Inventario")
    var categoriaSeleccionada by remember { mutableStateOf("Todas") }
    // MODO SELECCIÓN
    var seleccionados by remember { mutableStateOf(setOf<Int>()) }
    val modoSeleccionActivo = seleccionados.isNotEmpty()
    var mostrarDialogoBorrarVarios by remember { mutableStateOf(false) }

    BackHandler(enabled = modoSeleccionActivo) {
        seleccionados = emptySet()
    }

    val cerrarDialogo = {
        showDialog = false
        notaSeleccionada = null
    }

    val notasRaw by viewModel.obtenerNotasLocales().collectAsState(initial = emptyList())
    val notasMostrar = remember(notasRaw, tabIndex, categoriaSeleccionada) {
        val porEstado = if (tabIndex == 0) {
            notasRaw.filter { it.isListo == false }
        } else {
            notasRaw.filter { it.isListo == true }
        }

        val filtradas = if (categoriaSeleccionada == "Todas") {
            porEstado
        } else {
            porEstado.filter { it.categoria == categoriaSeleccionada }
        }

        // CAMBIO AQUÍ: Primero colorTitulo (Prioridad) y luego categoría
        filtradas.sortedWith(compareBy({ it.colorTitulo }, { it.categoria }))
    }




    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(if (modoSeleccionActivo) colorRojoSeleccion else colorAzulOscuro)
                    .padding(top = 48.dp, bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (modoSeleccionActivo) "${seleccionados.size} Seleccionados" else "Mis Pendientes",
                    color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (modoSeleccionActivo) "Toca para sumar o 'Atrás' para cancelar" else "Gestión del Consultorio",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!modoSeleccionActivo) {
                    SecondaryTabRow(
                        selectedTabIndex = tabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabIndex),
                                color = Color.White
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = tabIndex == index,
                                onClick = { tabIndex = index },
                                text = { Text(title, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (modoSeleccionActivo) mostrarDialogoBorrarVarios = true
                    else {
                        notaSeleccionada = null
                        showDialog = true
                    }
                },
                containerColor = if (modoSeleccionActivo) colorRojoSeleccion else Color(0xFF25D366),
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp).size(56.dp)
            ) {
                Icon(
                    imageVector = if (modoSeleccionActivo) Icons.Default.Delete else Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(colorFondo).padding(padding)) {
            if (!modoSeleccionActivo) {
                // Definimos el estado del scroll
                val scrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(scrollState), // Habilita el deslizamiento
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.width(16.dp)) // Margen inicial

                    categoriasFiltro.forEachIndexed { index, cat ->
                        FilterChip(
                            selected = categoriaSeleccionada == cat,
                            onClick = { categoriaSeleccionada = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorAzulOscuro,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Margen final
                }
            }
            if (notasMostrar.isEmpty()) {
                EstadoVacioConsultorio(
                    icono = if (tabIndex == 0) Icons.Default.EditNote else Icons.Default.History,
                    mensaje = if (tabIndex == 0) "Todo al día" else "No hay historial aún",
                    colorPersonalizado = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notasMostrar, key = { it.id }) { nota ->
                        val estaSeleccionado = seleccionados.contains(nota.id)
                        NotaCard(
                            nota = nota,
                            estaSeleccionado = estaSeleccionado,
                            modoSeleccionActivo = modoSeleccionActivo,
                            onToggleCheck = { index, isChecked -> viewModel.toggleChecklistItem(nota, index, isChecked) },
                            onClick = {
                                if (modoSeleccionActivo) {
                                    seleccionados = if (estaSeleccionado) seleccionados - nota.id else seleccionados + nota.id
                                } else {
                                    notaSeleccionada = nota
                                    showDialog = true
                                }
                            },
                            onLongClick = {
                                if (!modoSeleccionActivo) seleccionados = seleccionados + nota.id
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = cerrarDialogo, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            NotaDialog(
                nota = notaSeleccionada,
                onSave = { nuevaNota ->
                    if (notaSeleccionada == null) viewModel.insertarNota(nuevaNota)
                    else viewModel.actualizarNota(nuevaNota)
                    cerrarDialogo()
                },
                onCancel = cerrarDialogo
            )
        }
    }

    if (mostrarDialogoBorrarVarios) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrarVarios = false },
            title = { Text("Eliminar Seleccionados") },
            text = { Text("¿Eliminar las ${seleccionados.size} notas seleccionadas?") },
            confirmButton = {
                TextButton(onClick = {
                    val notasABorrar = notasRaw.filter { seleccionados.contains(it.id) }
                    viewModel.eliminarMultiplesNotas(notasABorrar)
                    seleccionados = emptySet()
                    mostrarDialogoBorrarVarios = false
                }) { Text("BORRAR", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoBorrarVarios = false }) { Text("CANCELAR") } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotaCard(
    nota: Nota,
    estaSeleccionado: Boolean,
    modoSeleccionActivo: Boolean,
    onToggleCheck: (Int, Boolean) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val checklist = nota.obtenerChecklist()
    val colorRojoVino = Color(0xFF530E0E)
    val interactionSource = remember { MutableInteractionSource() }

    val fondoCard = if (nota.categoria == "Inventario" && !nota.isListo) {
        Color(0xFFE3F2FD) // Un azul muy clarito "Sky Blue"
    } else {
        Color.White
    }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (estaSeleccionado) 3.dp else 0.dp,
                color = if (estaSeleccionado) colorRojoVino else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = ripple(), // Cambio puntual para evitar el crash
                interactionSource = interactionSource
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = fondoCard),
        elevation = CardDefaults.elevatedCardElevation(if (estaSeleccionado) 0.dp else 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = when {
                            nota.isListo -> Icons.Default.CheckCircle
                            nota.categoria == "Inventario" -> Icons.Default.ShoppingCart // El carrito que pediste
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = Color(nota.colorTitulo),
                        modifier = Modifier.size(32.dp)
                    )
                    if (estaSeleccionado) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(colorRojoVino.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = nota.titulo, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                    if (nota.isListo && nota.fechaCompletado > 0) {
                        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale("es", "ES"))
                        Text("Cerrado: ${sdf.format(Date(nota.fechaCompletado))}", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F0F0),
                    modifier = Modifier.padding(start = 8.dp).widthIn(max = 100.dp) // Limitamos ancho para proteger el diseño
                ) {
                    Text(
                        text = nota.categoria,
                        fontSize = 10.sp, // Bajamos a 10sp para que "Mantenimiento" quepa bien
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.DarkGray,
                        maxLines = 1 // Evita que salte a una segunda línea
                    )
                }
            }

            if (checklist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                checklist.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Checkbox(
                            checked = item.second,
                            onCheckedChange = { if (!modoSeleccionActivo) onToggleCheck(index, it) },
                            enabled = !modoSeleccionActivo,
                            colors = CheckboxDefaults.colors(checkedColor = Color(nota.colorTitulo))
                        )
                        Text(
                            text = item.first,
                            fontSize = 14.sp,
                            color = if (item.second) Color.Gray else Color.Black,
                            textDecoration = if (item.second) TextDecoration.LineThrough else null
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaDialog(nota: Nota?, onSave: (Nota) -> Unit, onCancel: () -> Unit) {
    var titulo by remember { mutableStateOf(nota?.titulo ?: "") }
    var colorTitulo by remember { mutableIntStateOf(nota?.colorTitulo ?: Color(0xFFa51b0b).toArgb()) }
    var categoria by remember { mutableStateOf(nota?.categoria ?: "Mantenimiento") }
    var isListo by remember { mutableStateOf(nota?.isListo ?: false) }
    var checklist by remember { mutableStateOf(nota?.obtenerChecklist() ?: emptyList()) }
    var expPrioridad by remember { mutableStateOf(false) }
    var expCategoria by remember { mutableStateOf(false) }

    val colorAzul = Color(0xFF101084)

    Surface(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f), shape = RoundedCornerShape(28.dp), color = Color.White) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
            Text("Detalle del Pendiente", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colorAzul)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Asunto") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                if (nota != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Listo?", fontSize = 10.sp)
                        Switch(checked = isListo, onCheckedChange = { isListo = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Le damos más peso a Categoría (1.2f) para que quepa "Mantenimiento"
                Box(modifier = Modifier.weight(1.0f)) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        // Reducimos un poco la fuente del texto interno
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expCategoria) }
                    )
                    Box(Modifier.matchParentSize().clickable(
                        indication = ripple(),
                        interactionSource = remember { MutableInteractionSource() }
                    ) { expCategoria = true })

                    DropdownMenu(expanded = expCategoria, onDismissRequest = { expCategoria = false }) {
                        // Eliminamos "Administrativo" y dejamos solo tus 3 originales
                        listOf("Mantenimiento", "Compras", "Inventario").forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, fontSize = 14.sp) },
                                onClick = { categoria = cat; expCategoria = false }
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(0.8f)) {
                    val prioridadActual = when(colorTitulo) {
                        Color(0xFFa51b0b).toArgb() -> "Urgente"
                        else -> "Prioridad" // Ahora prioridad es el estado base si no es urgente
                    }
                    OutlinedTextField(
                        value = prioridadActual,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prioridad", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expPrioridad) }
                    )
                    Box(Modifier.matchParentSize().clickable(
                        indication = ripple(),
                        interactionSource = remember { MutableInteractionSource() }
                    ) { expPrioridad = true })

                    DropdownMenu(expanded = expPrioridad, onDismissRequest = { expPrioridad = false }) {
                        // Solo dejamos las dos opciones que necesitas
                        listOf("Urgente" to Color(0xFFa51b0b), "Prioridad" to Color(0xFFFBC02D)).forEach { (nombre, color) ->
                            DropdownMenuItem(
                                text = { Text(nombre, fontSize = 14.sp) },
                                onClick = {
                                    colorTitulo = color.toArgb()
                                    expPrioridad = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Checklist de Tareas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                TextButton(onClick = { checklist = checklist + Pair("", false) }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(checklist.size) { index ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Checkbox(checked = checklist[index].second, onCheckedChange = { isChecked ->
                            val newList = checklist.toMutableList()
                            newList[index] = newList[index].copy(second = isChecked)
                            checklist = newList
                        })
                        OutlinedTextField(
                            value = checklist[index].first,
                            onValueChange = { newText ->
                                val newList = checklist.toMutableList()
                                newList[index] = newList[index].copy(first = newText)
                                checklist = newList
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(onClick = {
                            val newList = checklist.toMutableList()
                            newList.removeAt(index)
                            checklist = newList
                        }) { Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("CANCELAR", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (titulo.isNotBlank()) {
                            val checklistLimpio = checklist.filter { it.first.isNotBlank() }

                            // LÓGICA DE PERSISTENCIA CORREGIDA
                            val fechaCierre = if (isListo) {
                                // Si ya tenía fecha de cierre, la dejamos, si no, ponemos ahora.
                                if (nota?.fechaCompletado != null && nota.fechaCompletado > 0L) nota.fechaCompletado
                                else System.currentTimeMillis()
                            } else {
                                0L // Si no está listo, la fecha es 0
                            }

                            val base = nota ?: Nota(titulo = titulo, colorTitulo = colorTitulo, categoria = categoria)
                            val aGuardar = base.copy(
                                titulo = titulo,
                                colorTitulo = colorTitulo,
                                categoria = categoria,
                                isListo = isListo,
                                fechaCompletado = fechaCierre
                            ).copyWithChecklist(checklistLimpio)

                            onSave(aGuardar)
                        }
                    },
                    modifier = Modifier.weight(1.5f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorAzul)
                ) {
                    Text("GUARDAR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}