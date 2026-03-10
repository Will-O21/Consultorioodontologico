package com.wdog.consultorioodontologico.dao

import androidx.room.*
import com.wdog.consultorioodontologico.entities.Laboratorio
import kotlinx.coroutines.flow.Flow

@Dao
interface LaboratorioDao {
    @Query("SELECT * FROM laboratorios ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Laboratorio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(laboratorio: Laboratorio): Long

    @Update
    suspend fun actualizar(laboratorio: Laboratorio)

    @Delete
    suspend fun eliminar(laboratorio: Laboratorio)
}