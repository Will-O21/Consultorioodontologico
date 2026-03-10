package com.wdog.consultorioodontologico.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wdog.consultorioodontologico.ui.components.EstadoDiente
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val gson = Gson()

    // Conversión para LocalDateTime
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Conversión para List<String> (usado en Paciente.fotos aunque sea Transient)
    @TypeConverter
    fun stringListToJson(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun jsonToStringList(json: String?): List<String> {
        return if (json.isNullOrBlank()) {
            emptyList()
        } else {
            gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
        }
    }

    // Conversión para Color (usado en Afeccion)
    @TypeConverter
    fun colorToLong(color: androidx.compose.ui.graphics.Color?): Long? {
        return color?.value?.toLong() // Convertimos a Long explícitamente
    }

    @TypeConverter
    fun longToColor(value: Long?): androidx.compose.ui.graphics.Color? {
        // Usamos el constructor de ULong para reconstruir el color correctamente
        return value?.let { androidx.compose.ui.graphics.Color(it.toULong()) }
    }
    @TypeConverter
    fun fromEstadoDiente(value: EstadoDiente): String {
        return value.name
    }

    @TypeConverter
    fun toEstadoDiente(value: String): EstadoDiente {
        return EstadoDiente.valueOf(value)
    }

    @TypeConverter
    fun fromMap(map: Map<Long, Double>?): String? {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toMap(json: String?): Map<Long, Double> {
        if (json.isNullOrBlank()) return emptyMap()
        val type = object : TypeToken<Map<Long, Double>>() {}.type
        return gson.fromJson(json, type)
    }

}