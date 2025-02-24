package com.wdog.consultorioodontologico.ui.citas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel

@Composable
fun PantallaCitas(navController: NavController, viewModel: CitaViewModel) {
    // Observar las citas en tiempo real
    val citas by viewModel.obtenerCitas().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Citas Agendadas")

        // Botón para agregar una nueva cita
        Button(
            onClick = {
                // Navegar a la pantalla de agregar cita
                navController.navigate("ruta_para_agregar_cita")
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Agendar Nueva Cita")
        }

        // Lista de citas
        LazyColumn {
            items(citas) { cita ->
                Text(
                    text = "Cita: ${cita.fechaHora} - ${cita.observaciones}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}