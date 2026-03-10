package com.wdog.consultorioodontologico.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wdog.consultorioodontologico.database.AppDatabase
import com.wdog.consultorioodontologico.entities.*
import com.wdog.consultorioodontologico.sync.FirestoreSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableDoubleStateOf // Cambio por sugerencia de Android
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

@HiltViewModel
class PacienteViewModel @Inject constructor(
    application: Application,
    private val firestoreSync: FirestoreSync
) : AndroidViewModel(application) {

    private val pacienteDao = AppDatabase.getDatabase(application).pacienteDao()
    private val afeccionDao = AppDatabase.getDatabase(application).afeccionDao()

    val todosLosPacientes: Flow<List<Paciente>> = pacienteDao.getAllPacientes()

    init {
        try {
            firestoreSync.startListeningForPacientesChanges { pacientes ->
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        pacientes.forEach { paciente ->
                            pacienteDao.insertPaciente(paciente)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DATABASE_ERROR", "Error insertando desde nube", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("INIT_ERROR", "Error al iniciar ViewModel", e)
        }
    }
// ==================== LÓGICA DE ALMACENAMIENTO LOCAL ====================

    private fun guardarImagenLocal(uri: Uri): String? {
        return try {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(uri)
            val nombreArchivo = "img_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.jpg"
            val archivo = File(context.filesDir, nombreArchivo)

            inputStream?.use { entrada ->
                FileOutputStream(archivo).use { salida ->
                    entrada.copyTo(salida)
                }
            }
            archivo.absolutePath // Ruta interna privada de la app
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun eliminarImagenLocal(ruta: String?) {
        if (ruta == null) return
        try {
            val archivo = File(ruta)
            if (archivo.exists()) archivo.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== OPERACIONES CRUD ====================

    fun insertarPaciente(paciente: Paciente, afecciones: List<Afeccion>, fotoPerfilUri: Uri?, placasUris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Guardar fotos localmente y obtener rutas
                val rutaPerfil = fotoPerfilUri?.let { guardarImagenLocal(it) }
                val rutasPlacas = placasUris.take(2).mapNotNull { guardarImagenLocal(it) }

                // 2. Crear objeto final con rutas
                val pacienteConFotos = paciente.copy(
                    fotoPerfil = rutaPerfil,
                    fotosPlacas = rutasPlacas
                )

                // 3. Insertar en Room
                val idGenerado = pacienteDao.insertPaciente(pacienteConFotos)

                // 4. Insertar afecciones vinculadas
                afecciones.forEach { afeccion ->
                    afeccionDao.insertAfeccion(afeccion.copy(pacienteId = idGenerado, id = 0))
                }

                // 5. Sincronizar datos (texto) a Firestore
                firestoreSync.syncPacienteToCloud(pacienteConFotos.copy(id = idGenerado))
                val afeccionesVinculadas = afecciones.map { it.copy(pacienteId = idGenerado, id = 0) }
                firestoreSync.syncAfeccionesToCloud(idGenerado, afeccionesVinculadas)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarPaciente(
        paciente: Paciente,
        afecciones: List<Afeccion>,
        nuevaFotoPerfil: Uri?,
        nuevasPlacas: List<Uri>,
        borrarPerfilAnterior: Boolean,
        placasABorrar: List<String> = emptyList()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Manejo de Foto de Perfil CORREGIDO
                var rutaPerfilFinal = paciente.fotoPerfil

                // 1. Si se pidió borrar la foto, la eliminamos y ponemos la ruta en null
                if (borrarPerfilAnterior) {
                    eliminarImagenLocal(paciente.fotoPerfil)
                    rutaPerfilFinal = null
                }

                // 2. Si el usuario, además, mandó una foto nueva, la guardamos
                if (nuevaFotoPerfil != null) {
                    rutaPerfilFinal = guardarImagenLocal(nuevaFotoPerfil)
                }

                // Manejo de Placas
                placasABorrar.forEach { eliminarImagenLocal(it) }
                val placasRestantes = paciente.fotosPlacas.filter { it !in placasABorrar }
                val nuevasRutasPlacas = nuevasPlacas.mapNotNull { guardarImagenLocal(it) }
                val rutasPlacasFinales = (placasRestantes + nuevasRutasPlacas).take(2)

                // Creamos la copia final para actualizar
                val pacienteActualizado = paciente.copy(
                    fotoPerfil = rutaPerfilFinal,
                    fotosPlacas = rutasPlacasFinales
                )
                if (paciente.id == 0L) return@launch
                // Actualizar DB y Cloud
                val afeccionesListas = afecciones.map { it.copy(pacienteId = paciente.id, id = 0) }

// Actualizar DB Local
                pacienteDao.updatePaciente(pacienteActualizado)
                afeccionDao.deleteByPacienteId(paciente.id)
                afeccionesListas.forEach { afeccionDao.insertAfeccion(it) }

// Actualizar Cloud
                firestoreSync.syncPacienteToCloud(pacienteActualizado)
                firestoreSync.syncAfeccionesToCloud(paciente.id, afeccionesListas) // Usar la lista limpia
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun eliminarPaciente(paciente: Paciente) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Eliminar archivos físicos
                eliminarImagenLocal(paciente.fotoPerfil)
                paciente.fotosPlacas.forEach { eliminarImagenLocal(it) }

                // 2. Eliminar de Room
                pacienteDao.deletePaciente(paciente)
                afeccionDao.deleteByPacienteId(paciente.id)

                // 3. Eliminar de Firestore
                firestoreSync.deletePacienteFromCloud(paciente.id)
                firestoreSync.deleteAfeccionesFromCloud(paciente.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun obtenerAfeccionesPorPacienteId(pacienteId: Long): List<Afeccion> {
        return withContext(Dispatchers.IO) {
            afeccionDao.getAfeccionesByPacienteId(pacienteId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreSync.stopListening()
    }
    fun obtenerPacientePorId(pacienteId: Long): androidx.lifecycle.LiveData<Paciente> {
        return pacienteDao.getPacienteById(pacienteId)
    }

    // --- LÓGICA DE TASA BCV CORRECTA ---
    private val prefs = application.getSharedPreferences("config_consultorio", Context.MODE_PRIVATE)

    // Al iniciar, intentamos leer la última tasa guardada, si no existe, usamos el valor base
    var tasaBCV = mutableDoubleStateOf(prefs.getFloat("ultima_tasa_bcv", 411.09f).toDouble())
        private set

    var cargandoTasa = mutableStateOf(false)
        private set

    private val _mensajeToast = MutableSharedFlow<String>()
    val mensajeToast = _mensajeToast.asSharedFlow()

    fun obtenerTasaBCV(forzar: Boolean = false) {
        if (cargandoTasa.value) return
        if (!forzar) {
            val ultimaVez = prefs.getLong("timestamp_tasa", 0L)
            val ahora = System.currentTimeMillis()
            if ((ahora - ultimaVez) < (4 * 60 * 60 * 1000)) { // 4 horas
                android.util.Log.d("TASA_CACHE", "Usando valor en memoria (reciente)")
                return
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { cargandoTasa.value = true }

                // Lista de fuentes en orden de prioridad
                // Lista de fuentes optimizada (Fuentes estables)
                val fuentes = listOf(
                    { intentarBCV() },
                    { intentarBanplus() },
                    { intentarMercantil() },
                    { intentarMonitorDolar() }
                )

                var tasaEncontrada: Double? = null
                val nombresFuentes = listOf("BCV", "Banplus", "Mercantil", "MonitorDolar")


                val tasasValidas = mutableMapOf<String, Double>()
                // Fecha de hoy para validación
                val fechaHoy = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())

                for (i in fuentes.indices) {
                    try {
                        val tasa = fuentes[i]()

                        // Mantenemos tu filtro de 410.0
                        if (tasa != null && tasa > 410.0) {
                            tasasValidas[nombresFuentes[i]] = tasa
                            android.util.Log.d("TASA_CHECK", "✅ ${nombresFuentes[i]}: $tasa (Fecha validada: $fechaHoy)")
                        } else {
                            android.util.Log.w("TASA_CHECK", "❌ ${nombresFuentes[i]}: Valor inválido ($tasa)")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("TASA_CHECK", "💥 ${nombresFuentes[i]}: Error -> ${e.message}")
                    }
                }

                // IMPORTANTE: Buscamos la más alta y la asignamos
                val mejorFuente = tasasValidas.maxByOrNull { it.value }
                if (mejorFuente != null) {
                    // Redondeamos a 2 decimales para limpieza visual
                    tasaEncontrada = Math.round(mejorFuente.value * 100.0) / 100.0
                    android.util.Log.i("TASA_FINAL", "🏆 Ganadora: ${mejorFuente.key} con $tasaEncontrada (Original: ${mejorFuente.value})")
                }

                withContext(Dispatchers.Main) {
                    if (tasaEncontrada != null && tasaEncontrada > 0) {
                        tasaBCV.doubleValue = tasaEncontrada
                        // Guardamos en SharedPreferences para la próxima vez
                        prefs.edit {
                            putFloat("ultima_tasa_bcv", tasaEncontrada.toFloat())
                            putLong("timestamp_tasa", System.currentTimeMillis())
                        }

                        // Corregimos el error de Locale
                        val tasaFormateada = String.format(java.util.Locale.US, "%.2f", tasaEncontrada)
                        _mensajeToast.emit("Tasa actualizada: $tasaFormateada Bs.")
                    } else {
                        _mensajeToast.emit("Error: No se pudo conectar con ninguna fuente.")
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _mensajeToast.emit("Error de red. Verifique su conexión.")
                }
            } finally {
                withContext(Dispatchers.Main) { cargandoTasa.value = false }
            }
        }
    }

    // --- FUENTES DE RESPALDO ---

    private fun intentarBCV(): Double? {
        return try {
            val doc = org.jsoup.Jsoup.connect("https://www.bcv.org.ve/")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                // User-Agent de Chrome actualizado
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .header("Cache-Control", "no-cache")
                .timeout(25000)
                .ignoreHttpErrors(true)
                .get()

            // Verificamos si la página muestra la fecha actual (opcional, para log)
            val fechaPagina = doc.select(".date-display-single").text()
            val fechaHoySimple = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US).format(java.util.Date())

            // Si la fecha de la página no contiene el día de hoy (ej. 04/03/2026), retornamos null
            // Nota: Se usa un check flexible porque el BCV a veces escribe "04 Marzo"
            if (!fechaPagina.contains(fechaHoySimple.split("/")[0])) {
                android.util.Log.w("TASA_FECHA", "BCV desactualizado: $fechaPagina vs Hoy: $fechaHoySimple")
                return null
            }
            android.util.Log.d("TASA_FECHA", "✅ BCV al día: $fechaPagina")

            // Buscamos el div que contiene el dólar y luego el strong
            val texto = doc.select("#dolar strong").text().ifEmpty {
                doc.select(".field-content:contains(USD) + div strong").text().ifEmpty {
                    doc.getElementsContainingText("USD").first()?.parent()?.select("strong")?.text()
                }
            }

            // Limpieza: quitamos todo lo que no sea dígito o coma
            texto?.replace(Regex("[^0-9,]"), "")?.replace(",", ".")?.toDoubleOrNull()
        } catch (_: Exception) { null }
    }

    private fun intentarBanplus(): Double? {
        return try {
            val doc = conectarConHeaders("https://www.banplus.com/")
            // Filtramos por USD para no confundir con Euros
            val texto = doc.getElementsContainingText("USD").text()
            val match = Regex("""(?<!\d)\d{2,4},\d{2}(?!\s?%)""").find(texto)
            match?.value?.replace(",", ".")?.toDoubleOrNull()
        } catch (_: Exception) { null }
    }

    private fun intentarMercantil(): Double? {
        return try {
            val doc = conectarConHeaders("https://www.mercantilbanco.com/informacion/tasas,-tarifas-y-comisiones/tasa-mesa-de-cambio")
            // Buscamos el valor numérico que esté cerca de la frase clave
            val bloqueReferencia = doc.select("div:contains(Tipo de Cambio de Referencia BCV)").lastOrNull()

            // 2. Buscamos el número que está después de "Bs./USD" en ese bloque específico
            val textoBloque = bloqueReferencia?.text() ?: ""
            val match = Regex("""Bs\./USD\s+([\d,.]+)""").find(textoBloque)

            // 3. Si no lo encuentra así, buscamos cualquier número largo (más de 4 decimales como en tu imagen)
            val resultado = match?.groupValues?.get(1) ?: Regex("""\d{2,3},\d{5,10}""").find(textoBloque)?.value

            resultado?.replace(",", ".")?.toDoubleOrNull()
        } catch (e: Exception) {
            android.util.Log.e("TASA_ERR", "Mercantil falló: ${e.message}")
            null
        }
    }

    private fun intentarMonitorDolar(): Double? {
        return try {
            val doc = org.jsoup.Jsoup.connect("https://monitordolarve.com/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()
            // Buscamos específicamente en los elementos que tienen el texto BCV
            val cuadroBCV = doc.select("div.card:contains(Dólar BCV), div:contains(Dólar BCV)").lastOrNull()
            val texto = cuadroBCV?.text() ?: ""
            val elementos = doc.getElementsContainingText("Dólar BCV")
            val textoInteresante = elementos.map { it.text() }
                .find { it.contains(Regex("""\d{2,3},\d{2}""")) && !it.contains("Promedio", true) }

            val match = Regex("""\d{2,3},\d{2}""").find(textoInteresante ?: "")
            val finalTasa = match?.value?.replace(",", ".")?.toDoubleOrNull()

            // Si Monitor reporta algo demasiado alejado de nuestra tasa actual (ej. un 30% de diferencia)
            // es porque estamos leyendo el promedio paralelo y no el BCV que ellos reportan.
            val referencia = tasaBCV.doubleValue
            if (finalTasa != null && (finalTasa > referencia * 1.3)) {
                android.util.Log.w("TASA_CHECK", "Monitor reportó paralelo ($finalTasa), ignorando para priorizar oficial.")
                null
            } else {
                finalTasa
            }
        } catch (e: Exception) {
            android.util.Log.e("TASA_ERR", "Monitor fallo: ${e.message}")
            null
        }
    }

    private fun conectarConHeaders(url: String): org.jsoup.nodes.Document {
        return org.jsoup.Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
            .header("Accept-Language", "es-ES,es;q=0.9")
            .header("Cache-Control", "no-cache")
            .header("Pragma", "no-cache")
            .header("Sec-Ch-Ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"")
            .header("Sec-Ch-Ua-Mobile", "?0")
            .header("Sec-Ch-Ua-Platform", "\"Windows\"")
            .timeout(60000)
            .ignoreHttpErrors(true)
            .get()
    }
}
