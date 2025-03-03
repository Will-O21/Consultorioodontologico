package com.wdog.consultorioodontologico.ui.pacientes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.wdog.consultorioodontolgico.R
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarPaciente(
    navController: NavController,
    paciente: Paciente,
    viewModel: PacienteViewModel
) {
    var nombre by remember { mutableStateOf(paciente.nombre) }
    var apellido by remember { mutableStateOf(paciente.apellido) }
    var edad by remember { mutableStateOf(paciente.edad.toString()) }
    var observaciones by remember { mutableStateOf(paciente.observaciones) }
    var fotos by remember { mutableStateOf(paciente.fotos.map { Uri.parse(it) }) }
    var estadoPago by remember { mutableStateOf(paciente.estadoPago) }
    var monto by remember { mutableStateOf(paciente.monto.toInt().toString()) }
    var abono by remember { mutableStateOf(paciente.abono.toInt().toString()) }

    // Estado para errores de validación
    var errorMonto by remember { mutableStateOf(false) }
    var errorAbono by remember { mutableStateOf(false) }

    // Estado para afecciones
    var afeccionSeleccionada by remember { mutableStateOf<Color?>(null) }
    var puntosAfeccion by remember {
        mutableStateOf(
            paciente.historiaClinica.split(";").mapNotNull {
                val parts = it.split(",")
                if (parts.size == 3) {
                    val offsetX = parts[0].toFloatOrNull() ?: 0f
                    val offsetY = parts[1].toFloatOrNull() ?: 0f
                    val colorValue = parts[2].toLongOrNull() ?: 0L
                    Pair(Offset(offsetX, offsetY), Color(colorValue))
                } else {
                    null
                }
            }
        )
    }

    // Estado para el modo borrador
    var modoBorrador by remember { mutableStateOf(false) }

    // Launcher para seleccionar nuevas fotos
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { fotos = fotos + it }
        }
    )
    val colorAzul = Color(0xFF094293) // Definimos el color #094293

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Barra de título superior
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center // Centra el texto completamente
                ) {
                    Text(
                        text = "Datos del Paciente",
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

        // Botón "Atrás" debajo de la barra de título
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorAzul // Usamos el color #094293
            )
        ) {
            Text("Atrás", color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campos de texto para editar
            Text("Nombre:")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    singleLine = true
                )
            }

            Text("Apellido:")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    singleLine = true
                )
            }

            Text("Edad:")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = edad,
                    onValueChange = { edad = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    singleLine = true
                )
            }

            // Fotos del paciente
            Text("Fotos del paciente:")
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fotos.forEach { uri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f) // Ajusta la relación de aspecto
                            .clickable {
                                // Aquí puedes agregar lógica para expandir la imagen si lo deseas
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Foto del paciente",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f) // Ajusta la relación de aspecto
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit // Asegura que la imagen no se recorte
                        )
                        // Botón para eliminar la foto
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar foto",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clickable {
                                    fotos = fotos.filterNot { it == uri }
                                }
                                .padding(4.dp)
                                .background(Color.White, CircleShape)
                        )
                        // Botón para reemplazar la foto
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Reemplazar foto",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clickable {
                                    fotos = fotos.filterNot { it == uri }
                                    launcher.launch("image/*")
                                }
                                .padding(4.dp)
                                .background(Color.White, CircleShape)
                        )
                    }
                }
                if (fotos.size < 4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+")
                    }
                }
            }

            // Modelo dental con afecciones
            Text("Afecciones en el modelo dental:")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                if (modoBorrador) {
                                    // Eliminar el punto más cercano al hacer clic en modo borrador
                                    puntosAfeccion = puntosAfeccion.filterNot { (pointOffset, _) ->
                                        val distance = (pointOffset - offset).getDistance()
                                        distance < 20f // Umbral de distancia para eliminar el punto
                                    }
                                } else if (afeccionSeleccionada != null) {
                                    // Ajustar el offset para que el punto se dibuje correctamente
                                    val adjustedOffset = Offset(
                                        offset.x.coerceIn(0f, size.width.toFloat()),
                                        offset.y.coerceIn(0f, size.height.toFloat())
                                    )
                                    puntosAfeccion = puntosAfeccion + Pair(adjustedOffset, afeccionSeleccionada!!)
                                }
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.modelo_dental),
                        contentDescription = "Modelo dental",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Dibuja los puntos de afección con animaciones
                    puntosAfeccion.forEachIndexed { _, (offset, color) ->
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                            exit = fadeOut(animationSpec = tween(durationMillis = 500))
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
                                    .size(8.dp)
                                    .background(color, CircleShape)
                            )
                        }
                    }
                }
            }

            // Paleta de colores y botón de borrador
            Text("Selecciona un color para marcar o activar el borrador:")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Color.Red, Color.Yellow).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(color, CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                            .clickable {
                                afeccionSeleccionada = color
                                modoBorrador = false
                            }
                    )
                }
                // Botón de borrador
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(if (modoBorrador) Color.Blue else Color.LightGray, CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                        .clickable {
                            modoBorrador = true
                            afeccionSeleccionada = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("X", color = Color.White)
                }
            }

            // Campo Observaciones
            Text("Observaciones:")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )
            }

            // Campo Monto
            Text("Monto:")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .padding(8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    singleLine = true
                )
            }

            // Estado de Pago
            Text("Estado de Pago:")
            var expanded by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorAzul // Usamos el color #094293
                    )
                ) {
                    Text(estadoPago, color = Color.White)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Pendiente") },
                        onClick = {
                            estadoPago = "Pendiente"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Abonó") },
                        onClick = {
                            estadoPago = "Abonó"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Completo") },
                        onClick = {
                            estadoPago = "Completo"
                            expanded = false
                        }
                    )
                }
            }

            // Campo Abono (visible solo si el estado es "Abonó")
            if (estadoPago == "Abonó") {
                Text("Abono:")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = abono,
                        onValueChange = { abono = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        singleLine = true
                    )
                }
            }

            // Botón para guardar cambios
            Button(
                onClick = {
                    val montoValido = monto.toIntOrNull() ?: 0
                    val abonoValido = abono.toIntOrNull() ?: 0

                    errorMonto = montoValido < 0
                    errorAbono = abonoValido < 0

                    if (!errorMonto && !errorAbono) {
                        val pacienteActualizado = paciente.copy(
                            nombre = nombre,
                            apellido = apellido,
                            edad = edad.toIntOrNull() ?: 0,
                            observaciones = observaciones,
                            fotos = fotos.map { it.toString() },
                            historiaClinica = puntosAfeccion.joinToString(";") { (offset, color) ->
                                "${offset.x},${offset.y},${color.value.toInt()}"
                            },
                            estadoPago = estadoPago,
                            monto = montoValido.toDouble(),
                            abono = abonoValido.toDouble()
                        )
                        viewModel.actualizarPaciente(pacienteActualizado)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorAzul // Usamos el color #094293
                )
            ) {
                Text("Actualizar Datos", color = Color.White)
            }
        }
    }
}