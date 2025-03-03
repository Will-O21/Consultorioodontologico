package com.wdog.consultorioodontolgico

import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.viewmodels.CitaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class CitaViewModelTest {

    private lateinit var citaViewModel: CitaViewModel
    private lateinit var mockCitaRepository: CitaRepository

    @Before
    fun setUp() {
        // Simulamos el repositorio
        mockCitaRepository = mock(CitaRepository::class.java)
        citaViewModel = CitaViewModel(mockCitaRepository)
    }

    @Test
    fun testInsertarCita() = runTest {
        // Creamos una cita de prueba
        val cita = Cita(
            pacienteId = 1L,
            fechaHora = LocalDateTime.now(),
            observaciones = "Consulta de rutina"
        )

        // Ejecutamos el método a probar
        citaViewModel.insertarCita(cita)

        // Verificamos que el repositorio fue llamado
        verify(mockCitaRepository).insertarCita(cita)
    }
}