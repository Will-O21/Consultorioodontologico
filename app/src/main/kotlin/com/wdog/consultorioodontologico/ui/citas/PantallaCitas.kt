package com.wdog.consultorioodontologico.ui.citas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.navigation.AppNavigation
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCitas(
    navController: NavController,
    citaViewModel: CitaViewModel,
    pacienteViewModel: PacienteViewModel
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | hh:mm a")
    var busqueda by remember { mutableStateOf("") }
    val colorAzul = Color(0xFF094293)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Citas",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF101084)
            )
        )

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorAzul
            )
        ) {
            Text("Atrás", color = Color.White)
        }

        Button(
            onClick = { navController.navigate(AppNavigation.AGREGAR_CITA) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorAzul
            )
        ) {
            Text("Agendar Nueva Cita", color = Color.White)
        }

        TextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = {
                Text(
                    text = "Buscar cita por fecha u observaciones",
                    color = Color.Black
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorAzul.copy(alpha = 0.1f),
                unfocusedContainerColor = colorAzul.copy(alpha = 0.1f),
                focusedIndicatorColor = colorAzul,
                unfocusedIndicatorColor = colorAzul,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black
            )
        )

        val citas by citaViewModel.obtenerCitas().collectAsState(initial = emptyList())
        val pacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())

        val citasFiltradas = citas.filter {
            it.fechaHora.toString().contains(busqueda, ignoreCase = true) ||
                    it.observaciones.contains(busqueda, ignoreCase = true)
        }

        if (citasFiltradas.isEmpty()) {
            Text("No hay citas agendadas", modifier = Modifier.padding(8.dp))
        } else {
            LazyColumn {
                items(citasFiltradas) { cita ->
                    val paciente = pacientes.find { it.id == cita.pacienteId }
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    navController.navigate("detalle_cita/${cita.id}")
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = colorAzul.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Paciente: ${paciente?.nombre} ${paciente?.apellido}",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "Fecha: ${cita.fechaHora.format(formatter)}")
                                Text(text = "Observaciones: ${cita.observaciones}")
                            }
                        }
                    }
                }
            }
        }
    }
}