package com.wdog.consultorioodontologico.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.wdog.consultorioodontologico.entities.Paciente
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Cambia esto
    suspend fun insertPaciente(paciente: Paciente): Long

    @Query("""
    SELECT * FROM pacientes 
    ORDER BY 
    CASE estadoPago
        WHEN 'Pendiente' THEN 1
        WHEN 'Abonó' THEN 2
        WHEN 'Al día' THEN 3
        WHEN 'Completo' THEN 3
        ELSE 4
    END ASC, nombre ASC
""")
    fun getAllPacientes(): Flow<List<Paciente>>

    @Update
    suspend fun updatePaciente(paciente: Paciente)

    @Delete
    suspend fun deletePaciente(paciente: Paciente)

    @Query("SELECT * FROM pacientes WHERE id = :id")
    fun getPacienteById(id: Long): LiveData<Paciente>

    // Nueva consulta para buscar por cédula
    @Query("SELECT * FROM pacientes WHERE cedula LIKE :cedula")
    suspend fun buscarPorCedula(cedula: String): List<Paciente>
}