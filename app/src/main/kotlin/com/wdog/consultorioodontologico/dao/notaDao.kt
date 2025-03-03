package com.wdog.consultorioodontologico.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wdog.consultorioodontologico.entities.Nota
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaDao {
    @Query("SELECT * FROM notas")
    fun obtenerTodasLasNotas(): Flow<List<Nota>>

    @Insert
    suspend fun insertarNota(nota: Nota)

    @Update
    suspend fun actualizarNota(nota: Nota)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun eliminarNota(id: Int)
}