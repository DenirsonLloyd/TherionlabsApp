package com.dsy1105.therionlabsapp.datos

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AccesoAdminTest {
    private val contexto = mockk<Context>()
    private val preferencias = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>()

    @Before fun preparar() {
        every { contexto.getSharedPreferences("configuracion_admin", Context.MODE_PRIVATE) } returns preferencias
        every { preferencias.getString("hash", null) } returns null
        every { preferencias.getString("salt", null) } returns null
        every { preferencias.getBoolean("avisos", true) } returns true
        every { preferencias.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        mockkObject(SeguridadClave)
    }

    @After fun terminar() {
        unmockkObject(SeguridadClave)
    }

    @Test fun rechazaNombreDeAdministradorIncorrecto() = runTest {
        assertFalse(AccesoAdmin.ingresar(contexto, "alumno", "admin123"))
    }

    @Test fun permiteAccesoInicialConClaveCorrecta() = runTest {
        assertTrue(AccesoAdmin.ingresar(contexto, " admin ", "admin123"))
    }

    @Test fun rechazaClaveInicialIncorrecta() = runTest {
        assertFalse(AccesoAdmin.ingresar(contexto, "admin", "incorrecta"))
    }

    @Test fun compruebaClaveGuardadaCuandoHayHashYSalt() = runTest {
        every { preferencias.getString("hash", null) } returns "hashguardado"
        every { preferencias.getString("salt", null) } returns "saltguardado"
        every { SeguridadClave.esCorrecta("nueva123", "hashguardado", "saltguardado") } returns true
        assertTrue(AccesoAdmin.ingresar(contexto, "admin", "nueva123"))
    }

    @Test fun rechazaClaveGuardadaEquivocada() = runTest {
        every { preferencias.getString("hash", null) } returns "hashguardado"
        every { preferencias.getString("salt", null) } returns "saltguardado"
        every { SeguridadClave.esCorrecta("equivocada", "hashguardado", "saltguardado") } returns false
        assertFalse(AccesoAdmin.ingresar(contexto, "admin", "equivocada"))
    }

    @Test fun noCambiaClaveSiLaAnteriorEsIncorrecta() = runTest {
        assertFalse(AccesoAdmin.cambiarClave(contexto, "incorrecta", "nueva1234"))
        verify(exactly = 0) { preferencias.edit() }
    }

    @Test fun cambiaClaveDeAdministradorYGuardaHashYSalt() = runTest {
        every { SeguridadClave.crear("nueva1234") } returns Pair("nuevohash", "nuevasalt")
        assertTrue(AccesoAdmin.cambiarClave(contexto, "admin123", "nueva1234"))
        verify(exactly = 1) { editor.putString("hash", "nuevohash") }
        verify(exactly = 1) { editor.putString("salt", "nuevasalt") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test fun avisosEstanActivosPorDefecto() {
        assertTrue(AccesoAdmin.avisosActivos(contexto))
    }

    @Test fun detectaAvisosDesactivados() {
        every { preferencias.getBoolean("avisos", true) } returns false
        assertFalse(AccesoAdmin.avisosActivos(contexto))
    }

    @Test fun guardarAvisosPersisteLaOpcion() {
        AccesoAdmin.guardarAvisos(contexto, false)
        verify(exactly = 1) { editor.putBoolean("avisos", false) }
        verify(exactly = 1) { editor.apply() }
    }
}
