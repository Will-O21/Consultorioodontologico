package com.wdog.consultorioodontologico.ui.pagos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wdog.consultorioodontologico.entities.Paciente
import androidx.compose.material.icons.filled.Refresh
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import androidx.compose.ui.text.font.FontStyle
import java.util.Locale
import androidx.compose.foundation.verticalScroll

@Composable
fun DialogEditarPago(
    paciente: Paciente,
    viewModel: PacienteViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Paciente) -> Unit
) {
    // Estados locales para el manejo de los inputs
    var montoInput by remember { mutableStateOf("") }
    var seleccionAbono by remember { mutableStateOf(true) } // true = Abonar, false = Nuevo Monto

    LaunchedEffect(Unit) {
        viewModel.obtenerTasaBCV(forzar = false)
    }
    // --- LÓGICA DE TASA BCV ---
    val tasaBCV by viewModel.tasaBCV


    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorVerde = Color(0xFF155E29)
    val colorRojo = Color(0xFFAD1D1D)

    val montoActual = paciente.monto.toInt()
    val abonoActual = paciente.abono.toInt()
    val deudaActual = montoActual - abonoActual

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), // <--- El punto conecta ambos
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título del Dialog
                Text(
                    text = "Gestión de Pago",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorAzulOscuro
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                // --- SECCIÓN 1: DATOS ACTUALES ---
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Monto Total:", color = Color.Gray, fontSize = 15.sp)
                        Text("$$montoActual", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Deuda Actual:", color = Color.Gray, fontSize = 15.sp)
                        Text(
                            text = "$$deudaActual",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (deudaActual > 0) colorRojo else colorVerde
                        )
                    }
                }

                // --- SECCIÓN 2: SELECTOR DE OPERACIÓN (TABS) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFF1F1F1))
                        .padding(4.dp)
                ) {
                    // Pestaña Abonar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (seleccionAbono) colorAzulBotones else Color.Transparent)
                            .clickable { seleccionAbono = true; montoInput = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Abonar",
                            color = if (seleccionAbono) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    // Pestaña Nuevo Monto
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (!seleccionAbono) colorAzulBotones else Color.Transparent)
                            .clickable { seleccionAbono = false; montoInput = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nuevo Monto",
                            color = if (!seleccionAbono) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // --- SECCIÓN 3: INPUT DINÁMICO ---
                OutlinedTextField(
                    value = montoInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) montoInput = it },
                    label = { Text(if (seleccionAbono) "Monto abonado" else "Ingrese nueva deuda") },
                    placeholder = { Text("0") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("$ ") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))
                if (montoInput.isNotEmpty()) {
                    val montoParaCalcular = montoInput.toDoubleOrNull() ?: 0.0
                    val totalBs = montoParaCalcular * tasaBCV

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Tasa BCV: ${String.format(Locale.getDefault(), "%.2f", tasaBCV)} Bs",
                                        fontSize = 11.sp,
                                        color = colorAzulBotones,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        // Cambio quirúrgico: forzar = true por ser acción manual
                                        onClick = { viewModel.obtenerTasaBCV(forzar = true) },
                                        modifier = Modifier.size(24.dp).padding(start = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = colorAzulBotones, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(
                                    text = if (seleccionAbono) "Calculando Abono" else "Nuevo Cargo",
                                    fontSize = 9.sp,
                                    color = Color.DarkGray,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            Text(
                                text = "Monto: Bs. ${String.format(Locale.getDefault(), "%.2f", totalBs)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorAzulOscuro
                            )
                        }
                    }
                }
                // --- SECCIÓN 4: BOTONES CANCELAR / GUARDAR ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("CANCELAR", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val valor = montoInput.toDoubleOrNull() ?: 0.0
                            var nMonto = paciente.monto
                            var nAbono = paciente.abono

                            if (seleccionAbono) {
                                nAbono += valor
                                if (nAbono > nMonto) nAbono = nMonto // Evita abonos mayores al total
                            } else {
                                nMonto += valor // Suma a la deuda existente
                            }

                            val nEstado = when {
                                nAbono >= nMonto -> "Al día"
                                nAbono > 0 -> "Abonó"
                                else -> "Pendiente"
                            }

                            onConfirm(paciente.copy(
                                monto = nMonto,
                                abono = nAbono,
                                estadoPago = nEstado,
                                fechaUltimoPago = System.currentTimeMillis() // <--- Conecta con Idea 1 de Finanzas
                            ))
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAzulOscuro),
                        enabled = montoInput.isNotEmpty()
                    ) {
                        Text("GUARDAR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}