package com.wdog.consultorioodontologico.dao

import androidx.room.*
import com.wdog.consultorioodontologico.entities.ServicioPresupuesto
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicioPresupuestoDao {
    @Query("SELECT * FROM servicios_presupuesto ORDER BY nombreServicio ASC")
    fun obtenerServicios(): Flow<List<ServicioPresupuesto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarServicio(servicio: ServicioPresupuesto): Long

    @Update
    suspend fun actualizarServicio(servicio: ServicioPresupuesto)

    @Delete
    suspend fun eliminarServicio(servicio: ServicioPresupuesto)
}