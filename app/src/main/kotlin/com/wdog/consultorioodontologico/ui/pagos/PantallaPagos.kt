package com.wdog.consultorioodontologico.ui.pagos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.entities.Paciente

@Composable
fun PantallaPagos(pacientes: List<Paciente>, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Gestión de Pagos")
        pacientes.forEach { paciente ->
            Text(
                text = "${paciente.nombre} ${paciente.apellido} - Estado: ${paciente.estadoPago}",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}