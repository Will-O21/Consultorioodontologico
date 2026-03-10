package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laboratorios")
data class Laboratorio(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val contacto: String,
    val telefono: String,
    val especialidad: String, // Aquí guardaremos "Técnico", "Proveedor" o "Laboratorio"
    val notas: String = "",   // Aquí guardaremos el "Propósito"
    val estado: String = "Solo Registro", // "Pendiente", "Recibido", "Solo Registro"
    val fechaEntrega: String = ""         // Fecha opcional
)