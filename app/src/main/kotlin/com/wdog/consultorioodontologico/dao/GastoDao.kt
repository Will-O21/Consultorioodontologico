package com.wdog.consultorioodontologico.dao

import androidx.room.*
import com.wdog.consultorioodontologico.entities.Gasto
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    // Obtenemos solo los gastos que YA se hicieron (no próximos)
    @Query("SELECT * FROM gastos WHERE esProximo = 0 ORDER BY fecha DESC")
    fun obtenerGastosReales(): Flow<List<Gasto>>

    // IDEA 3: Obtener solo los recordatorios/gastos próximos
    @Query("SELECT * FROM gastos WHERE esProximo = 1 ORDER BY fecha ASC")
    fun obtenerRecordatorios(): Flow<List<Gasto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGasto(gasto: Gasto): Long

    @Delete
    suspend fun eliminarGasto(gasto: Gasto)

    @Query("SELECT SUM(monto) FROM gastos WHERE fecha >= :inicio AND fecha <= :fin AND esProximo = 0")
    fun obtenerSumaGastosRango(inicio: Long, fin: Long): Flow<Double?>
}