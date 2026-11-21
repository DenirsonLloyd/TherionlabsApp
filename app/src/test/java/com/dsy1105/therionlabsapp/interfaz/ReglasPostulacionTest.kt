package com.dsy1105.therionlabsapp.interfaz

import org.junit.Assert.*
import org.junit.Test

class ReglasPostulacionTest {
    @Test fun estadosValidos() {
        assertTrue(ReglasPostulacion.estadoValido("Recibida"))
        assertTrue(ReglasPostulacion.estadoValido("En revisión"))
        assertTrue(ReglasPostulacion.estadoValido("Aceptada"))
        assertTrue(ReglasPostulacion.estadoValido("Rechazada"))
    }
    @Test fun rechazaEstadoDesconocido() {
        assertFalse(ReglasPostulacion.estadoValido("Cualquier estado"))
        assertFalse(ReglasPostulacion.estadoValido(""))
    }
    @Test fun soloMuestraPropiasPostulaciones() {
        assertTrue(ReglasPostulacion.perteneceAlAlumno(10,10))
        assertFalse(ReglasPostulacion.perteneceAlAlumno(10,11))
        assertFalse(ReglasPostulacion.perteneceAlAlumno(0,0))
    }
    @Test fun sincronizacion() {
        assertTrue(ReglasPostulacion.debeSincronizar(0))
        assertFalse(ReglasPostulacion.debeSincronizar(20))
    }
}
