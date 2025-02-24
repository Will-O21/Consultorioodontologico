package com.wdog.consultorioodontologico.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wdog.consultorioodontologico.entities.Paciente
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {

    @Insert
    suspend fun insert(paciente: Paciente) // No retorna nada (Unit)

    @Query("SELECT * FROM pacientes")
    fun getAllPacientes(): Flow<List<Paciente>> // Usar Flow
}