package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "afecciones",
    foreignKeys = [ForeignKey(
        entity = Paciente::class,
        parentColumns = ["id"],
        childColumns = ["pacienteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["pacienteId"])]
)
data class Afeccion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val offsetX: Float,
    val offsetY: Float,
    val color: Long, // Guardamos el valor numérico del color
    val tipoFigura: String,
    val pacienteId: Long = 0,
    val orientacionInvertida: Boolean = false,
    val tamanoPincel: Float = 8f
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "offsetX" to offsetX,
            "offsetY" to offsetY,
            "color" to color,
            "tipoFigura" to tipoFigura,
            "pacienteId" to pacienteId,
            "orientacionInvertida" to orientacionInvertida,
            "tamanoPincel" to tamanoPincel
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Afeccion {
            return Afeccion(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                offsetX = (map["offsetX"] as? Number)?.toFloat() ?: 0f,
                offsetY = (map["offsetY"] as? Number)?.toFloat() ?: 0f,
                color = (map["color"] as? Number)?.toLong() ?: 0L,
                tipoFigura = map["tipoFigura"] as? String ?: "",
                pacienteId = (map["pacienteId"] as? Number)?.toLong() ?: 0L,
                orientacionInvertida = map["orientacionInvertida"] as? Boolean ?: false,
                tamanoPincel = (map["tamanoPincel"] as? Number)?.toFloat() ?: 8f
            )
        }
    }
}