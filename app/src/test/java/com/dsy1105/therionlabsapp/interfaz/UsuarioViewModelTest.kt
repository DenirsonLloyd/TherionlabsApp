package com.dsy1105.therionlabsapp.interfaz

import com.dsy1105.therionlabsapp.datos.UsuarioEntidad
import com.dsy1105.therionlabsapp.datos.UsuarioRepositorio
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModelTest {
    private val repositorio = mockk<UsuarioRepositorio>()
    private val usuario = UsuarioEntidad(
        id = 8, nombre = "Ana Pérez", correo = "ana@correo.cl",
        claveHash = "hash", salt = "salt", servidorId = 12
    )

    @Before fun preparar() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After fun terminar() {
        Dispatchers.resetMain()
    }

    private fun llenarRegistro(vm: UsuarioViewModel) {
        vm.nombre = "Ana Pérez"
        vm.correo = " ANA@CORREO.CL "
        vm.clave = "Clave1234"
        vm.confirmarClave = "Clave1234"
    }

    @Test fun registroRechazaCamposIncorrectos() {
        val vm = UsuarioViewModel(repositorio)
        var registrado = false
        vm.registrarUsuario { registrado = true }
        assertFalse(registrado)
        assertTrue(vm.errorNombre.isNotEmpty())
        assertTrue(vm.errorCorreo.isNotEmpty())
        assertTrue(vm.errorClave.isNotEmpty())
        assertTrue(vm.errorConfirmar.isNotEmpty())
        coVerify(exactly = 0) { repositorio.registrar(any(), any(), any()) }
    }

    @Test fun registroRechazaContrasenasDistintas() {
        val vm = UsuarioViewModel(repositorio)
        llenarRegistro(vm)
        vm.confirmarClave = "Otra12345"
        vm.registrarUsuario { }
        assertEquals("Las contraseñas deben coincidir.", vm.errorConfirmar)
        coVerify(exactly = 0) { repositorio.registrar(any(), any(), any()) }
    }

    @Test fun registroExitosoNormalizaCorreoYLimpiaCampos() = runTest {
        coEvery { repositorio.buscarPorCorreo("ana@correo.cl") } returns null
        coEvery { repositorio.registrar("Ana Pérez", "ana@correo.cl", "Clave1234") } returns usuario
        val vm = UsuarioViewModel(repositorio)
        llenarRegistro(vm)
        var registroCorrecto = false
        vm.registrarUsuario { registroCorrecto = true }
        advanceUntilIdle()
        assertTrue(registroCorrecto)
        assertEquals(usuario, vm.usuarioActual)
        assertEquals("", vm.clave)
        assertEquals("", vm.correo)
        assertFalse(vm.cargando)
        coVerify(exactly = 1) { repositorio.registrar("Ana Pérez", "ana@correo.cl", "Clave1234") }
    }

    @Test fun noDejaRegistrarCorreoRepetido() = runTest {
        coEvery { repositorio.buscarPorCorreo("ana@correo.cl") } returns usuario
        val vm = UsuarioViewModel(repositorio)
        llenarRegistro(vm)
        var registroCorrecto = false
        vm.registrarUsuario { registroCorrecto = true }
        advanceUntilIdle()
        assertFalse(registroCorrecto)
        assertEquals("Ya existe una cuenta con este correo.", vm.errorCorreo)
        coVerify(exactly = 0) { repositorio.registrar(any(), any(), any()) }
    }

    @Test fun muestraErrorSiFallaElRegistro() = runTest {
        coEvery { repositorio.buscarPorCorreo("ana@correo.cl") } throws IllegalStateException("Prueba de error")
        val vm = UsuarioViewModel(repositorio)
        llenarRegistro(vm)
        vm.registrarUsuario { }
        advanceUntilIdle()
        assertTrue(vm.errorGeneral.isNotEmpty())
        assertFalse(vm.cargando)
    }

    @Test fun loginValidaLosCampos() {
        val vm = UsuarioViewModel(repositorio)
        vm.iniciarSesion { }
        assertTrue(vm.errorCorreoLogin.isNotEmpty())
        assertTrue(vm.errorClaveLogin.isNotEmpty())
        coVerify(exactly = 0) { repositorio.iniciarSesion(any(), any()) }
    }

    @Test fun loginRechazaCredencialesIncorrectas() = runTest {
        coEvery { repositorio.iniciarSesion("ana@correo.cl", "Incorrecta123") } returns null
        val vm = UsuarioViewModel(repositorio)
        vm.correoLogin = " ANA@CORREO.CL "
        vm.claveLogin = "Incorrecta123"
        vm.iniciarSesion { }
        advanceUntilIdle()
        assertEquals("Correo o contraseña incorrectos.", vm.errorLogin)
        assertNull(vm.usuarioActual)
    }

    @Test fun loginExitosoYLogout() = runTest {
        coEvery { repositorio.iniciarSesion("ana@correo.cl", "Clave1234") } returns usuario
        val vm = UsuarioViewModel(repositorio)
        vm.correoLogin = "ana@correo.cl"
        vm.claveLogin = "Clave1234"
        var ingreso = false
        vm.iniciarSesion { ingreso = true }
        advanceUntilIdle()
        assertTrue(ingreso)
        assertEquals(usuario, vm.usuarioActual)
        assertEquals("", vm.claveLogin)
        vm.cerrarSesion()
        assertNull(vm.usuarioActual)
    }

    @Test fun loginMuestraErrorSiFallaRepositorio() = runTest {
        coEvery { repositorio.iniciarSesion(any(), any()) } throws IllegalStateException("Prueba de error")
        val vm = UsuarioViewModel(repositorio)
        vm.correoLogin = "ana@correo.cl"
        vm.claveLogin = "Clave1234"
        vm.iniciarSesion { }
        advanceUntilIdle()
        assertEquals("No se pudo ingresar. Inténtalo nuevamente.", vm.errorLogin)
        assertFalse(vm.cargando)
    }

    @Test fun perfilYClaveNoSeCambianSinSesion() {
        val vm = UsuarioViewModel(repositorio)
        vm.actualizarPerfil("Nombre Nuevo", "nuevo@correo.cl") { _, _ -> }
        vm.cambiarClave("Clave1234", "Clave5678", "Clave5678")
        assertEquals("", vm.mensajePerfil)
        assertEquals("", vm.mensajeClave)
        coVerify(exactly = 0) { repositorio.actualizarPerfil(any(), any(), any()) }
        coVerify(exactly = 0) { repositorio.cambiarClave(any(), any(), any()) }
    }

    @Test fun actualizarPerfilRechazaCorreoDuplicado() = runTest {
        coEvery { repositorio.iniciarSesion(any(), any()) } returns usuario
        coEvery { repositorio.buscarPorCorreo("otro@correo.cl") } returns usuario.copy(id = 9, correo = "otro@correo.cl")
        val vm = UsuarioViewModel(repositorio)
        vm.correoLogin = "ana@correo.cl"
        vm.claveLogin = "Clave1234"
        vm.iniciarSesion { }
        advanceUntilIdle()
        vm.actualizarPerfil("Ana Pérez", "otro@correo.cl") { _, _ -> }
        advanceUntilIdle()
        assertEquals("Este correo ya está registrado.", vm.mensajePerfil)
        coVerify(exactly = 0) { repositorio.actualizarPerfil(any(), any(), any()) }
    }

    @Test fun actualizarPerfilCorrectamente() = runTest {
        coEvery { repositorio.iniciarSesion(any(), any()) } returns usuario
        coEvery { repositorio.buscarPorCorreo("nuevo@correo.cl") } returns null
        coEvery { repositorio.actualizarPerfil(usuario, "Ana María", "nuevo@correo.cl") } returns
            usuario.copy(nombre = "Ana María", correo = "nuevo@correo.cl")
        val vm = UsuarioViewModel(repositorio)
        vm.correoLogin = "ana@correo.cl"
        vm.claveLogin = "Clave1234"
        vm.iniciarSesion { }
        advanceUntilIdle()
        var guardado = false
        vm.actualizarPerfil("Ana María", " NUEVO@CORREO.CL ") { _, _ -> guardado = true }
        advanceUntilIdle()
        assertTrue(guardado)
        assertEquals("Perfil actualizado correctamente.", vm.mensajePerfil)
        assertEquals("nuevo@correo.cl", vm.usuarioActual?.correo)
    }

    @Test fun cambiarClaveValidaYGuarda() = runTest {
        coEvery { repositorio.iniciarSesion(any(), any()) } returns usuario
        coEvery { repositorio.cambiarClave(usuario, "Clave1234", "Nueva5678") } returns
            usuario.copy(claveHash = "nuevoHash")
        val vm = UsuarioViewModel(repositorio)
        vm.correoLogin = "ana@correo.cl"
        vm.claveLogin = "Clave1234"
        vm.iniciarSesion { }
        advanceUntilIdle()
        vm.cambiarClave("Clave1234", "corta", "corta")
        assertTrue(vm.mensajeClave.contains("8 caracteres"))
        vm.cambiarClave("Clave1234", "Clave1234", "Clave1234")
        assertTrue(vm.mensajeClave.contains("diferente"))
        vm.cambiarClave("Clave1234", "Nueva5678", "Otra5678")
        assertTrue(vm.mensajeClave.contains("no coinciden"))
        vm.cambiarClave("Clave1234", "Nueva5678", "Nueva5678")
        advanceUntilIdle()
        assertEquals("Contraseña actualizada correctamente.", vm.mensajeClave)
        coVerify(exactly = 1) { repositorio.cambiarClave(usuario, "Clave1234", "Nueva5678") }
    }
}
