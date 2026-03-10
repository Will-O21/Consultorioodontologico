package com.wdog.consultorioodontologico.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import com.wdog.consultorioodontologico.ui.components.EstadoDiente
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri

class PdfOdontogramaGenerator(private val context: Context) {

    fun generarPdf(
        nombrePaciente: String, nombreDoctor: String, nombreConsultorio: String,
        logoPath: String?, dientesMarcados: Map<Int, Set<Int>>,
        estadosEspeciales: Map<Int, EstadoDiente>, notasDientes: Map<Int, String>,
        dientesTratados: Map<Int, Boolean>, puentes: Map<Int, Int?>,
        esModoPediatrico: Boolean
    ) {
        context.getExternalFilesDir(null)?.listFiles()?.forEach {
            if (it.name.endsWith(".pdf")) it.delete()
        }
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 18f }
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Cabecera y Logo
        if (!logoPath.isNullOrEmpty()) {
            try {
                val inputStream = context.contentResolver.openInputStream(logoPath.toUri())
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, Rect(40, 40, 100, 100), paint)
                }
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        canvas.drawText(nombreConsultorio.uppercase(), 110f, 65f, titlePaint)
        paint.textSize = 10f
        canvas.drawText("Dr. $nombreDoctor", 110f, 85f, paint)
        canvas.drawText("Paciente: $nombrePaciente", 40f, 130f, paint)
        canvas.drawText("Tipo: ${if(esModoPediatrico) "Odontograma Pediátrico" else "Odontograma Adulto"}", 40f, 145f, paint)
        canvas.drawLine(40f, 155f, 555f, 155f, paint)

        // LÓGICA DE FILAS
        val filasDientes = if (esModoPediatrico) {
            listOf(
                listOf(55, 54, 53, 52, 51) to listOf(61, 62, 63, 64, 65),
                listOf(85, 84, 83, 82, 81) to listOf(71, 72, 73, 74, 75)
            )
        } else {
            listOf(
                listOf(18, 17, 16, 15, 14, 13, 12, 11) to listOf(21, 22, 23, 24, 25, 26, 27, 28),
                listOf(48, 47, 46, 45, 44, 43, 42, 41) to listOf(31, 32, 33, 34, 35, 36, 37, 38)
            )
        }

        val sizeD = 22f
        var yPos = 200f

        filasDientes.forEach { (izq, der) ->
            var xPos = 60f
            izq.forEach { id ->
                dibujarDientePdf(canvas, xPos, yPos, id, sizeD,
                    dientesMarcados[id] ?: emptySet(),
                    estadosEspeciales[id] ?: EstadoDiente.NORMAL,
                    dientesTratados[id] ?: false,
                    puentes[id])
                xPos += sizeD + 4f
            }
            xPos += 15f
            der.forEach { id ->
                dibujarDientePdf(canvas, xPos, yPos, id, sizeD,
                    dientesMarcados[id] ?: emptySet(),
                    estadosEspeciales[id] ?: EstadoDiente.NORMAL,
                    dientesTratados[id] ?: false,
                    puentes[id])
                xPos += sizeD + 4f
            }
            yPos += 60f
        }

        // SECCIÓN NOTAS
        yPos += 20f
        canvas.drawText("NOTAS CLÍNICAS:", 40f, yPos, titlePaint.apply { textSize = 12f })
        yPos += 15f
        paint.textSize = 9f
        notasDientes.filter { it.value.isNotBlank() }.forEach { (id, nota) ->
            canvas.drawText("Diente $id: $nota", 50f, yPos, paint)
            yPos += 12f
        }

        pdfDocument.finishPage(page)
        val nombreLimpio = nombrePaciente.trim().replace(" ", "_")
        val file = File(context.getExternalFilesDir(null), "Odontograma_$nombreLimpio.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        // --- NUEVA LÓGICA PARA COMPARTIR ---
        compartirPdf(file)
    }

