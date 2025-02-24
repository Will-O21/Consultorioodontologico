package com.wdog.consultorioodontologico.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wdog.consultorioodontologico.dao.CitaDao
import com.wdog.consultorioodontologico.dao.PacienteDao
import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.entities.Paciente


@Database(
    entities = [Paciente::class, Cita::class], // Agregar Cita
    version = 2, // Incrementar versión
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citaDao(): CitaDao
    abstract fun pacienteDao(): PacienteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "consultorio_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
