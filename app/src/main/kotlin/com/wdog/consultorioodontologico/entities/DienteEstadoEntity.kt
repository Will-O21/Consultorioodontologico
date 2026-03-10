package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.wdog.consultorioodontologico.ui.components.EstadoDiente

@Entity(
    tableName = "dientes_estados",
    foreignKeys = [
        ForeignKey(
            entity = ServicioPresupuesto::class, // Asumiendo que tu entidad de presupuesto se llama así
            parentColumns = ["id"],
            childColumns = ["presupuestoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["presupuestoId"])]
)
data class DienteEstadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presupuestoId: Long,
    val dienteId: Int, // Ej: 18, 11, etc.
    val carasMarcadas: String, // Guardaremos las caras como string separado por comas "0,1,3"
    val estadoEspecial: EstadoDiente = EstadoDiente.NORMAL,
    val nota: String = "", // Nueva: Para la sección E (Notas por diente)
    val esTratado: Boolean = false, // Nueva: Para la sección C (Historial/Capas)
    val fechaRegistro: Long = System.currentTimeMillis(), // Para Timeline
    val puenteCon: Int? = null // ID del diente con el que se conecta (Ej.: 13 con 14)
)
