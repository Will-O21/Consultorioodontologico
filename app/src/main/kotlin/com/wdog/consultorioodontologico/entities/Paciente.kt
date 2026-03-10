package com.wdog.consultorioodontologico.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pacientes")
data class Paciente(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var nombre: String = "",
    var cedula: String = "",
    var fechaNacimiento: String = "",
    var edad: Int = 0,
    var telefono: String = "",
    var direccion: String = "",
    var enfermedadesSistemicas: String = "",
    var alergias: String = "",
    var habitos: String = "",
    var medicamentos: String = "",
    var motivoConsulta: String = "",

    // Cambiado a local: almacenan la ruta del archivo en el teléfono
    var fotoPerfil: String? = null,
    var fotosPlacas: List<String> = emptyList(),

    var planTratamiento: String = "",
    var estadoPago: String = "Pendiente",
    var monto: Double = 0.0,
    var abono: Double = 0.0,
    var fechaUltimoPago: Long = 0L // <--- NUEVO CAMPO PARA FINANZAS
) {
    // Constructor vacío para Firebase/Room corregido
    constructor() : this(
        0,      // id (Long)
        "",     // nombre (String)
        "",     // cedula (String)
        "",     // fechaNacimiento (String) -> Agregado en su posición correcta
        0,      // edad (Int) -> Ahora el 0 coincide con el Int de la edad
        "",     // telefono (String)
        "",     // direccion (String)
        "",     // enfermedadesSistemicas (String)
        "",     // alergias (String)
        "",     // habitos (String)
        "",     // medicamentos (String)
        "",     // motivoConsulta (String)
        null,   // fotoPerfil (String?)
        emptyList(), // fotosPlacas (List)
        "",     // planTratamiento (String)
        "Pendiente", // estadoPago (String)
        0.0,    // monto (Double)
        0.0,     // abono (Double)
        0L      // fechaUltimoPago (Long)
    )

    // Constructor desde Map (Firestore)
    constructor(map: Map<String, Any?>) : this() {
        id = (map["id"] as? Long) ?: 0
        nombre = map["nombre"] as? String ?: ""
        cedula = map["cedula"] as? String ?: ""
        fechaNacimiento = map["fechaNacimiento"] as? String ?: ""
        edad = (map["edad"] as? Number)?.toInt() ?: 0
        telefono = map["telefono"] as? String ?: ""
        direccion = map["direccion"] as? String ?: ""
        enfermedadesSistemicas = map["enfermedadesSistemicas"] as? String ?: ""
        alergias = map["alergias"] as? String ?: ""
        habitos = map["habitos"] as? String ?: ""
        medicamentos = map["medicamentos"] as? String ?: ""
        motivoConsulta = map["motivoConsulta"] as? String ?: ""
        fotoPerfil = map["fotoPerfil"] as? String
        @Suppress("UNCHECKED_CAST")
        fotosPlacas = map["fotosPlacas"] as? List<String> ?: emptyList()
        planTratamiento = map["planTratamiento"] as? String ?: ""
        estadoPago = map["estadoPago"] as? String ?: "Pendiente"
        monto = (map["monto"] as? Number)?.toDouble() ?: 0.0
        abono = (map["abono"] as? Number)?.toDouble() ?: 0.0
        fechaUltimoPago = (map["fechaUltimoPago"] as? Long) ?: 0L
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "cedula" to cedula,
            "fechaNacimiento" to fechaNacimiento,
            "edad" to edad,
            "telefono" to telefono,
            "direccion" to direccion,
            "enfermedadesSistemicas" to enfermedadesSistemicas,
            "alergias" to alergias,
            "habitos" to habitos,
            "medicamentos" to medicamentos,
            "motivoConsulta" to motivoConsulta,
            "fotoPerfil" to fotoPerfil,
            "fotosPlacas" to fotosPlacas,
            "planTratamiento" to planTratamiento,
            "estadoPago" to estadoPago,
            "monto" to monto,
            "abono" to abono,
            "fechaUltimoPago" to fechaUltimoPago
        )
    }
}