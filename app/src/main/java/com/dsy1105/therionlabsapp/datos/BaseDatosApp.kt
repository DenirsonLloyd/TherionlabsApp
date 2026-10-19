package com.dsy1105.therionlabsapp.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProspectoEntidad::class], version = 1, exportSchema = false)
abstract class BaseDatosApp : RoomDatabase() {

    abstract fun prospectoDao(): ProspectoDao

    companion object {
        @Volatile
        private var INSTANCE: BaseDatosApp? = null

        fun obtenerBaseDatos(context: Context): BaseDatosApp {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosApp::class.java,
                    "therionlabs_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}