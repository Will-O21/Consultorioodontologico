package com.wdog.consultorioodontologico.viewmodels

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.dao.ServicioPresupuestoDao
import com.wdog.consultorioodontologico.entities.ServicioPresupuesto
import com.wdog.consultorioodontologico.sync.FirestoreSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PresupuestoViewModel @Inject constructor(
    private val servicioDao: ServicioPresupuestoDao,
    private val firestoreSync: FirestoreSync
) : ViewModel() {

    // 1. Catálogo
    val listaServiciosBase: Flow<List<ServicioPresupuesto>> = servicioDao.obtenerServicios()

    // 2. Estado de la Calculadora: ID -> Cantidad (Idea 4)
    private val _itemsSeleccionados = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val itemsSeleccionados = _itemsSeleccionados.asStateFlow()

    // 2. Tasa BCV (Idea 2) - Inicializada en un valor base, se puede actualizar
    var tasaBCV = mutableDoubleStateOf(41.50)
        private set

    fun agregarServicioAlCatalogo(nombre: String, precio: Double, categoria: String, color: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Si el color es vacío, usamos el azul institucional por defecto
            val colorFinal = color.ifBlank { "#101084" }

            val nuevo = ServicioPresupuesto(
                nombreServicio = nombre,
                precioSugerido = precio,
                categoria = categoria,
                colorHex = colorFinal
            )
            val id = servicioDao.insertarServicio(nuevo)
            firestoreSync.syncServicioToCloud(nuevo.copy(id = id))
        }
    }

    fun actualizarServicioEnCatalogo(servicio: ServicioPresupuesto) {
        viewModelScope.launch(Dispatchers.IO) {
            // Aseguramos que nunca se guarde un color vacío por accidente en la edición
            val servicioValidado = if (servicio.colorHex.isBlank()) {
                servicio.copy(colorHex = "#101084")
            } else servicio

            servicioDao.actualizarServicio(servicioValidado)
            firestoreSync.syncServicioToCloud(servicioValidado)
        }
    }

    fun eliminarServicioDelCatalogo(servicio: ServicioPresupuesto) {
        viewModelScope.launch(Dispatchers.IO) {
            servicioDao.eliminarServicio(servicio)
            firestoreSync.deleteServicioFromCloud(servicio.id)
        }
    }

    // --- Lógica de la Calculadora (Idea 4) ---

    fun actualizarCantidad(servicioId: Long, nuevaCantidad: Int) {
        val mapaActual = _itemsSeleccionados.value.toMutableMap()
        if (nuevaCantidad <= 0) {
            mapaActual.remove(servicioId)
        } else {
            mapaActual[servicioId] = nuevaCantidad
        }
        _itemsSeleccionados.value = mapaActual
    }

    fun limpiarCalculadora() {
        _itemsSeleccionados.value = emptyMap()
    }

    // --- Generación de Mensaje (Idea 11: Personalizable) ---

    fun generarTextoPresupuesto(
        servicios: List<ServicioPresupuesto>,
        notaDoctor: String,
        tasaActual: Double
    ): String {
        val seleccionadosMap = _itemsSeleccionados.value
        val itemsFiltrados = servicios.filter { seleccionadosMap.containsKey(it.id) }

        val totalUSD = itemsFiltrados.sumOf { (seleccionadosMap[it.id] ?: 0) * it.precioSugerido }
        val totalBS = totalUSD * tasaActual

        val sb = StringBuilder()
        sb.append("🦷 *PRESUPUESTO ODONTOLÓGICO*\n")
        sb.append("-------------------------------------------\n")

        // Si el nombre viene en notaDoctor (encabezado), lo ponemos primero
        if (notaDoctor.isNotBlank()) {
            sb.append("$notaDoctor\n")
        }
        sb.append("-------------------------------------------\n\n")

        itemsFiltrados.forEach {
            val cant = seleccionadosMap[it.id] ?: 0
            val subtotalUSD = cant * it.precioSugerido
            val subtotalBS = subtotalUSD * tasaActual

            sb.append("✅ *${it.nombreServicio}*\n")
            sb.append("   Cant: $cant | $${String.format(java.util.Locale.US, "%.2f", subtotalUSD)} (Bs. ${String.format(java.util.Locale.US, "%.2f", subtotalBS)})\n\n")
        }

        sb.append("-------------------------------------------\n")
        sb.append("💰 *TOTAL ESTIMADO*\n")
        sb.append("💵 *USD: $${String.format(java.util.Locale.US, "%.2f", totalUSD)}*\n")
        sb.append("🇻🇪 *BS: ${String.format(java.util.Locale.US, "%.2f", totalBS)}*\n")
        sb.append("-------------------------------------------\n\n")

        sb.append("⚠️ _Nota: Monto en Bs. sujeto a cambios según la tasa oficial del BCV del día (${String.format(java.util.Locale.US, "%.2f", tasaActual)})._\n\n")
        sb.append("Estamos a su disposición para cualquier duda. ✨")

        return sb.toString()
    }
    // Estado para saber si está cargando la tasa (Idea 2)
    var cargandoTasa = mutableStateOf(false)
        private set

    fun obtenerTasaBCV() {
        if (cargandoTasa.value) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { cargandoTasa.value = true }

                val doc: org.jsoup.nodes.Document = org.jsoup.Jsoup.connect("https://www.bcv.org.ve/")
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get()

                val elementoDolar = doc.select("#dolar strong").first()
                if (elementoDolar != null) {
                    val tasaNum = elementoDolar.text().replace(".", "").replace(",", ".").trim().toDoubleOrNull()
                    if (tasaNum != null) {
                        withContext(Dispatchers.Main) {
                            tasaBCV.doubleValue = tasaNum
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BCV_ERROR", "${e.message}")
            } finally {
                withContext(Dispatchers.Main) { cargandoTasa.value = false }
            }
        }
    }
}