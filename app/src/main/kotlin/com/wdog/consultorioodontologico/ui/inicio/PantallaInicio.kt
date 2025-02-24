package com.wdog.consultorioodontologico.ui.inicio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PantallaInicio(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido al Consultorio Odontológico")
        Button(
            onClick = { navController.navigate("registro") },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Ir a Registro")
        }
        Button(
            onClick = { navController.navigate("pacientes") },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Ver Pacientes")
        }
    }
}