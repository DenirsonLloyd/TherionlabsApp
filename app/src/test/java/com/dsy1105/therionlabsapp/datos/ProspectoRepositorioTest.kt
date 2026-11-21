package com.dsy1105.therionlabsapp.datos

import com.dsy1105.therionlabsapp.api.PostulacionesApi
import com.dsy1105.therionlabsapp.api.PostulacionApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ProspectoRepositorioTest {
    private val dao = mockk<ProspectoDao>()
    private val servicio = mockk<PostulacionesApi>()
    private val repo = run {
        every { dao.obtenerPendientes() } returns flowOf(emptyList())
        every { dao.obtenerSincronizados() } returns flowOf(emptyList())
        ProspectoRepositorio(dao, servicio)
    }
    private val prospecto = ProspectoEntidad(
        id = 4, nombre = "Ana Pérez", correo = "ana@correo.cl",
        telefono = "912345678", usuarioId = 7
    )

    @Test fun consultaRegistros() = runTest {
        every { dao.obtenerTodos() } returns flowOf(listOf(prospecto))
        val resultado = repo.obtenerTodos().first()
        assertEquals(1, resultado.size)
        assertEquals("Ana Pérez", resultado.first().nombre)
    }

    @Test fun consultaRegistrosPorAlumno() = runTest {
        every { dao.obtenerPorUsuario(7) } returns flowOf(listOf(prospecto))
        assertEquals(4, repo.obtenerPorUsuario(7).first().first().id)
    }

    @Test fun guardarSinServidorConservaPostulacionLocal() = runTest {
        coEvery { dao.insertarProspecto(prospecto) } returns 4L
        val resultado = repo.guardar(prospecto, 0L)
        assertFalse(resultado.postulacionEnServidor)
        assertFalse(resultado.fotoEnServidor)
        coVerify(exactly = 1) { dao.insertarProspecto(prospecto) }
    }

    @Test fun sincronizarSinIdRemotoNoHacePeticiones() = runTest {
        repo.sincronizarPendientes(7, 0L)
        coVerify(exactly = 0) { dao.obtenerPorUsuario(any()) }
    }

    @Test fun noConsultaServidorCuandoUsuarioNoEstaSincronizado() = runTest {
        assertFalse(repo.actualizarDesdeServidor(7, 0L))
    }

    @Test fun cambiaEstadoLocalSinServidor() = runTest {
        coEvery { dao.cambiarEstadoPostulacion(4, "En revisión") } returns Unit
        every { dao.obtenerTodos() } returns flowOf(listOf(prospecto))
        repo.cambiarEstadoPostulacion(4, "En revisión")
        coVerify(exactly = 1) { dao.cambiarEstadoPostulacion(4, "En revisión") }
    }

    @Test fun marcaPostulacionComoSincronizada() = runTest {
        coEvery { dao.marcarComoSincronizado(4) } returns Unit
        repo.marcarComoSincronizado(4)
        coVerify(exactly = 1) { dao.marcarComoSincronizado(4) }
    }

    @Test fun actualizaEstadoDeSincronizacion() = runTest {
        coEvery { dao.actualizarEstado(4, true) } returns Unit
        repo.actualizarEstado(4, true)
        coVerify(exactly = 1) { dao.actualizarEstado(4, true) }
    }

    @Test fun eliminaRegistroLocal() = runTest {
        coEvery { dao.eliminar(prospecto) } returns Unit
        repo.eliminar(prospecto)
        coVerify(exactly = 1) { dao.eliminar(prospecto) }
    }

    private fun remoto(id: Long = 12L, estado: String = "Recibida") = PostulacionApi(
        id = id, usuarioServidorId = 6L, nombre = "Ana Pérez", correo = "ana@correo.cl",
        telefono = "912345678", rut = "12.345.678-5",
        carrera = "Ingeniería en Informática", modalidad = "Presencial",
        estado = estado, fechaCreacion = 123L, tieneFoto = false
    )

    @Test fun enviarAlServidorGuardaElIdRemoto() = runTest {
        coEvery { dao.insertarProspecto(prospecto) } returns 4L
        coEvery { servicio.crear(any()) } returns remoto()
        coEvery { dao.marcarEnServidor(4L, 12L) } returns Unit
        val resultado = repo.guardar(prospecto, 6L)
        assertTrue(resultado.postulacionEnServidor)
        assertFalse(resultado.fotoEnServidor)
        coVerify(exactly = 1) { dao.marcarEnServidor(4L, 12L) }
    }

    @Test fun cuandoFallaElServidorConservaCopiaLocal() = runTest {
        coEvery { dao.insertarProspecto(prospecto) } returns 4L
        coEvery { servicio.crear(any()) } throws IllegalStateException("Sin conexión")
        val resultado = repo.guardar(prospecto, 6L)
        assertFalse(resultado.postulacionEnServidor)
        coVerify(exactly = 1) { dao.insertarProspecto(prospecto) }
    }

    @Test fun sincronizaPostulacionesPendientes() = runTest {
        every { dao.obtenerPorUsuario(7) } returns flowOf(listOf(prospecto.copy(servidorId = 0)))
        coEvery { servicio.crear(any()) } returns remoto()
        coEvery { dao.marcarEnServidor(4L, 12L) } returns Unit
        repo.sincronizarPendientes(7, 6L)
        coVerify(exactly = 1) { servicio.crear(any()) }
    }

    @Test fun recuperaPostulacionQueNoExisteLocalmente() = runTest {
        coEvery { servicio.deUsuario(6L) } returns listOf(remoto())
        coEvery { dao.buscarPorServidorId(12L) } returns null
        coEvery { dao.insertarProspecto(any()) } returns 7L
        assertTrue(repo.actualizarDesdeServidor(7, 6L))
        coVerify(exactly = 1) { dao.insertarProspecto(match {
            it.servidorId == 12L && it.usuarioId == 7 && it.estadoPostulacion == "Recibida"
        }) }
    }

    @Test fun actualizaEstadoSiPostulacionYaExiste() = runTest {
        coEvery { servicio.deUsuario(6L) } returns listOf(remoto(estado = "Aceptada"))
        coEvery { dao.buscarPorServidorId(12L) } returns prospecto.copy(servidorId = 12L)
        coEvery { dao.cambiarEstadoEnServidor(12L, "Aceptada") } returns Unit
        assertTrue(repo.actualizarDesdeServidor(7, 6L))
        coVerify(exactly = 1) { dao.cambiarEstadoEnServidor(12L, "Aceptada") }
    }

    @Test fun avisaCuandoNoSePuedeActualizarDesdeServidor() = runTest {
        coEvery { servicio.deUsuario(6L) } throws IllegalStateException("Sin conexión")
        assertFalse(repo.actualizarDesdeServidor(7, 6L))
    }

    @Test fun actualizaEstadoRemotoYLocal() = runTest {
        coEvery { servicio.cambiarEstado(12L, any()) } returns remoto(estado = "En revisión")
        coEvery { dao.cambiarEstadoEnServidor(12L, "En revisión") } returns Unit
        repo.cambiarEstadoDeServidor(12L, "En revisión")
        coVerify(exactly = 1) { dao.cambiarEstadoEnServidor(12L, "En revisión") }
    }

    @Test fun eliminaRemotoYLocal() = runTest {
        coEvery { servicio.eliminar(12L) } returns Unit
        coEvery { dao.eliminarPorServidorId(12L) } returns Unit
        repo.eliminarDeServidor(12L)
        coVerify(exactly = 1) { servicio.eliminar(12L) }
        coVerify(exactly = 1) { dao.eliminarPorServidorId(12L) }
    }



    @Test fun consultaPendientesYSincronizados() = runTest {
        // Los dos Flow se reciben al construir el repositorio.
        assertTrue(repo.pendientes.first().isEmpty())
        assertTrue(repo.sincronizados.first().isEmpty())
    }

    @Test fun actualizaEstadoEnServidorCuandoLaPostulacionEstaVinculada() = runTest {
        val vinculado = prospecto.copy(servidorId = 12L)
        coEvery { dao.cambiarEstadoPostulacion(4, "Aceptada") } returns Unit
        every { dao.obtenerTodos() } returns flowOf(listOf(vinculado))
        coEvery { servicio.cambiarEstado(12L, any()) } returns remoto(estado = "Aceptada")

        repo.cambiarEstadoPostulacion(4, "Aceptada")

        coVerify(exactly = 1) { dao.cambiarEstadoPostulacion(4, "Aceptada") }
        coVerify(exactly = 1) { servicio.cambiarEstado(12L, match { it.estado == "Aceptada" }) }
    }

    @Test fun fallaElCambioRemotoPeroSeConservaEstadoLocal() = runTest {
        val vinculado = prospecto.copy(servidorId = 12L)
        coEvery { dao.cambiarEstadoPostulacion(4, "Rechazada") } returns Unit
        every { dao.obtenerTodos() } returns flowOf(listOf(vinculado))
        coEvery { servicio.cambiarEstado(12L, any()) } throws IllegalStateException("Sin red")

        repo.cambiarEstadoPostulacion(4, "Rechazada")

        coVerify(exactly = 1) { dao.cambiarEstadoPostulacion(4, "Rechazada") }
        coVerify(exactly = 1) { servicio.cambiarEstado(12L, any()) }
    }

    @Test fun noCambiaEstadoRemotoSiNoEncuentraElRegistroLocal() = runTest {
        coEvery { dao.cambiarEstadoPostulacion(4, "En revisión") } returns Unit
        every { dao.obtenerTodos() } returns flowOf(listOf(prospecto.copy(id = 99, servidorId = 12L)))

        repo.cambiarEstadoPostulacion(4, "En revisión")

        coVerify(exactly = 0) { servicio.cambiarEstado(any(), any()) }
    }

    @Test fun guardarConIdServidorNegativoConservaSoloCopiaLocal() = runTest {
        coEvery { dao.insertarProspecto(prospecto) } returns 4L

        val resultado = repo.guardar(prospecto, -1L)

        assertFalse(resultado.postulacionEnServidor)
        coVerify(exactly = 0) { servicio.crear(any()) }
    }

    @Test fun enviarAlServidorIncluyeLosDatosDeLaPostulacion() = runTest {
        val completo = prospecto.copy(
            rut = "12.345.678-5", cursoInteres = "Derecho", modalidad = "Online"
        )
        coEvery { dao.insertarProspecto(completo) } returns 4L
        coEvery { servicio.crear(any()) } returns remoto()
        coEvery { dao.marcarEnServidor(4L, 12L) } returns Unit

        assertTrue(repo.guardar(completo, 6L).postulacionEnServidor)

        coVerify(exactly = 1) { servicio.crear(match {
            it.usuarioServidorId == 6L && it.nombre == completo.nombre &&
                it.correo == completo.correo && it.telefono == completo.telefono &&
                it.rut == completo.rut && it.carrera == "Derecho" && it.modalidad == "Online"
        }) }
    }

    @Test fun elServidorPuedeGuardarLosDatosAunqueNoExistaLaFotoLocal() = runTest {
        val conFoto = prospecto.copy(fotoRuta = "ruta-que-no-existe/foto.jpg")
        coEvery { dao.insertarProspecto(conFoto) } returns 4L
        coEvery { servicio.crear(any()) } returns remoto()
        coEvery { dao.marcarEnServidor(4L, 12L) } returns Unit

        val resultado = repo.guardar(conFoto, 6L)

        assertTrue(resultado.postulacionEnServidor)
        assertFalse(resultado.fotoEnServidor)
        coVerify(exactly = 1) { dao.marcarEnServidor(4L, 12L) }
        coVerify(exactly = 0) { servicio.subirFoto(any(), any()) }
    }

    @Test fun guardaFotoCuandoDatosYFotografiaSeSubenCorrectamente() = runTest {
        // Se simula la conversión del JPEG: no necesitamos BitmapFactory en la JVM.
        val conFoto = prospecto.copy(fotoRuta = "foto-para-prueba.jpg")
        val repoConFoto = ProspectoRepositorio(dao, servicio, { byteArrayOf(1, 2, 3, 4) })
        coEvery { dao.insertarProspecto(conFoto) } returns 4L
        coEvery { servicio.crear(any()) } returns remoto()
        coEvery { dao.marcarEnServidor(4L, 12L) } returns Unit
        coEvery { servicio.subirFoto(12L, any()) } returns remoto().copy(tieneFoto = true)
        coEvery { dao.marcarFotoEnServidor(4L) } returns Unit

        val resultado = repoConFoto.guardar(conFoto, 6L)

        assertTrue(resultado.postulacionEnServidor)
        assertTrue(resultado.fotoEnServidor)
        coVerify(exactly = 1) { servicio.subirFoto(12L, any()) }
        coVerify(exactly = 1) { dao.marcarFotoEnServidor(4L) }
    }

    @Test fun fallaLaSubidaDeFotoPeroNoSeDuplicaLaPostulacion() = runTest {
        val conFoto = prospecto.copy(fotoRuta = "foto-para-prueba.jpg")
        val repoConFoto = ProspectoRepositorio(dao, servicio, { byteArrayOf(1, 2, 3) })
        coEvery { dao.insertarProspecto(conFoto) } returns 4L
        coEvery { servicio.crear(any()) } returns remoto()
        coEvery { dao.marcarEnServidor(4L, 12L) } returns Unit
        coEvery { servicio.subirFoto(12L, any()) } throws IllegalStateException("Error foto")

        val resultado = repoConFoto.guardar(conFoto, 6L)

        assertTrue(resultado.postulacionEnServidor)
        assertFalse(resultado.fotoEnServidor)
        coVerify(exactly = 1) { servicio.crear(any()) }
        coVerify(exactly = 0) { dao.marcarFotoEnServidor(any()) }
    }

    @Test fun sincronizaFotoPendienteSinVolverACrearLaPostulacion() = runTest {
        val conFoto = prospecto.copy(servidorId = 12L, fotoRuta = "foto-para-prueba.jpg", fotoEnServidor = false)
        val repoConFoto = ProspectoRepositorio(dao, servicio, { byteArrayOf(1, 2, 3) })
        every { dao.obtenerPorUsuario(7) } returns flowOf(listOf(conFoto))
        coEvery { servicio.subirFoto(12L, any()) } returns remoto().copy(tieneFoto = true)
        coEvery { dao.marcarFotoEnServidor(4L) } returns Unit

        repoConFoto.sincronizarPendientes(7, 6L)

        coVerify(exactly = 0) { servicio.crear(any()) }
        coVerify(exactly = 1) { servicio.subirFoto(12L, any()) }
        coVerify(exactly = 1) { dao.marcarFotoEnServidor(4L) }
    }

    @Test fun noReenviaLaFotoCuandoYaEstaEnElServidor() = runTest {
        val completo = prospecto.copy(servidorId = 12L, fotoRuta = "foto.jpg", fotoEnServidor = true)
        every { dao.obtenerPorUsuario(7) } returns flowOf(listOf(completo))

        repo.sincronizarPendientes(7, 6L)

        coVerify(exactly = 0) { servicio.crear(any()) }
        coVerify(exactly = 0) { servicio.subirFoto(any(), any()) }
    }

    @Test fun noReenviaPostulacionYaSincronizadaSinFoto() = runTest {
        every { dao.obtenerPorUsuario(7) } returns flowOf(listOf(prospecto.copy(servidorId = 12L)))

        repo.sincronizarPendientes(7, 6L)

        coVerify(exactly = 0) { servicio.crear(any()) }
        coVerify(exactly = 0) { servicio.subirFoto(any(), any()) }
    }

    @Test fun puedeSincronizarListaDePostulacionesVacia() = runTest {
        every { dao.obtenerPorUsuario(7) } returns flowOf(emptyList())

        repo.sincronizarPendientes(7, 6L)

        coVerify(exactly = 0) { servicio.crear(any()) }
    }

    @Test fun sincronizacionRemotaMarcaFotoSiYaSeEncuentraEnServidor() = runTest {
        coEvery { servicio.deUsuario(6L) } returns listOf(remoto().copy(tieneFoto = true))
        coEvery { dao.buscarPorServidorId(12L) } returns prospecto.copy(servidorId = 12L)
        coEvery { dao.cambiarEstadoEnServidor(12L, "Recibida") } returns Unit
        coEvery { dao.marcarFotoRemota(12L) } returns Unit

        assertTrue(repo.actualizarDesdeServidor(7, 6L))

        coVerify(exactly = 1) { dao.marcarFotoRemota(12L) }
        coVerify(exactly = 1) { dao.cambiarEstadoEnServidor(12L, "Recibida") }
    }

    @Test fun recuperaPostulacionRemotaQueTieneFotografia() = runTest {
        coEvery { servicio.deUsuario(6L) } returns listOf(remoto().copy(tieneFoto = true))
        coEvery { dao.buscarPorServidorId(12L) } returns null
        coEvery { dao.insertarProspecto(any()) } returns 9L

        assertTrue(repo.actualizarDesdeServidor(7, 6L))

        coVerify(exactly = 1) { dao.insertarProspecto(match {
            it.fotoEnServidor && it.esSincronizado && it.servidorId == 12L && it.usuarioId == 7
        }) }
    }

    @Test fun listaVaciaDelServidorEsUnaSincronizacionValida() = runTest {
        coEvery { servicio.deUsuario(6L) } returns emptyList()

        assertTrue(repo.actualizarDesdeServidor(7, 6L))

        coVerify(exactly = 0) { dao.insertarProspecto(any()) }
    }

    @Test fun alFallarLaEliminacionRemotaNoBorraElRegistroLocal() = runTest {
        coEvery { servicio.eliminar(12L) } throws IllegalStateException("Sin conexión")

        try {
            repo.eliminarDeServidor(12L)
            fail("Se esperaba la excepción de la eliminación remota")
        } catch (_: IllegalStateException) {
            // No se elimina localmente si el servidor falló.
        }

        coVerify(exactly = 0) { dao.eliminarPorServidorId(12L) }
    }

    @Test fun alFallarElCambioDeEstadoRemotoNoCambiaEstadoLocal() = runTest {
        coEvery { servicio.cambiarEstado(12L, any()) } throws IllegalStateException("Sin conexión")

        try {
            repo.cambiarEstadoDeServidor(12L, "Aceptada")
            fail("Se esperaba la excepción del servidor")
        } catch (_: IllegalStateException) {
            // Se deja el estado local como estaba.
        }

        coVerify(exactly = 0) { dao.cambiarEstadoEnServidor(any(), any()) }
    }

}
