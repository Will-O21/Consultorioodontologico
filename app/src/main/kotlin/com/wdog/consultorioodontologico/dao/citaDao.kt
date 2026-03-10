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

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(cita: Cita): Long

    @Query("SELECT COUNT(*) FROM citas WHERE fechaHora >= :inicio AND fechaHora <= :fin")
    suspend fun contarCitasDelDia(inicio: java.time.LocalDateTime, fin: java.time.LocalDateTime): Int
    @Query("SELECT * FROM citas ORDER BY fechaHora ASC")
    fun getAllCitas(): Flow<List<Cita>>

    @Update
    suspend fun updateCita(cita: Cita)

    @Delete
    suspend fun deleteCita(cita: Cita)
}