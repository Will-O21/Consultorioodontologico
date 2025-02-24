package com.wdog.consultorioodontologico.ui.pacientes

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.entities.Paciente

@Composable
fun PantallaPacientes(pacientes: List<Paciente>, navController: NavController) { // <-- Eliminamos navController
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(pacientes) { paciente ->
            Text(
                text = "${paciente.nombre} ${paciente.apellido}, Edad: ${paciente.edad}",
                modifier = Modifier.padding(8.dp))
        }
    }
}