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
    val pacienteNombre: String, // Este campo sí se sincroniza
    val fechaHora: LocalDateTime,
    val observaciones: String,
    val asistio: Boolean? = null, // null = pendiente, true = asistió, false = faltó
    val procedimiento: String = ""
) {

    // Constructor vacío
    constructor() : this(0, 0, "", LocalDateTime.now(), "", null, ""    )

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "pacienteId" to pacienteId,
            "pacienteNombre" to pacienteNombre,
            "fechaHora" to fechaHora.toString(), // Convertir LocalDateTime a String
            "observaciones" to observaciones,
            "asistio" to asistio,         // NUEVO
            "procedimiento" to procedimiento // NUEVO
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Cita {
            return Cita(
                id = (map["id"] as? Long) ?: 0,
                pacienteId = map["pacienteId"] as Long,
                pacienteNombre = map["pacienteNombre"] as String,
                fechaHora = LocalDateTime.parse(map["fechaHora"] as String),
                observaciones = map["observaciones"] as String,
                asistio = map["asistio"] as? Boolean, // NUEVO
                procedimiento = (map["procedimiento"] as? String) ?: "" // NUEVO
            )
        }
    }
}