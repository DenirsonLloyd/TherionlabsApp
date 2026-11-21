package com.dsy1105.therionlabsapp.interfaz

import org.junit.Assert.*
import org.junit.Test

class ValidacionesAdicionalesTest {
    @Test fun nombres() {
        assertTrue(Validaciones.nombreValido("Ana Pérez"))
        assertFalse(Validaciones.nombreValido("Al"))
        assertFalse(Validaciones.nombreValido("Juan 123"))
        assertFalse(Validaciones.nombreValido("  "))
    }
    @Test fun correos() {
        assertTrue(Validaciones.correoValido("usuario@duoc.cl"))
        assertFalse(Validaciones.correoValido(".usuario@duoc.cl"))
        assertFalse(Validaciones.correoValido("usuario.@duoc.cl"))
        assertFalse(Validaciones.correoValido("usuario@duoc..cl"))
        assertFalse(Validaciones.correoValido("usuario@-duoc.cl"))
    }
    @Test fun contrasenas() {
        assertTrue(Validaciones.claveValida("Alumno2026"))
        assertFalse(Validaciones.claveValida("soloLetras"))
        assertFalse(Validaciones.claveValida("123456789"))
    }
    @Test fun rutChileno() {
        assertTrue(Validaciones.rutValido("12.345.678-5"))
        assertFalse(Validaciones.rutValido("12.345.678-0"))
        assertFalse(Validaciones.rutValido("123"))
    }
    @Test fun fechas() {
        assertTrue(Validaciones.fechaValida("01/01/2001"))
        assertFalse(Validaciones.fechaValida("31/02/2001"))
        assertFalse(Validaciones.fechaValida("31/12/2999"))
        assertFalse(Validaciones.fechaValida("01-01-2001"))
    }
    @Test fun telefonosYDirecciones() {
        assertTrue(Validaciones.telefonoValido("912345678"))
        assertFalse(Validaciones.telefonoValido("9123"))
        assertTrue(Validaciones.direccionValida("Calle Los Pinos 123"))
        assertFalse(Validaciones.direccionValida("ABC"))
    }
}
