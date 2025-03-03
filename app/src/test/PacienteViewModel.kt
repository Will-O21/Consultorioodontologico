package com.wdog.consultorioodontolgico
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.viewmodels.PacienteViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class PacienteViewModelTest {

    private lateinit var pacienteViewModel: PacienteViewModel
    private lateinit var mockPacienteRepository: PacienteRepository

    @Before
    fun setUp() {
        // Simulamos el repositorio
        mockPacienteRepository = mock(PacienteRepository::class.java)
        pacienteViewModel = PacienteViewModel(mockPacienteRepository)
    }

    @Test
    fun testInsertarPaciente() = runTest {
        // Creamos un paciente de prueba
        val paciente = Paciente(
            nombre = "Juan",
            apellido = "Pérez",
            edad = 30,
            fotos = emptyList(),
            historiaClinica = "",
            observaciones = "Ninguna",
            estadoPago = "Pendiente"
        )

        // Ejecutamos el método a probar
        pacienteViewModel.insertarPaciente(paciente)

        // Verificamos que el repositorio fue llamado
        verify(mockPacienteRepository).insertarPaciente(paciente)
    }
}