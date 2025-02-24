package com.wdog.consultorioodontologico.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wdog.consultorioodontologico.entities.Cita
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Insert
    suspend fun insert(cita: Cita)

    @Query("SELECT * FROM citas ORDER BY fechaHora DESC")
    fun getAllCitas(): Flow<List<Cita>> // Método correctamente implementado
}