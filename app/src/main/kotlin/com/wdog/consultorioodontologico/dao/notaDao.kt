package com.wdog.consultorioodontologico.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wdog.consultorioodontologico.entities.Nota
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaDao {
    @Query("SELECT * FROM notas")
    fun obtenerTodasLasNotas(): Flow<List<Nota>>
    @Query("SELECT * FROM notas")
    suspend fun obtenerTodasLasNotasSimple(): List<Nota>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarNota(nota: Nota): Long // Cambiado para obtener el ID real
    @Update
    suspend fun actualizarNota(nota: Nota)

    @Query("DELETE FROM notas WHERE id = :id")
    suspend fun eliminarNota(id: Int)

    @androidx.room.Delete
    suspend fun eliminarMultiplesNotas(notas: List<Nota>)
}