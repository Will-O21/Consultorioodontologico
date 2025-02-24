package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.CASCADE // Opcional: Define el comportamiento al eliminar un paciente
        )
    ],
    indices = [Index(value = ["pacienteId"])] // Agregar un índice a pacienteId
)
data class Cita(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pacienteId: Long, // Clave foránea que referencia a la tabla de pacientes
    val fechaHora: LocalDateTime,
    val observaciones: String
)