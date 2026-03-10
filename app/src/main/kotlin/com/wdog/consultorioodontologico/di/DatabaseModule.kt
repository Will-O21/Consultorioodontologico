package com.wdog.consultorioodontologico.di // Cambia "di" si lo pusiste en otra carpeta

import android.content.Context
import com.wdog.consultorioodontologico.dao.GastoDao
import com.wdog.consultorioodontologico.dao.LaboratorioDao
import com.wdog.consultorioodontologico.dao.PacienteDao
import com.wdog.consultorioodontologico.dao.ServicioPresupuestoDao
import com.wdog.consultorioodontologico.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 1. Le enseñamos a Hilt cómo construir la Base de Datos completa
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // 2. Le enseñamos cómo sacar el PacienteDao
    @Provides
    fun providePacienteDao(database: AppDatabase): PacienteDao {
        return database.pacienteDao()
    }

    @Provides
    fun provideNotaDao(database: AppDatabase): com.wdog.consultorioodontologico.dao.NotaDao {
        return database.notaDao()
    }

    // 3. Le enseñamos cómo sacar el GastoDao
    @Provides
    fun provideGastoDao(database: AppDatabase): GastoDao {
        return database.gastoDao()
    }

    // 4. Le enseñamos cómo sacar el ServicioPresupuestoDao
    @Provides
    fun provideServicioPresupuestoDao(database: AppDatabase): ServicioPresupuestoDao {
        return database.servicioPresupuestoDao()
    }
    @Provides
    fun provideLaboratorioDao(database: AppDatabase): LaboratorioDao {
        return database.laboratorioDao()
    }

    // 5. Agregar InventarioDao para el control de insumos
    @Provides
    fun provideInventarioDao(database: AppDatabase): com.wdog.consultorioodontologico.dao.InventarioDao {
        return database.inventarioDao()
    }

    // 6. Agregar DienteEstadoDao para el Odontograma
    @Provides
    fun provideDienteEstadoDao(database: AppDatabase): com.wdog.consultorioodontologico.dao.DienteEstadoDao {
        return database.dienteEstadoDao()
    }

}