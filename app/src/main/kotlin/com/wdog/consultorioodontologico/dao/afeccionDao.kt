package com.wdog.consultorioodontologico.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wdog.consultorioodontologico.entities.Afeccion

@Dao
interface AfeccionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAfeccion(afeccion: Afeccion)

    @Query("SELECT * FROM afecciones WHERE pacienteId = :pacienteId")
    suspend fun getAfeccionesByPacienteId(pacienteId: Long): List<Afeccion>

    @Query("DELETE FROM afecciones WHERE pacienteId = :pacienteId")
    suspend fun deleteByPacienteId(pacienteId: Long)

    @Query("SELECT * FROM afecciones WHERE pacienteId = :pacienteId AND tipoFigura = :tipoFigura")
    suspend fun getAfeccionesByTipo(pacienteId: Long, tipoFigura: String): List<Afeccion>
}