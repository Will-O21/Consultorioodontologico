package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "material_inventario")
data class MaterialInventario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val categoria: String, // Bioseguridad, Restauración, etc.
    val cantidadActual: Double,
    val cantidadMinima: Double, // Punto de reorden
    val unidad: String, // Unidades, ml, Cajas
    val fechaVencimiento: Long? = null, // Timestamp
    val colorHex: String = "#101084"
)