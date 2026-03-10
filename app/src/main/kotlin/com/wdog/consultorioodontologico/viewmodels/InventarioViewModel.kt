package com.wdog.consultorioodontologico.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.dao.InventarioDao
import com.wdog.consultorioodontologico.entities.MaterialInventario
import com.wdog.consultorioodontologico.entities.KitProcedimiento
import com.wdog.consultorioodontologico.entities.Nota
import com.wdog.consultorioodontologico.sync.FirestoreSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val notaDao: com.wdog.consultorioodontologico.dao.NotaDao,
    private val firestoreSync: FirestoreSync,
    @param:dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    // --- FLUJOS OPTIMIZADOS CON STATEIN ---
    val materiales: StateFlow<List<MaterialInventario>> = inventarioDao.obtenerTodoElMaterial()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val kits: StateFlow<List<KitProcedimiento>> = inventarioDao.obtenerKits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _consumoActual = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val consumoActual = _consumoActual.asStateFlow()

    // --- LÓGICA DE CONSUMO ---

    fun ajustarConsumoTemporal(id: Long, delta: Double) {
        val mapa = _consumoActual.value.toMutableMap()
        val nuevoValor = (mapa[id] ?: 0.0) + delta
        if (nuevoValor <= 0) mapa.remove(id) else mapa[id] = nuevoValor
        _consumoActual.value = mapa
    }

    fun agregarKitAlConsumo(kit: KitProcedimiento) {
        val mapa = _consumoActual.value.toMutableMap()
        kit.composicion.forEach { (matId, cant) ->
            mapa[matId] = (mapa[matId] ?: 0.0) + cant
        }
        _consumoActual.value = mapa
    }

    fun limpiarConsumo() { _consumoActual.value = emptyMap() }

    fun procesarConsumoRealizado() {
        viewModelScope.launch {
            val consumo = _consumoActual.value
            if (consumo.isEmpty()) return@launch

            inventarioDao.registrarConsumo(consumo)

            // Sincronizar cambios de stock a Firebase de forma eficiente
            consumo.keys.forEach { id ->
                inventarioDao.obtenerMaterialPorId(id)?.let {
                    firestoreSync.syncMaterialToCloud(it)
                }
            }

            val cantidad = _consumoActual.value.size // Guardamos el número de items
            verificarAlertasStock()
            limpiarConsumo()

            // Toast para feedback visual
            android.widget.Toast.makeText(
                context,
                "¡Éxito! Se descontaron $cantidad productos del inventario",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    private val _seleccionados = MutableStateFlow<Set<Long>>(emptySet())
    val seleccionados = _seleccionados.asStateFlow()

    fun alternarSeleccion(id: Long) {
        val actual = _seleccionados.value.toMutableSet()
        if (id in actual) actual.remove(id) else actual.add(id)
        _seleccionados.value = actual
    }

    fun limpiarSeleccion() { _seleccionados.value = emptySet() }

    fun eliminarMaterialesSeleccionados() {
        viewModelScope.launch {
            val ids = _seleccionados.value

            // 1. Filtrar y eliminar Materiales
            val materialesAEliminar = materiales.value.filter { it.id in ids }
            materialesAEliminar.forEach { mat ->
                inventarioDao.eliminarMaterial(mat)
                firestoreSync.deleteMaterialFromCloud(mat.id)
            }

            // 2. Filtrar y eliminar Kits
            val kitsAEliminar = kits.value.filter { it.id in ids }
            kitsAEliminar.forEach { kit ->
                inventarioDao.eliminarKit(kit)
                firestoreSync.deleteKitFromCloud(kit.id)
            }

            limpiarSeleccion()
        }
    }



    // --- GESTIÓN DE MATERIALES Y KITS ---

    fun agregarMaterial(mat: MaterialInventario) {
        viewModelScope.launch {
            // Asegúrate que en tu InventarioDao el insert devuelva Long
            val nuevoId = inventarioDao.insertarMaterial(mat)
            firestoreSync.syncMaterialToCloud(mat.copy(id = nuevoId))
            verificarAlertasStock()
        }
    }

    fun agregarKit(nombre: String, composicion: Map<Long, Double>, idExistente: Long = 0) {
        viewModelScope.launch {
            // Si pasamos un idExistente (edición), Room lo reemplazará gracias al OnConflictStrategy.REPLACE
            val kit = KitProcedimiento(id = idExistente, nombreKit = nombre, composicion = composicion)
            val nuevoId = inventarioDao.insertarKit(kit)

            // Si era nuevo, usamos el nuevoId; si era edición, el idExistente se mantiene
            val idFinal = if (idExistente == 0L) nuevoId else idExistente
            firestoreSync.syncKitToCloud(kit.copy(id = idFinal))
        }
    }
// --- NUEVAS FUNCIONES: ELIMINAR Y EDITAR ---

    fun eliminarMaterial(mat: MaterialInventario) {
        viewModelScope.launch {
            inventarioDao.eliminarMaterial(mat)
            firestoreSync.deleteMaterialFromCloud(mat.id)
        }
    }
    fun actualizarStockManual(mat: MaterialInventario, nuevoStock: Double) {
        viewModelScope.launch {
            val materialActualizado = mat.copy(cantidadActual = nuevoStock)
            inventarioDao.actualizarMaterial(materialActualizado)
            firestoreSync.syncMaterialToCloud(materialActualizado)
            verificarAlertasStock()
        }
    }
    // --- SISTEMA DE ALERTAS (15 DÍAS Y STOCK) ---

    private fun verificarAlertasStock() {
        viewModelScope.launch {
            val hoy = System.currentTimeMillis()
            val quinceDiasEnMillis = 15L * 24 * 60 * 60 * 1000
            val todos = materiales.value

            val criticos = todos.filter {
                it.cantidadActual <= it.cantidadMinima ||
                        (it.fechaVencimiento != null && (it.fechaVencimiento - hoy) <= quinceDiasEnMillis)
            }

            if (criticos.isNotEmpty()) {
                // Mapeamos los materiales críticos al formato Pair(Texto, Boolean) que espera tu Nota
                val listaChecklist = criticos.map { mat ->
                    val motivo = when {
                        mat.fechaVencimiento != null && mat.fechaVencimiento <= hoy -> "¡VENCIDO!"
                        mat.fechaVencimiento != null && (mat.fechaVencimiento - hoy) <= quinceDiasEnMillis -> "Próximo a vencer"
                        else -> "Stock bajo: ${mat.cantidadActual} ${mat.unidad}"
                    }
                    Pair("${mat.nombre} ($motivo)", false)
                }

                val notaAlerta = Nota(
                    titulo = "Reposición de Insumos",
                    categoria = "Inventario",
                    colorTitulo = 0xFFa51b0b.toInt(), // Rojo Urgente
                    isListo = false
                ).copyWithChecklist(listaChecklist)

                val idGenerado = notaDao.insertarNota(notaAlerta)

                // 2. Sincronizamos con Firebase usando el ID real
                firestoreSync.syncNotaToCloud(notaAlerta.copy(id = idGenerado.toInt()))
            }
        }
    }
}