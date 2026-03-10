package com.wdog.consultorioodontologico.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.dao.DienteEstadoDao
import com.wdog.consultorioodontologico.entities.DienteEstadoEntity
import com.wdog.consultorioodontologico.sync.FirestoreSync
import com.wdog.consultorioodontologico.ui.components.EstadoDiente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.getValue // IMPORTANTE
import androidx.compose.runtime.setValue // IMPORTANTE
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OdontogramaViewModel @Inject constructor(
    private val dienteDao: DienteEstadoDao,
    private val firestoreSync: FirestoreSync
) : ViewModel() {

    // ID único (puede ser de Paciente o de Presupuesto)
    private var idPropietarioActual: Long = -1

    // Bandera para saber si estamos en modo Paciente o Presupuesto (opcional para lógica extra)
    private var esModoPaciente: Boolean = false

    // Sección B: Switch Pediatría
    private val _esModoPediatrico = MutableStateFlow(false)
    val esModoPediatrico = _esModoPediatrico.asStateFlow()

    // Sección E: Estado para las notas
    private val _notasDientes = MutableStateFlow<Map<Int, String>>(emptyMap())
    val notasDientes = _notasDientes.asStateFlow()

    private val _dientesMarcados = MutableStateFlow<Map<Int, Set<Int>>>(emptyMap())
    val dientesMarcados = _dientesMarcados.asStateFlow()

    private val _estadosEspeciales = MutableStateFlow<Map<Int, EstadoDiente>>(emptyMap())
    val estadosEspeciales = _estadosEspeciales.asStateFlow()

    // Sección C: Historial / Capas
    private val _dientesTratados = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val dientesTratados = _dientesTratados.asStateFlow()

    // Lógica de Puentes
    private val _puentes = MutableStateFlow<Map<Int, Int?>>(emptyMap())
    val puentes = _puentes.asStateFlow()

    // Estado para saber si estamos esperando el segundo diente de un puente
    var dienteEsperandoConexion by mutableStateOf<Int?>(null)

    fun marcarComoTratado(dienteId: Int, tratado: Boolean) {
        val mapa = _dientesTratados.value.toMutableMap()
        mapa[dienteId] = tratado
        _dientesTratados.value = mapa

        // Guardar cambio en la base de datos
        guardarEnDbYCloud(
            dienteId,
            _dientesMarcados.value[dienteId] ?: emptySet(),
            _estadosEspeciales.value[dienteId] ?: EstadoDiente.NORMAL
        )
    }
    // Función para cargar datos de un presupuesto específico
    /**
     * @param id El ID del paciente o del presupuesto.
     * @param esPaciente Si es true, usaremos un rango de IDs especial o lógica de guardado distinta si fuera necesario.
     */
    fun cargarOdontograma(id: Long, esPaciente: Boolean = false) {
        idPropietarioActual = id
        esModoPaciente = esPaciente

        viewModelScope.launch {
            // La base de datos ya filtra por Long, así que pasamos el ID del paciente directamente
            dienteDao.getEstadosPorPresupuesto(id).collect { lista ->
                val marcados = mutableMapOf<Int, Set<Int>>()
                val especiales = mutableMapOf<Int, EstadoDiente>()
                val notas = mutableMapOf<Int, String>()
                val tratados = mutableMapOf<Int, Boolean>()
                val puentesMap = mutableMapOf<Int, Int?>()
                lista.forEach { entidad ->
                    if (entidad.carasMarcadas.isNotBlank()) {
                        marcados[entidad.dienteId] = entidad.carasMarcadas.split(",").map { it.toInt() }.toSet()
                    }
                    especiales[entidad.dienteId] = entidad.estadoEspecial
                    notas[entidad.dienteId] = entidad.nota // Nueva línea
                    tratados[entidad.dienteId] = entidad.esTratado
                    puentesMap[entidad.dienteId] = entidad.puenteCon
                }
                _dientesMarcados.value = marcados
                _estadosEspeciales.value = especiales
                _notasDientes.value = notas // Nueva línea
                _dientesTratados.value = tratados
                _puentes.value = puentesMap
            }
        }
    }

    fun toggleModoPediatrico() {
        _esModoPediatrico.value = !_esModoPediatrico.value
    }

    fun guardarNotaDiente(dienteId: Int, nota: String) {
        val notasActuales = _notasDientes.value.toMutableMap()
        notasActuales[dienteId] = nota
        _notasDientes.value = notasActuales
        // Aquí llamaríamos a guardarEnDbYCloud incluyendo la nota
    }

    fun actualizarCaraDiente(dienteId: Int, cara: Int) {
        if (idPropietarioActual == -1L) return

        val mapaActual = _dientesMarcados.value.toMutableMap()
        val carasActuales = mapaActual[dienteId] ?: emptySet()
        val nuevasCaras = if (carasActuales.contains(cara)) carasActuales - cara else carasActuales + cara

        mapaActual[dienteId] = nuevasCaras
        _dientesMarcados.value = mapaActual

        // SOLO GUARDAR SI NO ES EL ID TEMPORAL DE PRESUPUESTO
        if (idPropietarioActual != -99L) {
            guardarEnDbYCloud(dienteId, nuevasCaras, _estadosEspeciales.value[dienteId] ?: EstadoDiente.NORMAL)
        }
    }

    fun rotarEstadoEspecial(dienteId: Int) {
        if (idPropietarioActual == -1L) return

        val mapaActual = _estadosEspeciales.value.toMutableMap()
        val actual = mapaActual[dienteId] ?: EstadoDiente.NORMAL
        val nuevo = when (actual) {
            EstadoDiente.NORMAL -> EstadoDiente.AUSENTE
            EstadoDiente.AUSENTE -> EstadoDiente.CORONA
            EstadoDiente.CORONA -> EstadoDiente.ENDODONCIA // Agregamos una secuencia lógica
            EstadoDiente.ENDODONCIA -> EstadoDiente.IMPLANTE
            EstadoDiente.IMPLANTE -> EstadoDiente.NORMAL
            else -> EstadoDiente.NORMAL // Cubre FRACTURA, CUELLO, etc., volviendo al estado base
        }
        mapaActual[dienteId] = nuevo
        _estadosEspeciales.value = mapaActual

        val caras = if (nuevo == EstadoDiente.AUSENTE) emptySet() else (_dientesMarcados.value[dienteId] ?: emptySet())
        // SOLO GUARDAR SI NO ES EL ID TEMPORAL DE PRESUPUESTO
        if (idPropietarioActual != -99L) {
            guardarEnDbYCloud(dienteId, caras, nuevo)
        }
    }

    private fun guardarEnDbYCloud(dienteId: Int, caras: Set<Int>, estado: EstadoDiente) {
        viewModelScope.launch(Dispatchers.IO) {
            val entidad = DienteEstadoEntity(
                presupuestoId = idPropietarioActual,
                dienteId = dienteId,
                carasMarcadas = caras.joinToString(","),
                estadoEspecial = estado,
                nota = _notasDientes.value[dienteId] ?: "",
                esTratado = _dientesTratados.value[dienteId] ?: false
            )
            dienteDao.insertarEstado(entidad)
            firestoreSync.syncDienteEstadoToCloud(entidad)
        }
    }

    fun iniciarOCompletarPuente(dienteId: Int) {
        val origen = dienteEsperandoConexion
        if (origen == null) {
            // Primer paso: seleccionamos el primer diente
            dienteEsperandoConexion = dienteId
        } else {
            // Segundo paso: si tocamos un diente distinto, creamos el puente
            if (origen != dienteId) {
                establecerConexionPuente(origen, dienteId)
            }
            dienteEsperandoConexion = null // Resetear estado
        }
    }

    fun establecerConexionPuente(dienteOrigen: Int, dienteDestino: Int) {
        val nuevosPuentes = _puentes.value.toMutableMap()
        nuevosPuentes[dienteOrigen] = dienteDestino
        _puentes.value = nuevosPuentes

        // Persistencia: Actualizamos el diente origen en DB con su nuevo puente
        viewModelScope.launch(Dispatchers.IO) {
            val caras = _dientesMarcados.value[dienteOrigen] ?: emptySet()
            val estado = _estadosEspeciales.value[dienteOrigen] ?: EstadoDiente.NORMAL

            val entidad = DienteEstadoEntity(
                presupuestoId = idPropietarioActual,
                dienteId = dienteOrigen,
                carasMarcadas = caras.joinToString(","),
                estadoEspecial = estado,
                puenteCon = dienteDestino, // Guardamos la conexión
                nota = _notasDientes.value[dienteOrigen] ?: "",
                esTratado = _dientesTratados.value[dienteOrigen] ?: false
            )
            dienteDao.insertarEstado(entidad)
            firestoreSync.syncDienteEstadoToCloud(entidad)
        }
    }

    fun setEstadoEspecial(dienteId: Int, nuevoEstado: EstadoDiente) {
        if (idPropietarioActual == -1L) return

        val mapaActual = _estadosEspeciales.value.toMutableMap()
        mapaActual[dienteId] = nuevoEstado
        _estadosEspeciales.value = mapaActual

        // Validación clínica: si es AUSENTE, limpiamos caras automáticamente
        val caras = if (nuevoEstado == EstadoDiente.AUSENTE) emptySet()
        else (_dientesMarcados.value[dienteId] ?: emptySet())

        if (nuevoEstado == EstadoDiente.AUSENTE) {
            val mapaCaras = _dientesMarcados.value.toMutableMap()
            mapaCaras[dienteId] = emptySet()
            _dientesMarcados.value = mapaCaras
        }

        if (idPropietarioActual != -99L) {
            guardarEnDbYCloud(dienteId, caras, nuevoEstado)
        }
    }

    fun limpiarDienteIndividual(dienteId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // Actualizamos estados locales
            _dientesMarcados.value -= dienteId
            _estadosEspeciales.value -= dienteId
            _notasDientes.value -= dienteId
            _dientesTratados.value -= dienteId
            _puentes.value -= dienteId

            // Guardar estado NORMAL en DB para "sobreescribir"
            guardarEnDbYCloud(dienteId, emptySet(), EstadoDiente.NORMAL)
        }
    }

    fun limpiarOdontograma() {
            if (idPropietarioActual == -1L) return
            viewModelScope.launch(Dispatchers.IO) {
                dienteDao.limpiarTodoElPresupuesto(idPropietarioActual)
                firestoreSync.limpiarOdontogramaFromCloud(idPropietarioActual)
            _dientesMarcados.value = emptyMap()
            _estadosEspeciales.value = emptyMap()
                _notasDientes.value = emptyMap() // Limpiar notas también
                _dientesTratados.value = emptyMap() // Limpiar historial también
        }
        }

    fun exportarAPdf(context: android.content.Context, nombrePaciente: String) {
        val generator = com.wdog.consultorioodontologico.util.PdfOdontogramaGenerator(context)
        // Aquí deberías obtener los datos de SharedPreferences o de tu DB de configuración
        val pref = context.getSharedPreferences("config_odontograma", android.content.Context.MODE_PRIVATE)

        generator.generarPdf(
            nombrePaciente = nombrePaciente,
            nombreDoctor = pref.getString("nombre_doctor", "No asignado") ?: "",
            nombreConsultorio = pref.getString("nombre_consultorio", "Mi Clínica") ?: "",
            logoPath = pref.getString("logo_path", null),
            dientesMarcados = _dientesMarcados.value,
            estadosEspeciales = _estadosEspeciales.value,
            notasDientes = _notasDientes.value,
            dientesTratados = _dientesTratados.value,
            puentes = _puentes.value,
            esModoPediatrico = _esModoPediatrico.value
        )
    }
}


