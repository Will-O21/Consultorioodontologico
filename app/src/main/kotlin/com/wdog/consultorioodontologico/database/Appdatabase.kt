package com.wdog.consultorioodontologico.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wdog.consultorioodontologico.dao.CitaDao
import com.wdog.consultorioodontologico.dao.NotaDao
import com.wdog.consultorioodontologico.dao.PacienteDao
import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.entities.Nota

@Database(
    entities = [Paciente::class, Cita::class, Nota::class], // Agregar Nota a las entidades
    version = 3, // Incrementar versión debido al cambio en el esquema
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citaDao(): CitaDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun notaDao(): NotaDao // Nuevo DAO para las notas

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "consultorio_database"
                )
                    .fallbackToDestructiveMigration() // Permite migraciones destructivas (útil durante el desarrollo)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}