    private fun compartirPdf(file: File) {
        try {
            // Importante: Debes tener configurado FileProvider en tu AndroidManifest
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Opcional: Para abrir directamente WhatsApp si está instalado
                // setPackage("com.whatsapp")
            }

            val chooser = Intent.createChooser(intent, "Compartir Presupuesto PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            // --- LÓGICA DE AUTO-ELIMINACIÓN ---
            // Esperamos 30 segundos para asegurar que la app receptora leyó el archivo
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    if (file.exists()) {
                        file.delete()
                        android.util.Log.d("PDF_GEN", "Archivo temporal eliminado: ${file.name}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 60000) // 60000 ms = 60 segundos

        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun dibujarDientePdf(
        canvas: Canvas,
        x: Float,
        y: Float,
        id: Int,
        size: Float = 22.0f, // Valor por defecto para evitar el aviso de 'always 22.0'
        caras: Set<Int>,
        estado: EstadoDiente,
        esTratado: Boolean,
        puenteCon: Int?
    ) {
        val paint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.BLACK }
        val fillPaint = Paint().apply { style = Paint.Style.FILL }

        // Uso de .toColorInt() como sugiere el IDE
        val colorMarca = if (esTratado) "#1976D2".toColorInt() else Color.RED
        val centroD = size / 2

        val textPaint = Paint().apply {
            textSize = 7f; textAlign = Paint.Align.CENTER; isFakeBoldText = true
            color = if (estado == EstadoDiente.AUSENTE) Color.LTGRAY else Color.BLACK
        }
        canvas.drawText(id.toString(), x + centroD, y - 4f, textPaint)

        val colorBase = when(estado) {
            EstadoDiente.CORONA -> "#BBDEFB".toColorInt()
            EstadoDiente.PROTESIS_FIJA -> "#E3F2FD".toColorInt()
            else -> Color.WHITE
        }

        if (estado != EstadoDiente.AUSENTE && estado != EstadoDiente.IMPLANTE) {
            val d = size * 0.3f
            fillPaint.color = if (caras.contains(0)) colorMarca else colorBase
            canvas.drawRect(x + d, y + d, x + size - d, y + size - d, fillPaint)
            canvas.drawRect(x + d, y + d, x + size - d, y + size - d, paint)

            for (i in 1..4) {
                val path = Path()
                when(i) {
                    1 -> { path.moveTo(x, y); path.lineTo(x + size, y); path.lineTo(x + size - d, y + d); path.lineTo(x + d, y + d) }
                    2 -> { path.moveTo(x + size, y); path.lineTo(x + size, y + size); path.lineTo(x + size - d, y + size - d); path.lineTo(x + size - d, y + d) }
                    3 -> { path.moveTo(x + size, y + size); path.lineTo(x, y + size); path.lineTo(x + d, y + size - d); path.lineTo(x + size - d, y + size - d) }
                    4 -> { path.moveTo(x, y + size); path.lineTo(x, y); path.lineTo(x + d, y + d); path.lineTo(x + d, y + size - d) }
                }
                path.close()
                fillPaint.color = if (caras.contains(i)) colorMarca else colorBase
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, paint)
            }
        }

        when (estado) {
            EstadoDiente.AUSENTE -> {
                paint.color = colorMarca; paint.strokeWidth = 2f
                canvas.drawLine(x, y, x + size, y + size, paint)
                canvas.drawLine(x + size, y, x, y + size, paint)
            }
            EstadoDiente.IMPLANTE -> {
                paint.color = Color.GRAY
                for (i in 1..4) {
                    val yLine = y + (i * size / 5)
                    canvas.drawLine(x + size * 0.3f, yLine, x + size * 0.7f, yLine + 1f, paint)
                }
                paint.color = Color.BLACK; canvas.drawLine(x + centroD, y, x + centroD, y + size, paint)
            }
            EstadoDiente.ENDODONCIA -> {
                paint.color = Color.BLUE; paint.strokeWidth = 3f
                canvas.drawLine(x + centroD, y, x + centroD, y + size, paint)
            }
            EstadoDiente.FRACTURA -> {
                paint.color = colorMarca; paint.strokeWidth = 2f
                val rayo = Path().apply {
                    moveTo(x + size * 0.2f, y + size * 0.1f); lineTo(x + size * 0.8f, y + size * 0.4f)
                    lineTo(x + size * 0.2f, y + size * 0.6f); lineTo(x + size * 0.8f, y + size * 0.9f)
                }
                canvas.drawPath(rayo, paint)
            }
            EstadoDiente.CUELLO -> {
                paint.color = colorMarca; paint.strokeWidth = 2f
                canvas.drawArc(x + size * 0.1f, y + size * 0.7f, x + size * 0.9f, y + size, 0f, 180f, false, paint)
            }
            EstadoDiente.ORTODONCIA -> {
                fillPaint.color = Color.GRAY; canvas.drawRect(x + size * 0.3f, y + size * 0.3f, x + size * 0.7f, y + size * 0.7f, fillPaint)
                canvas.drawLine(x, y + centroD, x + size, y + centroD, paint)
            }
            else -> {}
        }

        if (puenteCon != null) {
            paint.color = Color.BLUE; paint.alpha = 100; paint.strokeWidth = 4f
            canvas.drawLine(x + centroD, y, x + size + 15f, y, paint)
        }
    }
}