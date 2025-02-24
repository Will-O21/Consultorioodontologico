package com.wdog.consultorioodontologico.ui.registro

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.wdog.consultorioodontolgico.R
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModelFactory


@Composable
fun PantallaRegistro(navController: NavController) {
    // Estados para los campos de texto
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    // Estados para errores de validación
    var errorNombre by remember { mutableStateOf(false) }
    var errorApellido by remember { mutableStateOf(false) }
    var errorEdad by remember { mutableStateOf(false) }

    // Estado para fotos
    val fotos = remember { mutableStateListOf<Uri>() }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.takeIf { fotos.size < 4 }?.let { fotos.add(it) }
        }
    )

    // Estado para afección seleccionada
    var afeccionSeleccionada by remember { mutableStateOf<Color?>(null) }

    // ViewModel
    val context = LocalContext.current
    val viewModel: PacienteViewModel = viewModel(
        factory = PacienteViewModelFactory(context.applicationContext as Application)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo Nombre
        Text("Nombre:")
        BasicTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                errorNombre = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, if (errorNombre) Color.Red else Color.Gray)
        )
        if (errorNombre) Text("Campo obligatorio", color = Color.Red)

        // Campo Apellido
        Text("Apellido:")
        BasicTextField(
            value = apellido,
            onValueChange = {
                apellido = it
                errorApellido = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, if (errorApellido) Color.Red else Color.Gray)
        )
        if (errorApellido) Text("Campo obligatorio", color = Color.Red)

        // Campo Edad
        Text("Edad:")
        BasicTextField(
            value = edad,
            onValueChange = {
                edad = it
                errorEdad = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, if (errorEdad) Color.Red else Color.Gray)
        )
        if (errorEdad) Text("Campo obligatorio", color = Color.Red)

        // Selector de Fotos
        Text("Fotos del paciente (máximo 4):")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fotos) { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri), // <-- ¡Sintaxis correcta!
                    contentDescription = "Foto del paciente",
                    modifier = Modifier
                        .size(100.dp)
                        .border(1.dp, Color.Gray),
                    contentScale = ContentScale.Crop
                )
            }
            if (fotos.size < 4) {
                item {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.LightGray)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+")
                    }
                }
            }
        }

        // Modelo Dental
        Text("Seleccione afecciones en el modelo dental:")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White)
                .border(1.dp, Color.Gray)
                .clickable { afeccionSeleccionada = Color.Red },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.modelo_dental), //colocar la imagen
                contentDescription = "Modelo dental",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Paleta de Colores
        Text("Paleta de colores:")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(color, CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                        .clickable { afeccionSeleccionada = color }
                )
            }
        }

        // Campo Observaciones
        Text("Observaciones:")
        BasicTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(8.dp)
                .border(1.dp, Color.Gray)
        )

        // Botón Guardar
        Button(
            onClick = {
                errorNombre = nombre.isBlank()
                errorApellido = apellido.isBlank()
                errorEdad = edad.isBlank()

                if (!errorNombre && !errorApellido && !errorEdad) {
                    val paciente = Paciente(
                        nombre = nombre,
                        apellido = apellido,
                        edad = edad.toIntOrNull() ?: 0,
                        fotos = fotos.map { it.toString() },
                        historiaClinica = afeccionSeleccionada?.toString() ?: "",
                        observaciones = observaciones,
                        estadoPago = "Pendiente"
                    )
                    viewModel.insertarPaciente(paciente)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Guardar Paciente")
        }
    }
}