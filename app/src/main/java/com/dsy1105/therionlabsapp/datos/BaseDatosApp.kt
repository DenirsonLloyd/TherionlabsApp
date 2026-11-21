package com.dsy1105.therionlabsapp.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ProspectoEntidad::class, UsuarioEntidad::class], version = 7, exportSchema = false)
abstract class BaseDatosApp : RoomDatabase() {

    abstract fun prospectoDao(): ProspectoDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE prospectos ADD COLUMN rut TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE prospectos ADD COLUMN fechaNacimiento TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE prospectos ADD COLUMN direccion TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE prospectos ADD COLUMN facultad TEXT NOT NULL DEFAULT ''")
            }
        }

        // No borramos los prospectos antiguos al agregar la tabla de usuarios.
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nombre TEXT NOT NULL, correo TEXT NOT NULL, claveHash TEXT NOT NULL, salt TEXT NOT NULL)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_usuarios_correo ON usuarios (correo)")
            }
        }

        // Nueva columna sin perder los registros anteriores.
        private val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE prospectos ADD COLUMN fotoRuta TEXT NOT NULL DEFAULT ''")
            }
        }

        // En la versión 5 vinculamos las postulaciones con cada usuario.
        // Los registros anteriores quedan como registros administrativos (usuarioId = 0).
        private val MIGRACION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE prospectos ADD COLUMN usuarioId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE prospectos ADD COLUMN estadoPostulacion TEXT NOT NULL DEFAULT 'Recibida'")
            }
        }

        private val MIGRACION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE usuarios ADD COLUMN servidorId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE prospectos ADD COLUMN servidorId INTEGER NOT NULL DEFAULT 0")
            }
        }

        // La nueva columna permite reintentar la subida sin crear otra postulación.
        private val MIGRACION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE prospectos ADD COLUMN fotoEnServidor INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: BaseDatosApp? = null

        fun obtenerBaseDatos(context: Context): BaseDatosApp {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosApp::class.java,
                    "therionlabs_db"
                ).addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4, MIGRACION_4_5, MIGRACION_5_6, MIGRACION_6_7).build()
                INSTANCE = instance
                instance
            }
        }
    }
}