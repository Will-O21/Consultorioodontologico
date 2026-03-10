package com.wdog.consultorioodontologico.ui.inicio

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.navigation.AppNavigation
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import com.wdog.consultorioodontologico.viewmodels.NotaViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import android.content.Context
import androidx.compose.material.icons.filled.Paid
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toUri // <--- PARA LA ADVERTENCIA DE KTX

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    navController: NavController,
    citaViewModel: CitaViewModel,
    notaViewModel: NotaViewModel,
    pacienteViewModel: PacienteViewModel
) {
    val context = LocalContext.current
    // Acceso a SharedPreferences
    val prefs =
        remember { context.getSharedPreferences("config_consultorio", Context.MODE_PRIVATE) }

    // --- ESTADOS CON PERSISTENCIA ---
    var nombreDoctor by remember { mutableStateOf(prefs.getString("nombre_doc", "Doc") ?: "Doc") }
    var nombreConsultorio by remember { mutableStateOf(prefs.getString("nombre_cons", "Gestión Odontológica Profesional") ?: "Gestión Odontológica Profesional") }

    // Este efecto actualizará los nombres cada vez que el diálogo de configuración se cierre
    // (Asegura que el banner siempre coincida con lo que guardaste)
    androidx.compose.runtime.LaunchedEffect(key1 = prefs.all) {
        nombreDoctor = prefs.getString("nombre_doc", "Doc") ?: "Doc"
        nombreConsultorio = prefs.getString("nombre_cons", "Gestión Odontológica Profesional") ?: "Gestión Odontológica Profesional"
    }

    // --- LÓGICA DE CONTEO E INTEGRIDAD ---
    val numCitas by citaViewModel.conteoCitasHoy.collectAsState(initial = 0)
    val numNotas by notaViewModel.conteoNotasUrgentes.collectAsState(initial = 0)

    // --- LÓGICA DE FRASE MOTIVACIONAL ---
    val frasesMotivacionales = remember {
        listOf(
            "La sonrisa es el espejo del alma. ¡Que tengas un gran día!",
            "Tu trabajo cambia vidas, una sonrisa a la vez.",
            "La excelencia no es un acto, es un hábito.",
            "Haz que cada paciente se sienta especial hoy.",
            "El éxito comienza con una sonrisa saludable."
        )
    }
    val fraseDelDia = remember {
        val diaDelAno = java.time.LocalDate.now().dayOfYear
        frasesMotivacionales[diaDelAno % frasesMotivacionales.size]
    }

    val pacientes by pacienteViewModel.todosLosPacientes.collectAsState(initial = emptyList())

    // Filtramos TODOS los que cumplen hoy
    val cumpleanerosHoy = remember(pacientes) {
        val hoy = java.time.LocalDate.now()
        val diaMesHoy = "${String.format("%02d", hoy.dayOfMonth)}/${String.format("%02d", hoy.monthValue)}"
        pacientes.filter { it.fechaNacimiento.contains(diaMesHoy) }
    }
    val textoCumple = when {
        cumpleanerosHoy.isEmpty() -> "Ninguno"
        cumpleanerosHoy.size == 1 -> cumpleanerosHoy.first().nombre
        else -> "${cumpleanerosHoy.size} Pacientes 🎂" // Ejemplo: "3 Pacientes 🎂"
    }

    val colorAzulTitulo = Color(0xFF101084)
    val colorFondo = Color(0xFFF8F9FA)

    val tasaActual by remember { derivedStateOf { pacienteViewModel.tasaBCV.doubleValue } }
    val estaCargandoTasa by remember { derivedStateOf { pacienteViewModel.cargandoTasa.value } }
    // Cargar tasa al iniciar la pantalla
    LaunchedEffect(Unit) {
        pacienteViewModel.obtenerTasaBCV(forzar = false)
    }


    val logoPath = prefs.getString("logo_path", null)

    Box(modifier = Modifier.fillMaxSize().background(colorFondo)) {
        // --- LOGO DE FONDO (MARCA DE AGUA MEJORADA) ---
        val bitmap = remember(logoPath) {
            if (!logoPath.isNullOrEmpty()) {
                try {
                    context.contentResolver.openInputStream(logoPath.toUri()).use {
                        android.graphics.BitmapFactory.decodeStream(it)
                    }
                } catch (_: Exception) { null }
            } else null
        }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp)
                    .size(250.dp),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    // Muestra el logo cargado por el usuario
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        alpha = 0.05f,
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                } else {
                    // Muestra un diente/icono médico por defecto si no hay logo
                    Icon(
                        imageVector = Icons.Default.People, // Puedes usar un icono de diente si tienes el recurso, o este por defecto
                        contentDescription = null,
                        modifier = Modifier.size(150.dp),
                        tint = Color.Gray.copy(alpha = 0.1f)
                    )
                }
            }


        // --- DIÁLOGO DE CUMPLEAÑEROS (NUEVO) ---
        var mostrarDialogoCumple by remember { mutableStateOf(false) }
        if (mostrarDialogoCumple && cumpleanerosHoy.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoCumple = false },
                title = { Text("Cumpleañeros de Hoy 🎂", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        cumpleanerosHoy.forEach { paciente ->
                            Text("• ${paciente.nombre}", modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogoCumple = false }) { Text("Cerrar") }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // --- ENCABEZADO (Banner) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(colorAzulTitulo)
                .padding(bottom = 20.dp, top = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "¡Bienvenido, $nombreDoctor!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = nombreConsultorio,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "\"$fraseDelDia\"",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable(enabled = !estaCargandoTasa) {
                        pacienteViewModel.obtenerTasaBCV(forzar = true)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (estaCargandoTasa) {
                            // Indicador de carga pequeño
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            // Icono de moneda (Paid) en lugar de calculadora
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = if (estaCargandoTasa) "Actualizando..." else "Tasa BCV: ${String.format("%.2f", tasaActual)} Bs",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Resumen Diario",
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 14.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmartCardDiseno(
                titulo = "Citas Hoy",
                valor = numCitas.toString(),
                icono = Icons.Default.DateRange,
                color = Color(0xFF6366F1),
                modifier = Modifier.weight(1f).clickable {
                    navController.navigate(AppNavigation.CITAS)
                }
            )
            SmartCardDiseno(
                titulo = "Pendientes",
                valor = numNotas.toString(),
                icono = Icons.AutoMirrored.Filled.EventNote,
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f).clickable {
                    navController.navigate(AppNavigation.GESTION_CONSULTORIO)
                }
            )
            SmartCardDiseno(
                titulo = "Cumpleaños",
                valor = textoCumple,
                icono = Icons.Default.People,
                color = Color(0xFFF97316),
                modifier = Modifier.weight(1f).clickable {
                    // Si hay cumpleañeros, mostramos la lista. Si no, vamos a pacientes.
                    if (cumpleanerosHoy.isNotEmpty()) {
                        mostrarDialogoCumple = true
                    } else {
                        navController.navigate(AppNavigation.PACIENTES)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}
}


@Composable
fun SmartCardDiseno(
    titulo: String,
    valor: String,
    icono: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Franja de color superior
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(color))
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    titulo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = valor,
                    fontSize = if (valor.length > 12) 14.sp else 16.sp, // Se ajusta si es largo
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2, // Permite dos líneas si es nombre y apellido
                    lineHeight = 18.sp
                )
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
