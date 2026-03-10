package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kits_procedimiento")
data class KitProcedimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreKit: String = "",
    val categoria: String = "Kits",
    // Mapa de: ID del Material -> Cantidad que consume este kit
    val composicion: Map<Long, Double> = emptyMap()
)