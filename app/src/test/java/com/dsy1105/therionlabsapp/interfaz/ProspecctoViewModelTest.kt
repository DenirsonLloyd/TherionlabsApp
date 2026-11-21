package com.dsy1105.therionlabsapp.interfaz

import com.dsy1105.therionlabsapp.datos.ProspectoEntidad
import com.dsy1105.therionlabsapp.datos.ProspectoRepositorio
import com.dsy1105.therionlabsapp.datos.ResultadoGuardado
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
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
class ProspecctoViewModelTest {
    private val repositorio = mockk<ProspectoRepositorio>()

    @Before fun preparar() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { repositorio.obtenerTodos() } returns flowOf(emptyList())
        every { repositorio.pendientes } returns flowOf(emptyList())
        every { repositorio.sincronizados } returns flowOf(emptyList())
    }

    @After fun terminar() {
        Dispatchers.resetMain()
    }

    private fun completarFormulario(vm: ProspecctoViewModel) {
        vm.nombre = "Ana Pérez"
        vm.rut = "12.345.678-5"
        vm.fechaNacimiento = "01/01/2000"
        vm.correo = "ana@correo.cl"
        vm.telefono = "912345678"
        vm.direccion = "Calle Los Pinos 123"
        vm.cursoInteres = "Ingeniería en Informática"
    }

    @Test fun validaDatosVaciosYExplicaLosErrores() {
        val vm = ProspecctoViewModel(repositorio)
        assertFalse(vm.validarDatos())
        assertTrue(vm.errorNombre.isNotEmpty())
        assertTrue(vm.errorRut.isNotEmpty())
        assertTrue(vm.errorFecha.isNotEmpty())
        assertTrue(vm.errorEmail.isNotEmpty())
        assertTrue(vm.errorTelefono.isNotEmpty())
        assertTrue(vm.errorDireccion.isNotEmpty())
    }

    @Test fun aceptaFormularioCompleto() {
        val vm = ProspecctoViewModel(repositorio)
        completarFormulario(vm)
        assertTrue(vm.validarDatos())
        assertFalse(vm.errorCorreo)
    }

    @Test fun impideGuardarSinCuenta() {
        val vm = ProspecctoViewModel(repositorio)
        completarFormulario(vm)
        var error = false
        vm.guardarProspecto(validarDatosCompletos = true, usuarioId = 0, onError = { error = true }, onExito = {})
        assertTrue(error)
        coVerify(exactly = 0) { repositorio.guardar(any(), any()) }
    }

    @Test fun impideGuardarDatosIncorrectos() {
        val vm = ProspecctoViewModel(repositorio)
        var error = false
        vm.guardarProspecto(validarDatosCompletos = true, usuarioId = 7, onError = { error = true }, onExito = {})
        assertTrue(error)
        assertTrue(vm.errorCorreo)
        coVerify(exactly = 0) { repositorio.guardar(any(), any()) }
    }

    @Test fun registraPostulacionYLimpiaFormulario() = runTest {
        val enviado = slot<ProspectoEntidad>()
        coEvery { repositorio.guardar(capture(enviado), eq(6L)) } returns ResultadoGuardado(true, true)
        val vm = ProspecctoViewModel(repositorio)
        completarFormulario(vm)
        vm.fotoRuta = "ruta-de-foto-de-prueba"
        var registrado = false
        vm.guardarProspecto(validarDatosCompletos = true, usuarioId = 7, usuarioServidorId = 6L,
            onExito = { registrado = true })
        advanceUntilIdle()
        assertTrue(registrado)
        assertEquals(7, enviado.captured.usuarioId)
        assertEquals("Ingeniería en Informática", enviado.captured.cursoInteres)
        assertEquals("ruta-de-foto-de-prueba", enviado.captured.fotoRuta)
        assertEquals("Postulación guardada en el servidor", vm.mensajeServidor)
        assertEquals("", vm.nombre)
        assertEquals("", vm.fotoRuta)
    }

    @Test fun avisaSiPostulacionQuedoLocal() = runTest {
        coEvery { repositorio.guardar(any(), any()) } returns ResultadoGuardado(false, false)
        val vm = ProspecctoViewModel(repositorio)
        completarFormulario(vm)
        vm.guardarProspecto(validarDatosCompletos = true, usuarioId = 7, usuarioServidorId = 0L, onExito = {})
        advanceUntilIdle()
        assertTrue(vm.mensajeServidor.contains("localmente"))
    }

    @Test fun avisaSiFaltaEnviarLaFotografia() = runTest {
        coEvery { repositorio.guardar(any(), any()) } returns ResultadoGuardado(true, false)
        val vm = ProspecctoViewModel(repositorio)
        completarFormulario(vm)
        vm.fotoRuta = "foto-prueba"
        vm.guardarProspecto(validarDatosCompletos = true, usuarioId = 7, usuarioServidorId = 6L, onExito = {})
        advanceUntilIdle()
        assertTrue(vm.mensajeServidor.contains("fotografía pendiente"))
    }

    @Test fun cambiaEstadoSoloSiEsValido() = runTest {
        coEvery { repositorio.cambiarEstadoPostulacion(4, "Aceptada") } returns Unit
        val vm = ProspecctoViewModel(repositorio)
        vm.cambiarEstadoPostulacion(4, "Inexistente")
        advanceUntilIdle()
        coVerify(exactly = 0) { repositorio.cambiarEstadoPostulacion(any(), any()) }
        vm.cambiarEstadoPostulacion(4, "Aceptada")
        advanceUntilIdle()
        coVerify(exactly = 1) { repositorio.cambiarEstadoPostulacion(4, "Aceptada") }
    }

    @Test fun sincronizacionMuestraEstadoDelServidor() = runTest {
        coEvery { repositorio.sincronizarPendientes(7, 6L) } returns Unit
        coEvery { repositorio.actualizarDesdeServidor(7, 6L) } returns true
        val vm = ProspecctoViewModel(repositorio)
        vm.sincronizarDatos(7, 6L)
        advanceUntilIdle()
        assertEquals("Conectado al servidor", vm.mensajeServidor)
    }

    @Test fun sincronizacionInformaSiServidorNoEstaDisponible() = runTest {
        coEvery { repositorio.sincronizarPendientes(7, 0L) } returns Unit
        coEvery { repositorio.actualizarDesdeServidor(7, 0L) } returns false
        val vm = ProspecctoViewModel(repositorio)
        vm.sincronizarDatos(7, 0L)
        advanceUntilIdle()
        assertTrue(vm.mensajeServidor.contains("Sin servidor"))
    }

    @Test fun eliminarRegistroLocal() = runTest {
        val p = ProspectoEntidad(id = 9, nombre = "Ana Pérez", correo = "ana@correo.cl", telefono = "912345678")
        coEvery { repositorio.eliminar(p) } returns Unit
        val vm = ProspecctoViewModel(repositorio)
        vm.eliminarProspecto(p)
        advanceUntilIdle()
        coVerify(exactly = 1) { repositorio.eliminar(p) }
    }

    @Test fun cambiaEstadoDeSincronizacion() = runTest {
        coEvery { repositorio.actualizarEstado(9, true) } returns Unit
        val vm = ProspecctoViewModel(repositorio)
        vm.actualizarEstadoSincronizacion(9, true)
        advanceUntilIdle()
        coVerify(exactly = 1) { repositorio.actualizarEstado(9, true) }
    }

    @Test fun formularioAntiguoRechazaCorreoInvalido() {
        val vm = ProspecctoViewModel(repositorio)
        vm.correo = "incorrecto"
        var error = false
        vm.guardarProspecto(onError = { error = true }, onExito = {})
        assertTrue(error)
        assertTrue(vm.errorCorreo)
    }

    @Test fun reiniciarFormularioLimpiaCampos() {
        val vm = ProspecctoViewModel(repositorio)
        vm.nombre = "Prueba"
        vm.correo = "incorrecto"
        vm.validarDatos()
        vm.fotoRuta = "foto"
        vm.reiniciarFormulario()
        assertEquals("", vm.nombre)
        assertEquals("", vm.fotoRuta)
        assertEquals("Presencial", vm.modalidad)
        assertEquals("", vm.errorEmail)
    }

    @Test fun consultaPostulacionesDelEstudiante() = runTest {
        val alumno = ProspectoEntidad(id = 3, nombre = "Ana", correo = "ana@correo.cl", telefono = "912345678", usuarioId = 7)
        every { repositorio.obtenerPorUsuario(7) } returns flowOf(listOf(alumno))
        val vm = ProspecctoViewModel(repositorio)
        assertEquals(alumno, vm.obtenerPorUsuario(7).first().first())
    }

    @Test fun modificaYEliminaEnElServidor() = runTest {
        coEvery { repositorio.cambiarEstadoDeServidor(12L, "Aceptada") } returns Unit
        coEvery { repositorio.eliminarDeServidor(12L) } returns Unit
        val vm = ProspecctoViewModel(repositorio)
        vm.cambiarEstadoServidor(12L, "Aceptada")
        vm.eliminarServidor(12L)
        coVerify(exactly = 1) { repositorio.cambiarEstadoDeServidor(12L, "Aceptada") }
        coVerify(exactly = 1) { repositorio.eliminarDeServidor(12L) }
    }

}
