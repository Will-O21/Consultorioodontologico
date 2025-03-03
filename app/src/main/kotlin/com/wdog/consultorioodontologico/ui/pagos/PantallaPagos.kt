package com.wdog.consultorioodontologico.ui.pagos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPagos(
    pacientes: List<Paciente>,
    navController: NavController,
    viewModel: PacienteViewModel
) {
    val colorAzul = Color(0xFF094293) // Definimos el color #094293
    val colorVerde = Color(0xFF155e29) // Color verde para "Al día" y "Completo"
    val colorRojo = Color(0xFFa51b0b) // Color rojo para "Pendiente"
    val colorFondoCard = colorAzul.copy(alpha = 0.1f)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barra de título superior
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pagos",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF101084) // Color de fondo azul #101084
            )
        )

        // Lista de pacientes con pagos (organizada por estado)
        if (pacientes.isEmpty()) {
            Text("No hay pacientes registrados", modifier = Modifier.padding(8.dp))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .weight(1f) // Para que el LazyColumn ocupe el espacio restante
            ) {
                items(pacientes.sortedBy { it.estadoPago }) { paciente ->
                    var estadoPago by remember { mutableStateOf(paciente.estadoPago) }
                    var monto by remember { mutableStateOf(paciente.monto.toInt().toString()) }
                    var abono by remember { mutableStateOf(paciente.abono.toInt().toString()) }
                    var isEditing by remember { mutableStateOf(false) }
                    val montoOriginal by remember { mutableIntStateOf(paciente.monto.toInt()) }
                    var abonoOriginal by remember { mutableIntStateOf(paciente.abono.toInt()) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorFondoCard
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${paciente.nombre} ${paciente.apellido}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            when (estadoPago) {
                                "Pendiente" -> {
                                    Button(
                                        onClick = { isEditing = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = colorRojo)
                                    ) {
                                        Text("Pendiente")
                                    }
                                }
                                "Abonó" -> {
                                    Text("Debe: ${montoOriginal - abonoOriginal}")
                                    Button(
                                        onClick = { isEditing = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                                    ) {
                                        Text("Abonó")
                                    }
                                }
                                "Completo" -> {
                                    Button(
                                        onClick = { /* No hace nada */ },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = colorVerde)
                                    ) {
                                        Text("Al día")
                                    }
                                }
                            }
                        }
                    }

                    // Diálogo para editar el abono
                    if (isEditing) {
                        Dialog(
                            onDismissRequest = { isEditing = false }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Fila para Monto y su valor
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Monto Total:",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = monto,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    // Fila para Debe y su valor
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Debe Actualmente:",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${montoOriginal - abonoOriginal}",
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    // Fila para Abono y su valor editable
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Está Abonando:",
                                            fontWeight = FontWeight.Bold
                                        )
                                        BasicTextField(
                                            value = abono,
                                            onValueChange = { abono = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(8.dp),
                                            textStyle = LocalTextStyle.current.copy(
                                                textAlign = TextAlign.End,
                                                        fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    // Fila para Restarían y su valor numérico
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Restarían:",
                                            fontWeight = FontWeight.Bold
                                        )
                                        // Valor de Restarían (calculado en tiempo real)
                                        val restarian = remember(abono) {
                                            val abonoActual = abono.toIntOrNull() ?: 0
                                            (montoOriginal - abonoOriginal) - abonoActual
                                        }
                                        Text(
                                            text = if (restarian >= 0) "$restarian" else "Corrige el Abono",
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(8.dp),
                                            color = if (restarian >= 0) Color.Unspecified else Color.Red // Cambia el color a rojo si es negativo
                                        )
                                    }

                                    // Botones para guardar cambios
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val nuevoAbono = abono.toIntOrNull() ?: 0
                                                if (nuevoAbono <= (montoOriginal - abonoOriginal)) {
                                                    abonoOriginal += nuevoAbono
                                                    estadoPago = if (montoOriginal - abonoOriginal == 0) {
                                                        "Completo"
                                                    } else {
                                                        "Abonó"
                                                    }
                                                    val pacienteActualizado = paciente.copy(
                                                        estadoPago = estadoPago,
                                                        monto = montoOriginal.toDouble(),
                                                        abono = abonoOriginal.toDouble()
                                                    )
                                                    viewModel.actualizarPaciente(pacienteActualizado)
                                                    isEditing = false
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                                        ) {
                                            Text(
                                                text = "Abonado",
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center // Centrar el texto
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                estadoPago = "Completo"
                                                abonoOriginal = montoOriginal
                                                val pacienteActualizado = paciente.copy(
                                                    estadoPago = estadoPago,
                                                    monto = montoOriginal.toDouble(),
                                                    abono = abonoOriginal.toDouble()
                                                )
                                                viewModel.actualizarPaciente(pacienteActualizado)
                                                isEditing = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = colorVerde)
                                        ) {
                                            Text("Completo")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botón "Atrás" en la esquina inferior derecha, debajo de la lista de pacientes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), // Aumentamos el padding para más distancia
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(56.dp)
                    .background(colorAzul, shape = MaterialTheme.shapes.medium),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás"
                )
            }
        }
    }
}