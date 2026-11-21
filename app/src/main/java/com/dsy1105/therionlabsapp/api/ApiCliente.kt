package com.dsy1105.therionlabsapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeUnit


object ApiCliente {
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private fun retrofit(url: String) = Retrofit.Builder()
        .baseUrl(url)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val usuarios: UsuariosApi =
        retrofit("http://107.21.117.137:8081/")
            .create(UsuariosApi::class.java)

    val postulaciones: PostulacionesApi =
        retrofit("http://107.21.117.137:8082/")
            .create(PostulacionesApi::class.java)
    val feriados: FeriadosApi = retrofit("https://date.nager.at/").create(FeriadosApi::class.java)
}

data class DatosUsuarioApi(val nombre: String, val correo: String)
data class UsuarioApi(val id: Long, val nombre: String, val correo: String)
interface UsuariosApi {
    @GET("api/usuarios") suspend fun listar(): List<UsuarioApi>
    @GET("api/usuarios/correo/{correo}") suspend fun porCorreo(@Path("correo") correo: String): UsuarioApi
    @POST("api/usuarios") suspend fun crear(@Body usuario: DatosUsuarioApi): UsuarioApi
    @PUT("api/usuarios/{id}") suspend fun actualizar(@Path("id") id: Long, @Body usuario: DatosUsuarioApi): UsuarioApi
    @DELETE("api/usuarios/{id}") suspend fun eliminar(@Path("id") id: Long)
}

data class DatosPostulacionApi(
    val usuarioServidorId: Long,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val rut: String,
    val carrera: String,
    val modalidad: String
)
data class PostulacionApi(
    val id: Long, val usuarioServidorId: Long, val nombre: String, val correo: String,
    val telefono: String, val rut: String, val carrera: String, val modalidad: String,
    val estado: String, val fechaCreacion: Long, val tieneFoto: Boolean = false
)
data class EstadoApi(val estado: String)
interface PostulacionesApi {
    @GET("api/postulaciones") suspend fun listar(): List<PostulacionApi>
    @GET("api/postulaciones/usuario/{id}") suspend fun deUsuario(@Path("id") id: Long): List<PostulacionApi>
    @POST("api/postulaciones") suspend fun crear(@Body postulacion: DatosPostulacionApi): PostulacionApi
    @PUT("api/postulaciones/{id}") suspend fun actualizar(@Path("id") id: Long, @Body postulacion: DatosPostulacionApi): PostulacionApi
    @PUT("api/postulaciones/{id}/estado") suspend fun cambiarEstado(@Path("id") id: Long, @Body estado: EstadoApi): PostulacionApi
    @Multipart
    @POST("api/postulaciones/{id}/foto")
    suspend fun subirFoto(@Path("id") id: Long, @Part foto: MultipartBody.Part): PostulacionApi
    @Streaming
    @GET("api/postulaciones/{id}/foto")
    suspend fun descargarFoto(@Path("id") id: Long): Response<ResponseBody>
    @DELETE("api/postulaciones/{id}") suspend fun eliminar(@Path("id") id: Long)
}

data class FeriadoApi(val date: String, val localName: String, val name: String)
interface FeriadosApi {
    @GET("api/v3/PublicHolidays/{anio}/CL") suspend fun obtenerChile(@Path("anio") anio: Int): List<FeriadoApi>
}
