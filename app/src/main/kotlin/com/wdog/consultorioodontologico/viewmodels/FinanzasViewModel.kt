package com.wdog.consultorioodontologico.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.dao.GastoDao
import com.wdog.consultorioodontologico.dao.PacienteDao
import com.wdog.consultorioodontologico.entities.Gasto
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.sync.FirestoreSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanzasViewModel @Inject constructor(
    pacienteDao: PacienteDao,
    private val gastoDao: GastoDao,
    private val firestoreSync: FirestoreSync
) : ViewModel() {

    // Flujos base
    val todosLosGastos: Flow<List<Gasto>> = gastoDao.obtenerGastosReales()
    val recordatorios: Flow<List<Gasto>> = gastoDao.obtenerRecordatorios() // IDEA 3
    private val todosLosPacientes = pacienteDao.getAllPacientes()

    // 1. Ingresos Totales (Solo lo que ya se abonó)
    val ingresosTotales: Flow<Double> = todosLosPacientes.map { lista ->
        lista.sumOf { it.abono }
    }

    // 2. IDEA 5: Acceso Directo a Deudores (Monto total - Abono)
    val totalDeudaPacientes: Flow<Double> = todosLosPacientes.map { lista ->
        lista.sumOf { it.monto - it.abono }
    }

    // 3. IDEA 2: Desglose de Ingresos por Método
    val ingresosPorMetodo: Flow<Map<String, Double>> = todosLosPacientes.map { lista ->
        val desglose = mutableMapOf("Efectivo" to 0.0, "Binance" to 0.0, "Transferencia Bs" to 0.0)
        lista.forEach { paciente ->
            val metodo = when {
                paciente.estadoPago.contains("Efectivo", true) -> "Efectivo"
                paciente.estadoPago.contains("Binance", true) -> "Binance"
                paciente.estadoPago.contains("Transferencia", true) -> "Transferencia Bs"
                else -> "Efectivo" // Por defecto
            }
            desglose[metodo] = desglose.getOrDefault(metodo, 0.0) + paciente.abono
        }
        desglose
    }

    // 4. Gastos Totales
    val gastosTotales: Flow<Double> = todosLosGastos.map { it.sumOf { g -> g.monto } }

    // 5. Ganancia Neta
    val gananciaNeta: Flow<Double> = combine(ingresosTotales, gastosTotales) { ing, gas ->
        ing - gas
    }

    // --- Operaciones de Gastos ---

    fun agregarGasto(concepto: String, monto: Double, categoria: String, esProximo: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val nuevoGasto = Gasto(
                concepto = concepto,
                monto = monto,
                fecha = System.currentTimeMillis(),
                categoria = categoria,
                esProximo = esProximo,
                fechaVencimiento = if (esProximo) System.currentTimeMillis() + 86400000 else null // Ejemplo: +1 día
            )
            val id = gastoDao.insertarGasto(nuevoGasto)
            firestoreSync.syncGastoToCloud(nuevoGasto.copy(id = id))
        }
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch(Dispatchers.IO) {
            gastoDao.eliminarGasto(gasto)
            firestoreSync.deleteGastoFromCloud(gasto.id)
        }
    }

    fun obtenerGastosPorCategoria(): Flow<Map<String, Double>> = todosLosGastos.map { lista ->
        lista.groupBy { it.categoria }.mapValues { entry -> entry.value.sumOf { it.monto } }
    }
}