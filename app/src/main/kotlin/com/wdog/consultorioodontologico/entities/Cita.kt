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
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pacienteId"])]
)
data class Cita(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pacienteId: Long,
    val pacienteNombre: String, // Nuevo campo para almacenar el nombre del paciente
    val fechaHora: LocalDateTime,
    val observaciones: String
)