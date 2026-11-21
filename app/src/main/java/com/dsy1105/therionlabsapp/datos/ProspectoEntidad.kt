package com.dsy1105.therionlabsapp.datos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prospectos")
data class ProspectoEntidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val nivelEducativo: String = "",
    val areaInteres: String = "",
    val cursoInteres: String = "",
    val modalidad: String = "Presencial",
    val rut: String = "",
    val fechaNacimiento: String = "",
    val direccion: String = "",
    val facultad: String = "",
    val fotoRuta: String = "",
    @ColumnInfo(defaultValue = "0") val fotoEnServidor: Boolean = false,
    // 0 corresponde a registros antiguos, anteriores a las cuentas locales.
    @ColumnInfo(defaultValue = "0") val usuarioId: Int = 0,
    @ColumnInfo(defaultValue = "'Recibida'") val estadoPostulacion: String = "Recibida",

    @ColumnInfo(name = "esSincronizado")
    val esSincronizado: Boolean = false,
    val fechaCreacion: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val servidorId: Long = 0
)