package com.wdog.consultorioodontologico.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.wdog.consultorioodontologico.entities.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSync @Inject constructor(
    val firestore: FirebaseFirestore
) {
    val pacientesCollection = firestore.collection("pacientes")
    val citasCollection = firestore.collection("citas")
    val notasCollection = firestore.collection("notas")
    val afeccionesCollection = firestore.collection("afecciones")

    val gastosCollection = firestore.collection("gastos")
    val catalogoCollection = firestore.collection("catalogo_precios")
    val laboratoriosCollection = firestore.collection("laboratorios")

    val dientesEstadosCollection = firestore.collection("dientes_estados") // Nueva collect

    val inventarioCollection = firestore.collection("inventario") // NUEVA
    val kitsCollection = firestore.collection("kits_procedimiento") // NUEVA

    var pacientesListener: ListenerRegistration? = null

    // -------------------- Sincronización Bidireccional --------------------
    fun startListeningForPacientesChanges(onChange: (List<Paciente>) -> Unit) {
        // Se corrige la sintaxis del listener y el manejo de nulos
        pacientesListener = pacientesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Solo logueamos el error, no crashers
                android.util.Log.e("FirestoreSync", "Error escuchando pacientes", error)
                return@addSnapshotListener
            }

            val pacientes = snapshot?.documents?.mapNotNull { doc ->
                // Usamos toObject en lugar de fromMap manual
                doc.toObject(Paciente::class.java)
            } ?: emptyList()

            onChange(pacientes)
        }
    }

    fun stopListening() {
        pacientesListener?.remove()
    }

    // -------------------- Métodos CRUD para Pacientes --------------------
    suspend fun syncPacienteToCloud(paciente: Paciente) {
        // Firestore puede guardar el objeto directamente sin toMap()
        pacientesCollection.document(paciente.id.toString())
            .set(paciente).await()
    }

    suspend fun getPacientesFromCloud(): List<Paciente> {
        return try {
            pacientesCollection.get().await()
                .documents.mapNotNull { doc ->
                    doc.toObject(Paciente::class.java)
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // -------------------- Métodos para Citas --------------------
    suspend fun syncCitaToCloud(cita: Cita) {
        // Usamos toMap() para asegurar que la fecha se guarde como String
        citasCollection.document(cita.id.toString())
            .set(cita.toMap()).await()
    }
    suspend fun deleteCita(citaId: Long) {
        citasCollection.document(citaId.toString()).delete().await()
    }

    suspend fun getCitasFromCloud(): List<Cita> {
        return try {
            citasCollection.get().await()
                .documents.mapNotNull { doc ->
                    doc.toObject(Cita::class.java)
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // -------------------- Métodos para Notas --------------------
    suspend fun syncNotaToCloud(nota: Nota) {
        notasCollection.document(nota.id.toString())
            .set(nota).await()
    }

    suspend fun getNotasFromCloud(): List<Nota> {
        return try {
            notasCollection.get().await()
                .documents.mapNotNull { doc ->
                    doc.toObject(Nota::class.java)
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun deleteNotaFromCloud(notaId: Int) {
        notasCollection.document(notaId.toString()).delete().await()
    }

    // -------------------- Métodos para Afecciones --------------------
    suspend fun syncAfeccionesToCloud(pacienteId: Long, afecciones: List<Afeccion>) {
        // 1. Primero borramos las que ya existen para este paciente en la nube
        deleteAfeccionesFromCloud(pacienteId)

        // 2. Ahora subimos las nuevas
        if (afecciones.isEmpty()) return

        val batch = firestore.batch()
        afecciones.forEach { afeccion ->
            // Dejamos que Firestore genere ID únicos aleatorios para cada punto
            val docRef = afeccionesCollection.document()
            batch.set(docRef, afeccion)
        }
        batch.commit().await()
    }

    suspend fun deletePacienteFromCloud(pacienteId: Long) {
        pacientesCollection.document(pacienteId.toString()).delete().await()
    }

    suspend fun deleteAfeccionesFromCloud(pacienteId: Long) {
        // Nota: Asegúrate de que en Firestore el campo se llame igual que en tu modelo (ej. "pacienteId")
        val query = afeccionesCollection.whereEqualTo("pacienteId", pacienteId).get().await()
        val batch = firestore.batch()
        query.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }
    suspend fun syncGastoToCloud(gasto: Gasto) {
        gastosCollection.document(gasto.id.toString()).set(gasto).await()
    }

    suspend fun deleteGastoFromCloud(gastoId: Long) {
        gastosCollection.document(gastoId.toString()).delete().await()
    }

    suspend fun getGastosFromCloud(): List<Gasto> = try {
        gastosCollection.get().await().documents.mapNotNull { it.toObject(Gasto::class.java) }
    } catch (_: Exception) { emptyList() }

    // -------------------- Métodos para Catálogo (Presupuestos) --------------------
    suspend fun syncServicioToCloud(servicio: ServicioPresupuesto) {
        catalogoCollection.document(servicio.id.toString()).set(servicio).await()
    }

    suspend fun deleteServicioFromCloud(servicioId: Long) {
        catalogoCollection.document(servicioId.toString()).delete().await()
    }

    suspend fun getCatalogoFromCloud(): List<ServicioPresupuesto> = try {
        catalogoCollection.get().await().documents.mapNotNull { it.toObject(ServicioPresupuesto::class.java) }
    } catch (_: Exception) { emptyList() }

    // -------------------- Métodos para Laboratorios --------------------
    suspend fun syncLaboratorioToCloud(lab: Laboratorio) {
        laboratoriosCollection.document(lab.id.toString()).set(lab).await()
    }

    suspend fun deleteLaboratorioFromCloud(labId: Long) {
        laboratoriosCollection.document(labId.toString()).delete().await()
    }

    suspend fun getLaboratoriosFromCloud(): List<Laboratorio> = try {
        laboratoriosCollection.get().await().documents.mapNotNull { it.toObject(Laboratorio::class.java) }
    } catch (_: Exception) { emptyList() }

    // -------------------- Métodos para Odontograma --------------------
    suspend fun syncDienteEstadoToCloud(estado: DienteEstadoEntity) {
        // Usamos una clave compuesta "presupuestoId_dienteId" para evitar duplicados en la nube
        val docId = "${estado.presupuestoId}_${estado.dienteId}"
        dientesEstadosCollection.document(docId).set(estado).await()
    }

    suspend fun deleteDienteEstadoFromCloud(presupuestoId: Long, dienteId: Int) {
        val docId = "${presupuestoId}_${dienteId}"
        dientesEstadosCollection.document(docId).delete().await()
    }

    suspend fun getDientesEstadosFromCloud(presupuestoId: Long): List<DienteEstadoEntity> = try {
        dientesEstadosCollection.whereEqualTo("presupuestoId", presupuestoId)
            .get().await().documents.mapNotNull { it.toObject(DienteEstadoEntity::class.java) }
    } catch (_: Exception) { emptyList() }

    suspend fun limpiarOdontogramaFromCloud(presupuestoId: Long) {
        val query = dientesEstadosCollection.whereEqualTo("presupuestoId", presupuestoId).get().await()
        val batch = firestore.batch()
        query.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    // -------------------- Métodos para Inventario --------------------
    suspend fun syncMaterialToCloud(material: MaterialInventario) {
        inventarioCollection.document(material.id.toString()).set(material).await()
    }

    suspend fun deleteMaterialFromCloud(materialId: Long) {
        inventarioCollection.document(materialId.toString()).delete().await()
    }

    suspend fun syncKitToCloud(kit: KitProcedimiento) {
        kitsCollection.document(kit.id.toString()).set(kit).await()
    }

    suspend fun getInventarioFromCloud(): List<MaterialInventario> = try {
        inventarioCollection.get().await().documents.mapNotNull { it.toObject(MaterialInventario::class.java) }
    } catch (_: Exception) { emptyList() }

    suspend fun getKitsFromCloud(): List<KitProcedimiento> = try {
        kitsCollection.get().await().documents.mapNotNull { it.toObject(KitProcedimiento::class.java) }
    } catch (_: Exception) { emptyList() }

    // --- NUEVAS FUNCIONES DE BORRADO ---
    suspend fun deleteKitFromCloud(kitId: Long) {
        kitsCollection.document(kitId.toString()).delete().await()
    }

}