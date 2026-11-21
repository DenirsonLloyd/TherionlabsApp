package com.dsy1105.therionlabsapp.interfaz

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {

    @Test
    fun validaCorreosConDominio() {
        assertTrue(Validaciones.correoValido("alumno@universidad.cl"))
        assertTrue(Validaciones.correoValido("alumno.prueba+1@correo.com"))
        assertFalse(Validaciones.correoValido("alumno@"))
        assertFalse(Validaciones.correoValido("alumno@universidad"))
        assertFalse(Validaciones.correoValido("alumno@@universidad.cl"))
        assertFalse(Validaciones.correoValido("alumno@-universidad.cl"))
    }

    @Test
    fun contrasenaDebeTenerOchoCaracteresLetraYNumero() {
        assertTrue(Validaciones.claveValida("MiClave123"))
        assertFalse(Validaciones.claveValida("12345678"))
        assertFalse(Validaciones.claveValida("abcdefgh"))
        assertFalse(Validaciones.claveValida("Clave12"))
    }

    @Test
    fun compruebaRutYFechas() {
        assertTrue(Validaciones.rutValido("12.345.678-5"))
        assertFalse(Validaciones.rutValido("12.345.678-9"))
        assertTrue(Validaciones.fechaValida("28/10/2000"))
        assertFalse(Validaciones.fechaValida("31/02/2000"))
    }

    @Test
    fun compruebaTelefonosChilenos() {
        assertTrue(Validaciones.telefonoValido("912345678"))
        assertTrue(Validaciones.telefonoValido("+56 9 1234 5678"))
        assertFalse(Validaciones.telefonoValido("1234"))
    }
}
