package com.wdog.consultorioodontologico.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.wdog.consultorioodontologico.entities.Paciente
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {

    @Insert
    suspend fun insert(paciente: Paciente) // No retorna nada (Unit)

    @Query("SELECT * FROM pacientes")
    fun getAllPacientes(): Flow<List<Paciente>> // Usar Flow

    @Update
    suspend fun updatePaciente(paciente: Paciente) // Método para actualizar un paciente

    @Delete
    suspend fun deletePaciente(paciente: Paciente) // Método para eliminar un paciente
}