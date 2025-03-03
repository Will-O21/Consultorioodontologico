package com.wdog.consultorioodontologico

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.wdog.consultorioodontologico.viewmodels.CitasViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class CitasScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAgregarCita() {
        // Simulamos el ViewModel
        val mockViewModel = mock(CitasViewModel::class.java)

        // Lanzamos la pantalla en un entorno de prueba
        composeTestRule.setContent {
            CitasScreen(viewModel = mockViewModel)
        }

        // Simulamos la interacción del usuario
        composeTestRule.onNodeWithText("Nombre").performTextInput("Juan")
        composeTestRule.onNodeWithText("Fecha").performTextInput("2023-10-01")
        composeTestRule.onNodeWithText("Hora").performTextInput("10:00")
        composeTestRule.onNodeWithText("Agregar Cita").performClick()

        // Verificamos que el ViewModel fue llamado correctamente
        verify(mockViewModel).agregarCita("Juan", "2023-10-01", "10:00")
    }
}