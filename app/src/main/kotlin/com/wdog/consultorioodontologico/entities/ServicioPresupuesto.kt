package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servicios_presupuesto")
data class ServicioPresupuesto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreServicio: String, // Ej: "Limpieza profunda"
    val precioSugerido: Double,// Ej: 50.0
    val categoria: String = "General", // Idea 6
    val colorHex: String = "#101084",    // Idea 5
    val iconoNombre: String = "MedicalServices" // Idea 8
)