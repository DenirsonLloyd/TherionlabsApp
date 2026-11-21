package com.dsy1105.therionlabsapp.datos

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import org.junit.Assert.*
import org.junit.Test

class FuncionesDocumentoTest {
    private val contexto = mockk<Context>()
    private val resolver = mockk<ContentResolver>()
    private val uri = mockk<Uri>()

    @Test fun exportarPostulacionesEscribeEncabezadoYDatos() {
        val salida = ByteArrayOutputStream()
        every { contexto.contentResolver } returns resolver
        every { resolver.openOutputStream(uri) } returns salida
        val alumnos = listOf(
            ProspectoEntidad(id = 4, nombre = "Ana Pérez", correo = "ana@correo.cl",
                telefono = "912345678", rut = "12.345.678-5", cursoInteres = "Derecho", esSincronizado = true),
            ProspectoEntidad(id = 5, nombre = "Juan", correo = "juan@correo.cl",
                telefono = "923456789", cursoInteres = "Ingeniería en Informática", esSincronizado = false)
        )
        FuncionesDocumento.exportarPostulaciones(contexto, uri, alumnos)
        val resultado = salida.toString(Charsets.UTF_8.name())
        assertTrue(resultado.startsWith("ID;Nombre;RUT;Correo;Telefono;Carrera;Estado\n"))
        assertTrue(resultado.contains("\"4\";\"Ana Pérez\""))
        assertTrue(resultado.contains("\"Sincronizado\""))
        assertTrue(resultado.contains("\"Pendiente\""))
    }

    @Test fun exportacionEscapaComillasEnCampos() {
        val salida = ByteArrayOutputStream()
        every { contexto.contentResolver } returns resolver
        every { resolver.openOutputStream(uri) } returns salida
        FuncionesDocumento.exportarPostulaciones(contexto, uri,
            listOf(ProspectoEntidad(nombre = "Ana \"Test\"", correo = "ana@correo.cl", telefono = "912345678")))
        val resultado = salida.toString(Charsets.UTF_8.name())
        assertTrue(resultado.contains("\"Ana \"\"Test\"\"\""))
    }

    @Test fun exportacionSinRegistrosMantieneCabecera() {
        val salida = ByteArrayOutputStream()
        every { contexto.contentResolver } returns resolver
        every { resolver.openOutputStream(uri) } returns salida
        FuncionesDocumento.exportarPostulaciones(contexto, uri, emptyList())
        assertEquals("ID;Nombre;RUT;Correo;Telefono;Carrera;Estado\n", salida.toString(Charsets.UTF_8.name()))
    }

    @Test fun mallasReferencialesSonDistintasPorCarrera() {
        val carreras = listOf(
            "Ingeniería en Informática", "Ingeniería Comercial", "Psicología",
            "Derecho", "Medicina", "Otra carrera"
        )
        val mallas = carreras.map { FuncionesDocumento.asignaturas(it) }
        assertEquals(6, mallas.distinct().size)
        assertTrue(mallas.all { it.size == 6 })
        assertEquals("Programación I", mallas[0].first())
        assertEquals("Introducción al Derecho", mallas[3].first())
        assertEquals("Formación inicial", mallas[5].first())
    }

    @Test fun exportacionInformaErrorSiNoSePuedeAbrirArchivo() {
        every { contexto.contentResolver } returns resolver
        every { resolver.openOutputStream(uri) } returns null
        try {
            FuncionesDocumento.exportarPostulaciones(contexto, uri, emptyList())
            fail("Se esperaba un error al no poder escribir el archivo")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("No se pudo exportar"))
        }
    }
}
