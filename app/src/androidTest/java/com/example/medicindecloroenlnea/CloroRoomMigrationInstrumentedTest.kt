package com.example.medicindecloroenlnea

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.medicindecloroenlnea.data.local.room.CloroRoomDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CloroRoomMigrationInstrumentedTest {
    @Test
    fun migraLaBaseSQLiteExistenteConservandoPuntosMedicionesYAlertas() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "room-migration-test.db"
        context.deleteDatabase(databaseName)
        val legacyDatabase = context.openOrCreateDatabase(databaseName, 0, null)
        legacyDatabase.setForeignKeyConstraintsEnabled(true)
        legacyDatabase.execSQL("CREATE TABLE granjas (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL)")
        legacyDatabase.execSQL(
            """CREATE TABLE puntos (
                id INTEGER PRIMARY KEY,
                granja_id INTEGER NOT NULL REFERENCES granjas(id),
                nombre TEXT NOT NULL,
                codigo TEXT NOT NULL,
                galpon TEXT NOT NULL,
                tipo TEXT NOT NULL,
                nivel REAL NOT NULL,
                actualizado TEXT NOT NULL,
                critico_bajo REAL NOT NULL DEFAULT 2,
                normal_bajo REAL NOT NULL DEFAULT 3,
                normal_alto REAL NOT NULL DEFAULT 5,
                critico_alto REAL NOT NULL DEFAULT 6
            )"""
        )
        legacyDatabase.execSQL(
            """CREATE TABLE mediciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                punto_id INTEGER NOT NULL REFERENCES puntos(id),
                nivel REAL NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL,
                direccion TEXT,
                sincronizada INTEGER NOT NULL DEFAULT 0,
                usuario_id INTEGER
            )"""
        )
        legacyDatabase.execSQL(
            """CREATE TABLE alertas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                punto_id INTEGER NOT NULL REFERENCES puntos(id),
                medicion_id INTEGER NOT NULL REFERENCES mediciones(id),
                nivel REAL NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL,
                direccion TEXT NOT NULL,
                atendida INTEGER NOT NULL DEFAULT 0,
                cerrada_por TEXT,
                medicion_verificada_id INTEGER REFERENCES mediciones(id)
            )"""
        )
        legacyDatabase.execSQL("INSERT INTO granjas VALUES (7, 'Granja migrada')")
        legacyDatabase.execSQL(
            """INSERT INTO puntos
                (id, granja_id, nombre, codigo, galpon, tipo, nivel, actualizado)
                VALUES (11, 7, 'Punto migrado', 'P-11', 'Galpón 1', 'Bebedero', 1.5, '2026-10-29T12:00:00')"""
        )
        legacyDatabase.execSQL(
            """INSERT INTO mediciones
                (id, punto_id, nivel, fecha_hora, estado, direccion, sincronizada)
                VALUES (21, 11, 1.5, '2026-10-29T12:00:00', 'CRITICO', 'INSUFICIENTE', 0)"""
        )
        legacyDatabase.execSQL(
            """INSERT INTO alertas
                (id, punto_id, medicion_id, nivel, fecha_hora, estado, direccion)
                VALUES (31, 11, 21, 1.5, '2026-10-29T12:00:00', 'CRITICO', 'INSUFICIENTE')"""
        )
        legacyDatabase.version = 1
        legacyDatabase.close()

        val roomDatabase = Room.databaseBuilder(
            context,
            CloroRoomDatabase::class.java,
            databaseName
        )
            .addMigrations(CloroRoomDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        try {
            roomDatabase.openHelper.writableDatabase
            val point = roomDatabase.puntoDao().observarPuntos(listOf(7)).first().single()
            assertEquals(11, point.id)
            assertEquals("Punto migrado", point.nombre)
            assertEquals(1, roomDatabase.puntoDao().contarMediciones(point.id))
            assertEquals(1, roomDatabase.puntoDao().contarAlertas(point.id))
        } finally {
            roomDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }
}
