package com.wdog.consultorioodontologico.ui.pacientes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPacientes(
    pacientes: List<Paciente>,
    navController: NavController,
    viewModel: PacienteViewModel
) {
    var busqueda by remember { mutableStateOf("") }
    val colorAzul = Color(0xFF094293) // Definimos el color #094293

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Barra de título superior
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center // Centra el texto completamente
                ) {
                    Text(
                        text = "Pacientes",
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

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
                    // Campo de búsqueda
                    TextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        label = {
                            Text(
                                text = "Buscar paciente por nombre",
                                color = Color.Black // Texto del label en color negro
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorAzul.copy(alpha = 0.1f), // Fondo cuando está enfocado
                            unfocusedContainerColor = colorAzul.copy(alpha = 0.1f), // Fondo cuando no está enfocado
                            focusedIndicatorColor = colorAzul, // Color del indicador cuando está enfocado
                            unfocusedIndicatorColor = colorAzul, // Color del indicador cuando no está enfocado
                            focusedLabelColor = Color.Black, // Color del label cuando está enfocado
                            unfocusedLabelColor = Color.Black // Color del label cuando no está enfocado
                        )
                    )

                    // Lista de pacientes filtrada
                    val pacientesFiltrados = pacientes.filter { it.nombre.contains(busqueda, ignoreCase = true) }

                    if (pacientesFiltrados.isEmpty()) {
                        Text("No hay pacientes registrados", modifier = Modifier.padding(8.dp))
                    } else {
                        LazyColumn {
                            items(pacientesFiltrados.sortedBy { it.nombre }) { paciente ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = colorAzul.copy(alpha = 0.2f) // Fondo de la card con un tono más claro de azul
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically, // Alinea verticalmente el contenido
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${paciente.nombre} ${paciente.apellido}",
                                            modifier = Modifier.weight(1f),
                                            color = Color.Black, // Color del texto en negro
                                                    fontWeight = FontWeight.Bold
                                        )

                                        // Botón para ver detalles del paciente (icono de ojo)
                                        IconButton(
                                            onClick = {
                                                navController.navigate("editar_paciente/${paciente.id}")
                                            },
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "Ver/Editar",
                                                tint = colorAzul // Color del icono
                                            )
                                        }

                                        // Botón para eliminar paciente (icono de papelera)
                                        IconButton(
                                            onClick = {
                                                viewModel.eliminarPaciente(paciente)
                                            },
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.Black // Color del icono rojo
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
    }
}