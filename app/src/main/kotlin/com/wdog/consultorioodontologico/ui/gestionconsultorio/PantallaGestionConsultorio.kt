package com.wdog.consultorioodontologico.ui.gestionconsultorio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wdog.consultorioodontologico.database.AppDatabase
import com.wdog.consultorioodontologico.entities.Nota
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionConsultorio() {
    val colorAzulTitulo = Color(0xFF101084)

    var showDialog by remember { mutableStateOf(false) }
    var notaSeleccionada by remember { mutableStateOf<Nota?>(null) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val notaDao = db.notaDao()

    // Cargar las notas desde Room
    val notas by notaDao.obtenerTodasLasNotas().collectAsState(initial = emptyList())

    // Función para guardar una nota
    fun guardarNota(nota: Nota) {
        CoroutineScope(Dispatchers.IO).launch {
            if (nota.id == 0) {
                notaDao.insertarNota(nota)
            } else {
                notaDao.actualizarNota(nota)
            }
        }
    }

    // Función para eliminar una nota
    fun eliminarNota(nota: Nota) {
        CoroutineScope(Dispatchers.IO).launch {
            notaDao.eliminarNota(nota.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pendientes del Consultorio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorAzulTitulo,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    notaSeleccionada = null
                    showDialog = true
                },
                containerColor = colorAzulTitulo,
                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar nota", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            LazyColumn {
                items(notas) { nota ->
                    NotaCard(
                        nota = nota,
                        onNotaClick = {
                            notaSeleccionada = nota
                            showDialog = true
                        },
                        onDeleteClick = {
                            eliminarNota(nota)
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = {
            showDialog = false
            notaSeleccionada = null
        }) {
            NotaDialog(
                nota = notaSeleccionada,
                onSave = { nuevaNota ->
                    guardarNota(nuevaNota)
                    showDialog = false
                    notaSeleccionada = null
                },
                onDismiss = {
                    showDialog = false
                    notaSeleccionada = null
                }
            )
        }
    }
}

@Composable
fun NotaCard(
    nota: Nota,
    onNotaClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onNotaClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)) // Fondo azul claro
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo de color al lado del título
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(nota.colorTitulo))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = nota.titulo,
                color = Color.Black,
                fontWeight = FontWeight.Bold // Título en negrilla
            )
            Spacer(modifier = Modifier.weight(1f)) // Espacio flexible
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar nota",
                    tint = Color.Black // Ícono de papelera en negro
                )
            }
        }
    }
}

@Composable
fun NotaDialog(
    nota: Nota?,
    onSave: (Nota) -> Unit,
    onDismiss: () -> Unit
) {
    var titulo by remember { mutableStateOf(nota?.titulo ?: "") }
    var cuerpo by remember { mutableStateOf(nota?.cuerpo ?: "") }
    var colorTitulo by remember { mutableIntStateOf(nota?.colorTitulo ?: Color(0xFFa51b0b).toArgb()) } // Rojo por defecto
    var isColorSelected by remember { mutableStateOf(nota != null) } // Si es edición, el color ya está seleccionado
    var isTituloEmpty by remember { mutableStateOf(false) } // Validación de título vacío

    Card(
        modifier = Modifier
            .width(400.dp) // Tamaño más grande
            .height(500.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Scroll en el cuerpo del texto
        ) {
            TextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE3F2FD), // Fondo azul claro
                    unfocusedContainerColor = Color(0xFFE3F2FD),
                    focusedTextColor = Color.Black, // Texto negro
                    unfocusedTextColor = Color.Black
                ),
                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold), // Título en negrilla
                isError = isTituloEmpty // Mostrar error si el título está vacío
            )
            if (isTituloEmpty) {
                Text(
                    text = "El título no puede estar vacío",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = cuerpo,
                onValueChange = { cuerpo = it },
                label = { Text("Escriba aquí...") }, // Cambio de texto
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Altura fija para el cuerpo
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE3F2FD),
                    unfocusedContainerColor = Color(0xFFE3F2FD),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center // Centrar los botones
            ) {
                Button(
                    onClick = {
                        colorTitulo = Color(0xFFa51b0b).toArgb() // Rojo
                        isColorSelected = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (colorTitulo == Color(0xFFa51b0b).toArgb()) Color(0xFF8a1508) else Color(0xFFa51b0b) // Oscurece si está seleccionado
                    )
                ) {
                    Text("Urgente", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        colorTitulo = Color(0xFFf4ed25).toArgb() // Amarillo
                        isColorSelected = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (colorTitulo == Color(0xFFf4ed25).toArgb()) Color(0xFFd4c920) else Color(0xFFf4ed25) // Oscurece si está seleccionado
                    )
                ) {
                    Text("Prioridad", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    colorTitulo = Color(0xFF5cb500).toArgb() // Verde
                    isColorSelected = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colorTitulo == Color(0xFF5cb500).toArgb()) Color(0xFF4a9a00) else Color(0xFF5cb500) // Oscurece si está seleccionado
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Listo", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        isTituloEmpty = true // Mostrar error si el título está vacío
                    } else {
                        isTituloEmpty = false
                        if (isColorSelected) {
                            onSave(Nota(titulo = titulo, cuerpo = cuerpo, colorTitulo = colorTitulo))
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF094293)), // Botón azul
                modifier = Modifier.fillMaxWidth(),
                enabled = isColorSelected // Solo habilitado si se selecciona un color
            ) {
                Text("Guardar", color = Color.White)
            }
        }
    }
}