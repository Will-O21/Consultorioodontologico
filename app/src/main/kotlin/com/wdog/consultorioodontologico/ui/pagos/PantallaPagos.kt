package com.wdog.consultorioodontologico.ui.pagos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.ui.components.EstadoVacioConsultorio
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPagos(
    pacientes: List<Paciente>,
    viewModel: PacienteViewModel
) {
    var busqueda by remember { mutableStateOf("") }
    val colorAzulOscuro = Color(0xFF101084)
    val colorAzulBotones = Color(0xFF094293)
    val colorVerde = Color(0xFF155E29)
    val colorRojo = Color(0xFFAD1D1D)
    val colorFondo = Color(0xFFF8F9FA)

    val prioridadPago = mapOf(
        "Pendiente" to 1,
        "Abonó" to 2,
        "Al día" to 3,
        "Completo" to 3 // Agregamos Completo por si acaso se usa esa string
    )

// 2. Aplicamos el filtrado y el nuevo ordenamiento
    val pacientesFiltrados = pacientes.filter {
        it.nombre.contains(busqueda, ignoreCase = true)
    }.sortedWith(compareBy({ prioridadPago[it.estadoPago] ?: 4 }, { it.nombre }))

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(colorAzulOscuro)
                    .padding(top = 48.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Control de Pagos",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondo)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar paciente por nombre...") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorAzulBotones) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = colorAzulBotones
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ya no definimos pacientesFiltrados aquí, usamos la de arriba
            if (pacientesFiltrados.isEmpty()) {
                EstadoVacioConsultorio(
                    icono = Icons.Default.Payments,
                    mensaje = "Sin información de pagos",
                    colorPersonalizado = Color.Gray
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(pacientesFiltrados) { paciente ->
                        ItemPagoCard(
                            paciente = paciente,
                            viewModel = viewModel,
                            colorAzul = colorAzulBotones,
                            colorVerde = colorVerde,
                            colorRojo = colorRojo,
                            onActualizar = { pActualizado ->
                                viewModel.actualizarPaciente(
                                    paciente = pActualizado,
                                    afecciones = emptyList(),
                                    nuevaFotoPerfil = null,
                                    nuevasPlacas = emptyList(),
                                    borrarPerfilAnterior = false,
                                    placasABorrar = emptyList()
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemPagoCard(
    paciente: Paciente,
    viewModel: PacienteViewModel,
    colorAzul: Color,
    colorVerde: Color,
    colorRojo: Color,
    onActualizar: (Paciente) -> Unit
) {
    // 1. Eliminamos 'abonoInput' porque ya no se usa aquí.
    var isEditing by remember { mutableStateOf(false) }

    val montoOriginal = paciente.monto.toInt()
    val abonoActual = paciente.abono.toInt()
    val deuda = montoOriginal - abonoActual

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- AVATAR ---
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(colorAzul.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!paciente.fotoPerfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = File(paciente.fotoPerfil!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = colorAzul)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- INFORMACIÓN ---
            Column(modifier = Modifier.weight(1f)) {
                Text(text = paciente.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (paciente.estadoPago != "Completo" && paciente.estadoPago != "Al día") {
                    Text(text = "Debe: $$deuda", fontSize = 12.sp, color = colorRojo)
                } else {
                    Text(text = "Pago total recibido", fontSize = 12.sp, color = colorVerde)
                }
            }

            // --- BOTÓN DE ESTADO ---
            val (btnColor, btnText) = when (paciente.estadoPago) {
                "Pendiente" -> colorRojo to "Pendiente"
                "Abonó" -> Color.Blue to "Abonó"
                else -> colorVerde to "Al día"
            }

            Button(
                onClick = { isEditing = true }, // 2. Solo activamos el estado
                colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(btnText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- DIÁLOGO DE GESTIÓN RÁPIDA ---
    if (isEditing) {
        DialogEditarPago(
            paciente = paciente,
            viewModel = viewModel,
            onDismiss = { isEditing = false },
            onConfirm = { pacienteActualizado ->
                onActualizar(pacienteActualizado)
                isEditing = false
            }
        )
    }
}
