package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val concepto: String,       // Ej: "Pago de Renta", "Compra de Resinas"
    val monto: Double,         // Ej: 500.0
    val fecha: Long,           // System.currentTimeMillis()
    val categoria: String,      // "Materiales", "Servicios", "Renta", "Otros"
    val esProximo: Boolean = false, // IDEA 3: Identificar si es recordatorio
    val fechaVencimiento: Long? = null // Para gastos a futuro
)