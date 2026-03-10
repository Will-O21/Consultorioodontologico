package com.wdog.consultorioodontologico.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wdog.consultorioodontologico.navigation.AppNavigation
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun PantallaDashboard(
    navController: NavController,
    citaViewModel: CitaViewModel,
    pacienteViewModel: PacienteViewModel
) {
    // Recolectamos las citas del ViewModel
    val citas by citaViewModel.obtenerCitasLocales().collectAsState(initial = emptyList())
    val hoy = LocalDate.now()

    // Filtramos las citas que coinciden con la fecha de hoy
    val citasDeHoy = remember(citas) {
        citas.filter { it.fechaHora.toLocalDate() == hoy }
    }

    // Lógica para cerrar automáticamente después de 10 segundos
    LaunchedEffect(Unit) {
        delay(10000)
        // Navega al inicio y limpia el dashboard de la memoria
        navController.navigate(AppNavigation.INICIO) {
            popUpTo(AppNavigation.DASHBOARD) { inclusive = true }
        }
    }

    // El Box ocupa toda la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .clickable(
                indication = ripple(), // Solución técnica para el error de Indication
                interactionSource = remember { MutableInteractionSource() }
            ) {
                navController.navigate(AppNavigation.INICIO) {
                    popUpTo(AppNavigation.DASHBOARD) { inclusive = true }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp), // Bordes más suaves
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ICONO DE BIENVENIDA
                Icon(
                    imageVector = Icons.Default.WavingHand, // Necesitarás importar este icono
                    contentDescription = null,
                    tint = Color(0xFFF97316), // Color naranja para resaltar
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¡Hola, Doc!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF101084)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp).width(50.dp),
                    thickness = 3.dp,
                    color = Color(0xFF101084).copy(alpha = 0.2f)
                )

                if (citasDeHoy.isNotEmpty()) {
                    // RESUMEN ENCAPSULADO
                    Surface(
                        color = Color(0xFF101084).copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Agenda de Hoy", fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                text = "${citasDeHoy.size} Pacientes",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // EL PRÓXIMO PACIENTE (Resaltado)
                    val proximaCita = citasDeHoy.firstOrNull() // El primero de la lista
                    Text(
                        text = "Primer turno:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = proximaCita?.pacienteNombre ?: "Cargando...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF094293)
                    )
                } else {
                    Text(
                        text = "Hoy es un día tranquilo.\n¡Disfruta tu café!",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // BOTÓN DE ACCIÓN MANUAL (Si no quiere esperar los 10s)
                Button(
                    onClick = {
                        navController.navigate(AppNavigation.INICIO) {
                            popUpTo(AppNavigation.DASHBOARD) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF101084)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entrar al Consultorio")
                }
            }
        }
    }
}