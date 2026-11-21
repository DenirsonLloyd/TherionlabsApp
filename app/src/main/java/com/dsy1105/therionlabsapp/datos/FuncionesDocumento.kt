package com.dsy1105.therionlabsapp.datos

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri

// Es una malla demostrativa, no un plan oficial de ninguna universidad.
object FuncionesDocumento {
    internal fun asignaturas(carrera: String): List<String> = when (carrera) {
        "Ingeniería en Informática" -> listOf("Programación I", "Matemática", "Bases de Datos", "Redes", "Desarrollo Móvil", "Proyecto de Título")
        "Ingeniería Comercial" -> listOf("Introducción a la Economía", "Matemática", "Contabilidad", "Marketing", "Finanzas", "Proyecto de Título")
        "Psicología" -> listOf("Psicología General", "Biología", "Psicología del Desarrollo", "Metodología", "Psicología Social", "Práctica")
        "Derecho" -> listOf("Introducción al Derecho", "Derecho Romano", "Derecho Civil", "Derecho Penal", "Derecho Procesal", "Clínica Jurídica")
        "Medicina" -> listOf("Biología Celular", "Química", "Anatomía", "Fisiología", "Semiología", "Práctica Clínica")
        else -> listOf("Formación inicial", "Fundamentos", "Especialidad", "Taller práctico", "Proyecto", "Práctica")
    }

    fun guardarMalla(context: Context, uri: Uri, carrera: String, facultad: String) {
        val pdf = PdfDocument()
        try {
            val pagina = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val canvas = pagina.canvas
            val pintura = Paint(Paint.ANTI_ALIAS_FLAG)
            pintura.color = Color.rgb(7, 52, 95)
            pintura.textSize = 24f
            pintura.isFakeBoldText = true
            canvas.drawText("Malla curricular de referencia", 36f, 65f, pintura)
            pintura.textSize = 16f
            canvas.drawText(carrera, 36f, 106f, pintura)
            pintura.isFakeBoldText = false
            pintura.textSize = 12f
            canvas.drawText(facultad, 36f, 127f, pintura)
            pintura.color = Color.DKGRAY
            canvas.drawText("Ejemplo académico para demostración. No es una malla oficial.", 36f, 157f, pintura)
            canvas.drawText("La universidad deberá publicar las asignaturas oficiales.", 36f, 174f, pintura)
            pintura.textSize = 14f
            asignaturas(carrera).forEachIndexed { indice, nombre ->
                canvas.drawText("Asignatura referencial ${indice + 1}: $nombre", 44f, 218f + indice * 38f, pintura)
            }
            pintura.textSize = 11f
            canvas.drawText("Generado en TherionLabs · Documento de ejemplo", 36f, 775f, pintura)
            pdf.finishPage(pagina)
            context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }
                ?: error("No se pudo crear el documento")
        } finally {
            pdf.close()
        }
    }

    private fun csv(valor: String): String {
        val escapado = valor.replace("\"", "\"\"")
        return "\"" + escapado + "\""
    }

    fun exportarPostulaciones(context: Context, uri: Uri, alumnos: List<ProspectoEntidad>) {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { salida ->
            salida.write("ID;Nombre;RUT;Correo;Telefono;Carrera;Estado\n")
            alumnos.forEach { alumno ->
                salida.write(listOf(alumno.id.toString(), alumno.nombre, alumno.rut, alumno.correo,
                    alumno.telefono, alumno.cursoInteres,
                    if (alumno.esSincronizado) "Sincronizado" else "Pendiente"
                ).joinToString(";") { csv(it) } + "\n")
            }
        } ?: error("No se pudo exportar el archivo")
    }
}
