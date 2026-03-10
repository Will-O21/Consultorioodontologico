package com.wdog.consultorioodontologico.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wdog.consultorioodontologico.dao.AfeccionDao
import com.wdog.consultorioodontologico.dao.CitaDao
import com.wdog.consultorioodontologico.dao.DienteEstadoDao
import com.wdog.consultorioodontologico.dao.GastoDao
import com.wdog.consultorioodontologico.dao.InventarioDao
import com.wdog.consultorioodontologico.dao.LaboratorioDao
import com.wdog.consultorioodontologico.dao.NotaDao
import com.wdog.consultorioodontologico.dao.PacienteDao
import com.wdog.consultorioodontologico.dao.ServicioPresupuestoDao
import com.wdog.consultorioodontologico.entities.Afeccion
import com.wdog.consultorioodontologico.entities.Cita
import com.wdog.consultorioodontologico.entities.DienteEstadoEntity
import com.wdog.consultorioodontologico.entities.Gasto
import com.wdog.consultorioodontologico.entities.KitProcedimiento
import com.wdog.consultorioodontologico.entities.Laboratorio
import com.wdog.consultorioodontologico.entities.MaterialInventario
import com.wdog.consultorioodontologico.entities.Paciente
import com.wdog.consultorioodontologico.entities.Nota
import com.wdog.consultorioodontologico.entities.ServicioPresupuesto

@Database(
    entities = [Paciente::class, Cita::class, Nota::class, Afeccion::class,
        Gasto::class,
        ServicioPresupuesto::class, Laboratorio::class,
        DienteEstadoEntity::class,
        MaterialInventario::class, KitProcedimiento::class],
    version = 10,  // Incrementado debido a los cambios en el esquema
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citaDao(): CitaDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun notaDao(): NotaDao
    abstract fun afeccionDao(): AfeccionDao

    abstract fun gastoDao(): GastoDao
    // Nueva
    abstract fun servicioPresupuestoDao(): ServicioPresupuestoDao // Nueva

    abstract fun laboratorioDao(): LaboratorioDao

    abstract fun dienteEstadoDao(): DienteEstadoDao

    abstract fun inventarioDao(): InventarioDao // NUEVO

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}