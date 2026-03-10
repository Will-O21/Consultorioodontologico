package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notas")
data class Nota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String = "",
    val cuerpo: String = "", // Aquí guardaremos el checklist internamente
    val colorTitulo: Int = 0,
    val categoria: String = "Mantenimiento", // NUEVO
    val isListo: Boolean = false,            // NUEVO (Para el Historial)
    val fechaCompletado: Long = 0L           // NUEVO
) {
    constructor() : this(0, "", "", 0, "Mantenimiento", false, 0L)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "titulo" to titulo,
            "cuerpo" to cuerpo,
            "colorTitulo" to colorTitulo,
            "categoria" to categoria,
            "isListo" to isListo,
            "fechaCompletado" to fechaCompletado
        )
    }

    // Funciones mágicas para convertir el texto en Checklist y viceversa
    fun obtenerChecklist(): List<Pair<String, Boolean>> {
        if (cuerpo.isBlank()) return emptyList()
        return cuerpo.split("\n").mapNotNull {
            val partes = it.split("|##|")
            if (partes.size == 2) Pair(partes[0], partes[1].toBoolean()) else null
        }
    }

    fun copyWithChecklist(checklist: List<Pair<String, Boolean>>): Nota {
        val nuevoCuerpo = checklist.joinToString("\n") { "${it.first}|##|${it.second}" }
        return this.copy(cuerpo = nuevoCuerpo)
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Nota {
            return Nota(
                id = (map["id"] as? Long)?.toInt() ?: (map["id"] as? Int) ?: 0,
                titulo = map["titulo"] as? String ?: "",
                cuerpo = map["cuerpo"] as? String ?: "",
                colorTitulo = (map["colorTitulo"] as? Long)?.toInt() ?: (map["colorTitulo"] as? Int) ?: 0,
                categoria = map["categoria"] as? String ?: "Mantenimiento",
                isListo = map["isListo"] as? Boolean ?: false,
                fechaCompletado = map["fechaCompletado"] as? Long ?: 0L
            )
        }
    }
}