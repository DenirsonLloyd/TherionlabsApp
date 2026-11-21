package com.dsy1105.therionlabsapp.interfaz

// Reglas básicas que se usan en la aplicación y que se pueden probar con JUnit.
object ReglasPostulacion {
    val estados = listOf("Recibida", "En revisión", "Aceptada", "Rechazada")
    fun estadoValido(estado: String): Boolean = estado in estados
    fun perteneceAlAlumno(usuarioIdPostulacion: Int, usuarioIdActual: Int): Boolean {
        return usuarioIdActual > 0 && usuarioIdPostulacion == usuarioIdActual
    }
    fun debeSincronizar(servidorId: Long): Boolean = servidorId <= 0
}
