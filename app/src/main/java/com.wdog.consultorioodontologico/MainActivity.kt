package com.wdog.consultorioodontologico

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wdog.consultorioodontologico.navigation.AppNavigation
import com.wdog.consultorioodontologico.ui.theme.ConsultorioOdontologicoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsultorioOdontologicoTheme {
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// Componentes Composable (si son necesarios en otros lugares)
@Composable
fun PantallaInicio() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido al Consultorio Odontológico",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        BarraNavegacion()
    }
}

@Composable
fun BarraNavegacion() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TextButton(onClick = { /* Navegar a Registro */ }) {
            Text("Registro")
        }
        TextButton(onClick = { /* Navegar a Pacientes */ }) {
            Text("Pacientes")
        }
        TextButton(onClick = { /* Navegar a Citas */ }) {
            Text("Citas")
        }
        TextButton(onClick = { /* Navegar a Pagos */ }) {
            Text("Pagos")
        }
    }
}