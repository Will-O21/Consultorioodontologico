import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wdog.consultorioodontologico.ui.citas.PantallaAgregarCita
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class PantallaAgregarCitaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAgregarCita() {
        // Simulamos las dependencias
        val mockNavController = mock(NavController::class.java)
        val mockCitaViewModel = mock(CitaViewModel::class.java)
        val mockPacienteViewModel = mock(PacienteViewModel::class.java)

        // Lanzamos la pantalla en un entorno de prueba
        composeTestRule.setContent {
            PantallaAgregarCita(
                navController = mockNavController,
                citaViewModel = mockCitaViewModel,
                pacienteViewModel = mockPacienteViewModel
            )
        }

        // Simulamos la interacción del usuario
        composeTestRule.onNodeWithText("Seleccionar Fecha").performClick()
        composeTestRule.onNodeWithText("Seleccionar Hora").performClick()
        composeTestRule.onNodeWithText("Observaciones").performTextInput("Consulta de rutina")
        composeTestRule.onNodeWithText("Guardar Cita").performClick()

        // Verificamos que el ViewModel fue llamado correctamente
        verify(mockCitaViewModel).insertarCita(any())
    }
}