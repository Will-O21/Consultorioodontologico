package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notas")
data class Nota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val cuerpo: String,
    val colorTitulo: Int // Guardamos el color como un entero (ARGB)
)