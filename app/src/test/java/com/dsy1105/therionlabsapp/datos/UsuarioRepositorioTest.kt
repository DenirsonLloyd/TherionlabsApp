package com.dsy1105.therionlabsapp.datos

import android.util.Log
import com.dsy1105.therionlabsapp.api.UsuarioApi
import com.dsy1105.therionlabsapp.api.UsuariosApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UsuarioRepositorioTest {
    private val dao = mockk<UsuarioDao>()
    private val servicio = mockk<UsuariosApi>()
    private val repo = UsuarioRepositorio(dao, servicio)
    private val usuario = UsuarioEntidad(
        id = 7, nombre = "Ana Pérez", correo = "ana@correo.cl",
        claveHash = "hash", salt = "salt"
    )

    @Before fun preparar() {
        mockkObject(SeguridadClave)
        every { SeguridadClave.crear(any()) } returns Pair("hash", "salt")
        every { SeguridadClave.esCorrecta(any(), any(), any()) } answers {
            firstArg<String>() == "Clave1234"
        }
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After fun terminar() {
        unmockkObject(SeguridadClave)
        unmockkStatic(Log::class)
    }

    @Test fun buscaPorCorreo() = runTest {
        coEvery { dao.buscarPorCorreo("ana@correo.cl") } returns usuario
        assertEquals(usuario, repo.buscarPorCorreo("ana@correo.cl"))
        coVerify(exactly = 1) { dao.buscarPorCorreo("ana@correo.cl") }
    }

    @Test fun devuelveNullSiCorreoNoExiste() = runTest {
        coEvery { dao.buscarPorCorreo("noexiste@correo.cl") } returns null
        assertNull(repo.buscarPorCorreo("noexiste@correo.cl"))
    }

    @Test fun loginRechazaCorreoNoRegistrado() = runTest {
        coEvery { dao.buscarPorCorreo("nadie@correo.cl") } returns null
        assertNull(repo.iniciarSesion("nadie@correo.cl", "Clave1234"))
    }

    @Test fun registraEnRoomYEnSpringBoot() = runTest {
        coEvery { dao.insertarUsuario(any()) } returns 7L
        coEvery { servicio.crear(any()) } returns UsuarioApi(12L, "Ana Pérez", "ana@correo.cl")
        coEvery { dao.actualizarServidorId(7, 12L) } returns Unit
        val nuevo = repo.registrar("Ana Pérez", "ana@correo.cl", "Clave1234")
        assertEquals(7, nuevo.id)
        assertEquals(12L, nuevo.servidorId)
        coVerify(exactly = 1) { dao.insertarUsuario(any()) }
        coVerify(exactly = 1) { dao.actualizarServidorId(7, 12L) }
    }

    @Test fun recuperaUsuarioSiServidorYaTieneEseCorreo() = runTest {
        coEvery { dao.insertarUsuario(any()) } returns 7L
        coEvery { servicio.crear(any()) } throws IllegalStateException("Correo repetido")
        coEvery { servicio.porCorreo("ana@correo.cl") } returns UsuarioApi(12L, "Ana Pérez", "ana@correo.cl")
        coEvery { dao.actualizarServidorId(7, 12L) } returns Unit
        assertEquals(12L, repo.registrar("Ana Pérez", "ana@correo.cl", "Clave1234").servidorId)
    }

    @Test fun registroLocalSiServidorEstaApagado() = runTest {
        coEvery { dao.insertarUsuario(any()) } returns 7L
        coEvery { servicio.crear(any()) } throws IllegalStateException("Sin conexión")
        coEvery { servicio.porCorreo(any()) } throws IllegalStateException("Sin conexión")
        val nuevo = repo.registrar("Ana Pérez", "ana@correo.cl", "Clave1234")
        assertEquals(0L, nuevo.servidorId)
        coVerify(exactly = 0) { dao.actualizarServidorId(any(), any()) }
    }

    @Test fun loginRechazaContrasenaIncorrecta() = runTest {
        coEvery { dao.buscarPorCorreo("ana@correo.cl") } returns usuario
        assertNull(repo.iniciarSesion("ana@correo.cl", "Incorrecta12"))
    }

    @Test fun loginReconectaUsuarioLocal() = runTest {
        coEvery { dao.buscarPorCorreo("ana@correo.cl") } returns usuario
        coEvery { servicio.crear(any()) } returns UsuarioApi(12L, "Ana Pérez", "ana@correo.cl")
        coEvery { dao.actualizarServidorId(7, 12L) } returns Unit
        val encontrado = repo.iniciarSesion("ana@correo.cl", "Clave1234")
        assertEquals(12L, encontrado?.servidorId)
    }

    @Test fun actualizaDatosLocalesYRemotos() = runTest {
        val conectado = usuario.copy(servidorId = 12L)
        coEvery { dao.actualizarPerfil(7, "Ana María", "nuevo@correo.cl") } returns Unit
        coEvery { servicio.actualizar(12L, any()) } returns UsuarioApi(12L, "Ana María", "nuevo@correo.cl")
        coEvery { dao.actualizarServidorId(7, 12L) } returns Unit
        val actualizado = repo.actualizarPerfil(conectado, "Ana María", "nuevo@correo.cl")
        assertEquals("nuevo@correo.cl", actualizado.correo)
        coVerify(exactly = 1) { dao.actualizarPerfil(7, "Ana María", "nuevo@correo.cl") }
    }

    @Test fun cambiaClaveSoloSiLaAnteriorEsCorrecta() = runTest {
        assertNull(repo.cambiarClave(usuario, "Incorrecta12", "Nueva5678"))
        coVerify(exactly = 0) { dao.actualizarClave(any(), any(), any()) }
        coEvery { dao.actualizarClave(7, "hash", "salt") } returns Unit
        val nuevo = repo.cambiarClave(usuario, "Clave1234", "Nueva5678")
        assertNotNull(nuevo)
        coVerify(exactly = 1) { dao.actualizarClave(7, "hash", "salt") }
    }
}
