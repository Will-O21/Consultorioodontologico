package com.wdog.consultorioodontologico.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.navigation.AppNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(navController: NavController) {
    val colorAzulTitulo = Color(0xFF101084) // Color azul para la barra de título
    val colorAzulBotones = Color(0xFF094293) // Color azul para los botones

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Consultorio Odontológico",
                        modifier = Modifier.padding(start = 16.dp) // Alineado verticalmente a la izquierda
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorAzulTitulo, // Color de fondo azul #101084
                    titleContentColor = Color.White // Texto en color blanco
                )
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White), // Fondo de la pantalla en color negro
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Centra el contenido verticalmente
            ) {
                Text(
                    text = "Toma el control",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black, // Texto en color blanco
                    fontSize = 24.sp, // Tamaño de la fuente más grande
                    fontFamily = FontFamily.Default, // Fuente Roboto (por defecto en Compose)
                    fontWeight = FontWeight.Bold, // Texto en negritas
                    textAlign = TextAlign.Center // Texto centrado
                )
                Button(
                    onClick = { navController.navigate(AppNavigation.REGISTRO) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorAzulBotones // Color de fondo azul #094293
                    )
                ) {
                    Text("Registrar  Nuevo  Paciente", color = Color.White)
                }
                Button(
                    onClick = { navController.navigate(AppNavigation.PACIENTES) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorAzulBotones // Color de fondo azul #094293
                    )
                ) {
                    Text("Ver  Pacientes", color = Color.White)
                }
                Button(
                    onClick = { navController.navigate(AppNavigation.CITAS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorAzulBotones // Color de fondo azul #094293
                    )
                ) {
                    Text("Gestionar  Citas", color = Color.White)
                }
                Button(
                    onClick = { navController.navigate(AppNavigation.PAGOS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorAzulBotones // Color de fondo azul #094293
                    )
                ) {
                    Text("Gestión  de  Pagos", color = Color.White)
                }
                // Nuevo botón para la gestión del consultorio
                Button(
                    onClick = { navController.navigate(AppNavigation.GESTION_CONSULTORIO) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorAzulBotones // Color de fondo azul #094293
                    )
                ) {
                    Text("Gestión del Consultorio", color = Color.White)
                }
            }
        }
    )
}