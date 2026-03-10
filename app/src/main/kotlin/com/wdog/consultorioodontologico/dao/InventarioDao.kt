package com.wdog.consultorioodontologico.dao

import androidx.room.*
import com.wdog.consultorioodontologico.entities.MaterialInventario
import com.wdog.consultorioodontologico.entities.KitProcedimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface InventarioDao {
    // Materiales
    @Query("SELECT * FROM material_inventario ORDER BY nombre ASC")
    fun obtenerTodoElMaterial(): Flow<List<MaterialInventario>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMaterial(material: MaterialInventario): Long // Cambia Unit por Long

    @Query("SELECT * FROM material_inventario WHERE id = :id")
    suspend fun obtenerMaterialPorId(id: Long): MaterialInventario?

    // Kits
    @Query("SELECT * FROM kits_procedimiento ORDER BY nombreKit ASC")
    fun obtenerKits(): Flow<List<KitProcedimiento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarKit(kit: KitProcedimiento): Long // Cambia Unit por Long

    // Lógica de Consumo
    @Transaction
    suspend fun registrarConsumo(materialesId: Map<Long, Double>) {
        materialesId.forEach { (id, cantidadARestar) ->
            val mat = obtenerMaterialPorId(id)
            mat?.let {
                val nuevaCantidad = (it.cantidadActual - cantidadARestar).coerceAtLeast(0.0)
                actualizarMaterial(it.copy(cantidadActual = nuevaCantidad))
            }
        }
    }

    @Update
    suspend fun actualizarMaterial(material: MaterialInventario)

    @Delete
    suspend fun eliminarMaterial(material: MaterialInventario)

    @Delete
    suspend fun eliminarKit(kit: KitProcedimiento) // <-- AÑADIR ESTA LÍNEA
}