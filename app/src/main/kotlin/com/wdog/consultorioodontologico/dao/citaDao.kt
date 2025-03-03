package com.wdog.consultorioodontologico.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.wdog.consultorioodontologico.entities.Cita
import kotlinx.coroutines.flow.Flow

@Dao
interface CitaDao {
    @Insert
    suspend fun insert(cita: Cita)

    @Query("SELECT * FROM citas ORDER BY fechaHora DESC")
    fun getAllCitas(): Flow<List<Cita>> // Método correctamente implementado

    @Update
    suspend fun updateCita(cita: Cita) // Método para actualizar una cita

    @Delete
    suspend fun deleteCita(cita: Cita) // Método para eliminar una cita
}