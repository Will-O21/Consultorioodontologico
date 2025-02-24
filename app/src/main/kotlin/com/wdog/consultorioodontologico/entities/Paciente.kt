package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
        import androidx.room.PrimaryKey

@Entity(tableName = "pacientes")
data class Paciente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val apellido: String,
    val edad: Int,
    val fotos: List<String>,
    val observaciones: String,
    val historiaClinica: String,
    val estadoPago: String = "Pendiente" // Nuevo campo
)
