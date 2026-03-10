package com.wdog.consultorioodontologico.dao

import androidx.room.*
import com.wdog.consultorioodontologico.entities.DienteEstadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DienteEstadoDao {
    @Query("SELECT * FROM dientes_estados WHERE presupuestoId = :presupuestoId")
    fun getEstadosPorPresupuesto(presupuestoId: Long): Flow<List<DienteEstadoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEstado(diente: DienteEstadoEntity)

    @Query("DELETE FROM dientes_estados WHERE presupuestoId = :presupuestoId AND dienteId = :dienteId")
    suspend fun eliminarEstadoDiente(presupuestoId: Long, dienteId: Int)

    @Query("DELETE FROM dientes_estados WHERE presupuestoId = :presupuestoId")
    suspend fun limpiarTodoElPresupuesto(presupuestoId: Long)
}