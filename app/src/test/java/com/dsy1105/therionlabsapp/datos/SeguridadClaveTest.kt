package com.dsy1105.therionlabsapp.datos

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SeguridadClaveTest {
    @Before fun preparar() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any<ByteArray>(), Base64.NO_WRAP) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg())
        }
        every { Base64.decode(any<String>(), Base64.NO_WRAP) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }
    }

    @After fun terminar() {
        unmockkStatic(Base64::class)
    }

    @Test fun crearGeneraHashYSalt() {
        val (hash, salt) = SeguridadClave.crear("Clave1234")
        assertTrue(hash.isNotBlank())
        assertTrue(salt.isNotBlank())
        assertNotEquals("Clave1234", hash)
        assertEquals(16, java.util.Base64.getDecoder().decode(salt).size)
    }

    @Test fun contrasenaCorrectaSeAceptaEIncorrectaSeRechaza() {
        val (hash, salt) = SeguridadClave.crear("Clave1234")
        assertTrue(SeguridadClave.esCorrecta("Clave1234", hash, salt))
        assertFalse(SeguridadClave.esCorrecta("Clave9999", hash, salt))
    }

    @Test fun dosUsuariosNoCompartenLaMismaSal() {
        val primero = SeguridadClave.crear("Clave1234")
        val segundo = SeguridadClave.crear("Clave1234")
        assertNotEquals(primero.second, segundo.second)
        assertNotEquals(primero.first, segundo.first)
    }
}
