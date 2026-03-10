package com.wdog.consultorioodontologico.entities

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class Cuadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "first" to (first as? Any),
            "second" to (second as? Any),
            "third" to (third as? Any),
            "fourth" to (fourth as? Any)
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <A, B, C, D> fromMap(map: Map<String, Any>): Cuadruple<A, B, C, D>? {
            return try {
                Cuadruple(
                    first = map["first"] as? A ?: return null,
                    second = map["second"] as? B ?: return null,
                    third = map["third"] as? C ?: return null,
                    fourth = map["fourth"] as? D ?: return null
                )
            } catch (_: ClassCastException) {
                null
            }
        }
    }

    fun toAfeccion(pacienteId: Long): Afeccion? {
        // Si el ID es 0 o negativo, algo salió mal en la lógica del ViewModel
        if (pacienteId <= 0L) return null

        return try {
            val pos = first as? Offset ?: return null
            val col = second as? Color ?: return null
            Afeccion(
                offsetX = pos.x,
                offsetY = pos.y,
                color = col.value.toLong(),
                tipoFigura = third as? String ?: "pincel_8",
                orientacionInvertida = fourth as? Boolean ?: false,
                pacienteId = pacienteId // Este es el ID REAL que viene de la DB
            )
        } catch (_: Exception) { // Usamos el guion bajo para quitar la advertencia
            null
        }
    }
}