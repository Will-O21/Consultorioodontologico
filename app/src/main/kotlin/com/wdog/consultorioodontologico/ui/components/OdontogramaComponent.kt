package com.wdog.consultorioodontologico.ui.components // Ajusta según tu estructura

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Enum para manejar los estados especiales del diente
enum class EstadoDiente {
    NORMAL, AUSENTE, CORONA, ENDODONCIA, IMPLANTE, ORTODONCIA, FRACTURA, CUELLO, PROTESIS_FIJA
}

@Composable
fun OdontogramaInteractivo(
    dientesSeleccionados: Map<Int, Set<Int>>,
    estadosEspeciales: Map<Int, EstadoDiente> = emptyMap(),
    notasDientes: Map<Int, String> = emptyMap(),
    dientesTratados: Map<Int, Boolean> = emptyMap(),
    puentes: Map<Int, Int?> = emptyMap(),
    esModoPediatrico: Boolean = false, // Nuevo
    onToggleCara: (Int, Int) -> Unit,
    onLongClickDiente: (Int) -> Unit,
    onGuardarNota: (Int, String) -> Unit,
    onMarcarTratado: (Int, Boolean) -> Unit, // Nuevo
    onLimpiar: () -> Unit,
    onToggleModoPediatrico: () -> Unit,
    onCambiarEstadoEspecial: (Int, EstadoDiente) -> Unit, // NUEVO
    onLimpiarDiente: (Int) -> Unit, // NUEVO
    onConfigurarPuente: (Int) -> Unit // NUEVO
) {
    var mostrarConfirmacionLimpieza by remember { mutableStateOf(false) }
    var dienteParaNota by remember { mutableStateOf<Int?>(null) } // Control del diálogo

    // Lógica B: Filtrado dinámico de dientes
    val filasDientes = if (esModoPediatrico) {
        listOf(
            listOf(55, 54, 53, 52, 51) to listOf(61, 62, 63, 64, 65),
            listOf(85, 84, 83, 82, 81) to listOf(71, 72, 73, 74, 75)
        )
    } else {
        listOf(
            listOf(18, 17, 16, 15, 14, 13, 12, 11) to listOf(21, 22, 23, 24, 25, 26, 27, 28),
            listOf(48, 47, 46, 45, 44, 43, 42, 41) to listOf(31, 32, 33, 34, 35, 36, 37, 38)
        )
    }
    var dienteParaMenu by remember { mutableStateOf<Int?>(null) }

    if (dienteParaMenu != null) {
        val idD = dienteParaMenu!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { dienteParaMenu = null },
            title = { Text("Opciones Diente $idD") },
            text = {
                Column {
                    // Selector de Estado
                    Text("Estado Especial", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        EstadoDiente.entries.forEach { est ->
                            androidx.compose.material3.SuggestionChip(
                                onClick = {
                                    onCambiarEstadoEspecial(idD, est)
                                    dienteParaMenu = null
                                },
                                label = { Text(est.name, fontSize = 9.sp) }
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            onConfigurarPuente(idD)
                            dienteParaMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("CONECTAR PUENTE") }

                    androidx.compose.material3.TextButton(
                        onClick = {
                            onLimpiarDiente(idD)
                            dienteParaMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("LIMPIAR DIENTE", color = Color.Red) }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { dienteParaMenu = null }) { Text("CERRAR") }
            }
        )
    }
    // Diálogo para la Sección E (Notas)
    if (dienteParaNota != null) {
        val idDiente = dienteParaNota!!
        var textoTmp by remember { mutableStateOf(notasDientes[idDiente] ?: "") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { dienteParaNota = null },
            title = { Text("Nota Diente $idDiente", fontSize = 14.sp) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = textoTmp,
                    onValueChange = { textoTmp = it },
                    placeholder = { Text("Escribe una observación...") }
                )
                Spacer(Modifier.height(8.dp))
                // Switch de Evolución (Sección C)
                // Switch de Evolución (Sección C)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Switch(
                        checked = dientesTratados[idDiente] ?: false,
                        onCheckedChange = { onMarcarTratado(idDiente, it) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Tratamiento Realizado", fontSize = 12.sp)
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    onGuardarNota(idDiente, textoTmp)
                    dienteParaNota = null
                }) { Text("GUARDAR") }
            }
        )
    }

    Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Selector Adulto/Pediátrico
        Row(
            Modifier.fillMaxWidth(0.8f).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.FilterChip(
                selected = !esModoPediatrico,
                onClick = { if (esModoPediatrico) onToggleModoPediatrico() },
                label = { Text("ADULTO", fontSize = 9.sp) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            androidx.compose.material3.FilterChip(
                selected = esModoPediatrico,
                onClick = { if (!esModoPediatrico) onToggleModoPediatrico() },
                label = { Text("PEDIÁTRICO", fontSize = 9.sp) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Text("ODONTOGRAMA INTERACTIVO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
        Text("Click: Caras | Long Click: Estado (Normal/Ausente/Corona)", fontSize = 8.sp, color = Color.LightGray)
        Spacer(Modifier.height(8.dp))

        filasDientes.forEach { (izq, der) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                izq.forEach { id ->
                    DienteGrafico(
                        id = id,
                        carasActivas = dientesSeleccionados[id] ?: emptySet(),
                        estado = estadosEspeciales[id] ?: EstadoDiente.NORMAL,
                        tieneNota = !notasDientes[id].isNullOrBlank(),
                        esTratado = dientesTratados[id] ?: false, // Nuevo parámetro
                        onClickCara = { onToggleCara(id, it) },
                        onLongClick = { onLongClickDiente(id) },
                        onAbrirNota = { dienteParaNota = id } // Nuevo parámetro
                    )
                }
                Spacer(Modifier.width(12.dp))
                der.forEach { id ->
                    DienteGrafico(
                        id = id,
                        carasActivas = dientesSeleccionados[id] ?: emptySet(),
                        estado = estadosEspeciales[id] ?: EstadoDiente.NORMAL,
                        tieneNota = !notasDientes[id].isNullOrBlank(),
                        esTratado = dientesTratados[id] ?: false, // Nuevo parámetro
                        conectadoCon = puentes[id], // NUEVO
                        onClickCara = { cara ->
                            // Aquí pasamos la lógica de recidiva
                            onToggleCara(id, cara)
                        },
                        onLongClick = { onLongClickDiente(id) },
                        onAbrirNota = { dienteParaNota = id } // Nuevo parámetro
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        // Nuevo Botón de Limpieza modificado
        androidx.compose.material3.TextButton(
            onClick = { mostrarConfirmacionLimpieza = true },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("LIMPIAR MAPA", color = Color.Red.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        if (mostrarConfirmacionLimpieza) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { mostrarConfirmacionLimpieza = false },
                title = { Text("¿Limpiar Odontograma?") },
                text = { Text("Se borrarán todas las marcas y estados de los dientes. Esta acción no se puede deshacer.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            onLimpiar()
                            mostrarConfirmacionLimpieza = false
                        }
                    ) { Text("LIMPIAR TODO", color = Color.Red) }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { mostrarConfirmacionLimpieza = false }) {
                        Text("CANCELAR")
                    }
                }
            )
        }
    }
}


@Composable
fun DienteGrafico(
    id: Int,
    carasActivas: Set<Int>,
    estado: EstadoDiente,
    tieneNota: Boolean,
    esTratado: Boolean,
    onClickCara: (Int) -> Unit,
    conectadoCon: Int? = null,
    onLongClick: () -> Unit,
    onAbrirNota: () -> Unit
) {
    // Lógica C: Si es tratado es Azul (clínico), si no es Rojo (patología)
    val colorMarca = if (esTratado) Color(0xFF1976D2) else Color.Red

    val colorTexto = when(estado) {
        EstadoDiente.AUSENTE -> Color.LightGray
        EstadoDiente.CORONA -> Color(0xFF101084)
        else -> if (carasActivas.isNotEmpty()) Color.Red else Color.Black
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(1.dp)) {
        Text("$id", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = colorTexto)
        Box(
            Modifier.size(28.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (estado != EstadoDiente.AUSENTE) {
                                val size = 28.dp.toPx()
                                val centro = size / 2
                                val cara = when {
                                    sqrt((offset.x - centro) * (offset.x - centro) + (offset.y - centro) * (offset.y - centro)) < centro * 0.4f -> 0
                                    offset.y < offset.x && offset.y < (size - offset.x) -> 1
                                    offset.x > offset.y && offset.y > (size - offset.x) -> 2
                                    offset.y > offset.x && offset.y > (size - offset.x) -> 3
                                    else -> 4
                                }
                                onClickCara(cara)
                            }
                        },
                        onLongPress = { onLongClick() },
                        onDoubleTap = { onAbrirNota() } // Se eliminó el parámetro 'offset' no usado
                    )
                }
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val s = size.minDimension
                val centro = s / 2
                val colorBase = when(estado) {
                    EstadoDiente.CORONA -> Color(0xFFBBDEFB)
                    EstadoDiente.PROTESIS_FIJA -> Color(0xFFE3F2FD)
                    else -> Color.White
                }

                if (estado != EstadoDiente.AUSENTE && estado != EstadoDiente.IMPLANTE) {
                    drawCircle(if (carasActivas.contains(0)) Color.Red else colorBase, centro * 0.4f)
                    drawCircle(Color.Black, centro * 0.4f, style = Stroke(1f))
                    for (i in 1..4) {
                        val path = Path().apply {
                            when(i) {
                                1 -> { moveTo(0f, 0f); lineTo(s, 0f); lineTo(s*0.7f, s*0.3f); lineTo(s*0.3f, s*0.3f) }
                                2 -> { moveTo(s, 0f); lineTo(s, s); lineTo(s*0.7f, s*0.7f); lineTo(s*0.7f, s*0.3f) }
                                3 -> { moveTo(s, s); lineTo(0f, s); lineTo(s*0.3f, s*0.7f); lineTo(s*0.7f, s*0.7f) }
                                4 -> { moveTo(0f, s); lineTo(0f, 0f); lineTo(s*0.3f, s*0.3f); lineTo(s*0.3f, s*0.7f) }
                            }
                            close()
                        }
                        drawPath(path, if (carasActivas.contains(i)) colorMarca else colorBase)
                        drawPath(path, Color.Black, style = Stroke(1f))
                    }
                }

                when (estado) {
                    EstadoDiente.AUSENTE -> {
                        // Cambiamos Color.Red por colorMarca
                        drawLine(colorMarca, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(s, s), strokeWidth = 3f)
                        drawLine(colorMarca, androidx.compose.ui.geometry.Offset(s, 0f), androidx.compose.ui.geometry.Offset(0f, s), strokeWidth = 3f)
                    }
                    EstadoDiente.IMPLANTE -> {
                        for (y in 1..4) {
                            drawLine(Color.Gray, androidx.compose.ui.geometry.Offset(s*0.3f, (y*s)/5), androidx.compose.ui.geometry.Offset(s*0.7f, (y*s)/5 + 2f), strokeWidth = 2f)
                        }
                        // CORRECCIÓN: Se eliminó 'style = Stroke' del drawLine porque no existe
                        drawLine(Color.Black, androidx.compose.ui.geometry.Offset(centro, 0f), androidx.compose.ui.geometry.Offset(centro, s), strokeWidth = 2f)
                    }
                    EstadoDiente.ENDODONCIA -> {
                        drawLine(Color.Blue, androidx.compose.ui.geometry.Offset(centro, 0f), androidx.compose.ui.geometry.Offset(centro, s), strokeWidth = 5f)
                    }
                    EstadoDiente.FRACTURA -> {
                        val rayo = Path().apply {
                            moveTo(s*0.2f, s*0.1f); lineTo(s*0.8f, s*0.4f); lineTo(s*0.2f, s*0.6f); lineTo(s*0.8f, s*0.9f)
                        }
                        drawPath(rayo, colorMarca, style = Stroke(3f))
                    }
                    EstadoDiente.CUELLO -> {
                        drawArc(colorMarca, 0f, 180f, false,
                            topLeft = androidx.compose.ui.geometry.Offset(s*0.1f, s*0.7f),
                            size = androidx.compose.ui.geometry.Size(s*0.8f, s*0.3f),
                            style = Stroke(3f))
                    }
                    EstadoDiente.ORTODONCIA -> {
                        drawRect(Color.Gray, topLeft = androidx.compose.ui.geometry.Offset(s*0.3f, s*0.3f), size = androidx.compose.ui.geometry.Size(s*0.4f, s*0.4f))
                        drawLine(Color.Gray, androidx.compose.ui.geometry.Offset(0f, centro), androidx.compose.ui.geometry.Offset(s, centro), strokeWidth = 2f)
                    }
                    EstadoDiente.PROTESIS_FIJA -> {
                        drawRect(Color.Blue, style = Stroke(2f))
                    }
                    else -> {}
                }

                if (tieneNota) {
                    drawCircle(
                        color = Color(0xFFFFD600),
                        radius = 6f,
                        center = androidx.compose.ui.geometry.Offset(size.width - 5f, 5f)
                    )
                }

                if (conectadoCon != null) {
                    drawLine(
                        color = Color.Blue.copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(centro, 0f),
                        end = androidx.compose.ui.geometry.Offset(centro + size.width, 0f),
                        strokeWidth = 8f
                    )
                }
            }
        }
    }
}