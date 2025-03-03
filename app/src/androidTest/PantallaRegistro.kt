import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wdog.consultorioodontologico.ui.registro.PantallaRegistro
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class PantallaRegistroTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRegistroPaciente() {
        // Simulamos las dependencias
        val mockNavController = mock(NavController::class.java)
        val mockPacienteViewModel = mock(PacienteViewModel::class.java)

        // Lanzamos la pantalla en un entorno de prueba
        composeTestRule.setContent {
            PantallaRegistro(navController = mockNavController)
        }

        // Simulamos la interacción del usuario
        composeTestRule.onNodeWithText("Nombre:").performTextInput("Juan")
        composeTestRule.onNodeWithText("Apellido:").performTextInput("Pérez")
        composeTestRule.onNodeWithText("Edad:").performTextInput("30")
        composeTestRule.onNodeWithText("Guardar Paciente").performClick()

        // Verificamos que el ViewModel fue llamado correctamente
        verify(mockPacienteViewModel).insertarPaciente(any())
    }
}