package com.wdog.consultorioodontologico.ui.pagos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel

@Composable
fun PantallaEditarPago(
    navController: NavController,
    paciente: Paciente,
    viewModel: PacienteViewModel
) {
    var estadoPago by remember { mutableStateOf(paciente.estadoPago) }
    var monto by remember { mutableStateOf(paciente.monto.toString()) }
    var abono by remember { mutableStateOf(paciente.abono.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Editar Estado de Pago")

        Text("Monto:")
        BasicTextField(
            value = monto,
            onValueChange = { monto = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Text("Abono:")
        BasicTextField(
            value = abono,
            onValueChange = { abono = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        // Botón para guardar cambios
        Button(
            onClick = {
                val pacienteActualizado = paciente.copy(
                    estadoPago = estadoPago,
                    monto = monto.toDoubleOrNull() ?: 0.0,
                    abono = abono.toDoubleOrNull() ?: 0.0
                )
                viewModel.actualizarPaciente(pacienteActualizado)
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Actualizar Pago")
        }
    }
}