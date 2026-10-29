package com.example.medicindecloroenlnea

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.medicindecloroenlnea.data.local.room.AlertaRoomEntity
import com.example.medicindecloroenlnea.data.local.room.CloroRoomDatabase
import com.example.medicindecloroenlnea.data.local.room.GranjaRoomEntity
import com.example.medicindecloroenlnea.data.local.room.MedicionRoomEntity
import com.example.medicindecloroenlnea.data.local.room.PuntoRoomEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PuntoRoomDaoInstrumentedTest {
    @Test
    fun creaActualizaLeeYProtegePuntosConHistorial() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, CloroRoomDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        try {
            val dao = database.puntoDao()
            dao.insertarGranja(GranjaRoomEntity(id = 1, nombre = "Granja de prueba"))

            dao.crearPuntoConMedicionInicial(
                punto = PuntoRoomEntity(
                    granjaId = 1,
                    nombre = "Punto uno",
                    codigo = "P-TEST",
                    galpon = "Galpón 1",
                    tipo = "Bebedero",
                    nivel = 1.5,
                    actualizado = "2026-10-29T12:00:00"
                ),
                medicion = MedicionRoomEntity(
                    puntoId = 0,
                    nivel = 1.5,
                    fechaHora = "2026-10-29T12:00:00",
                    estado = "CRITICO",
                    direccion = "INSUFICIENTE"
                ),
                alerta = AlertaRoomEntity(
                    puntoId = 0,
                    medicionId = 0,
                    nivel = 1.5,
                    fechaHora = "2026-10-29T12:00:00",
                    estado = "CRITICO",
                    direccion = "INSUFICIENTE"
                )
            )

            val point = dao.observarPuntos(listOf(1)).first().single()
            assertEquals("P-TEST", point.codigo)
            assertEquals(1, dao.contarMediciones(point.id))
            assertEquals(1, dao.contarAlertas(point.id))

            dao.actualizarPunto(point.copy(nombre = "Punto actualizado"))
            assertEquals("Punto actualizado", dao.obtenerPunto(point.id)?.nombre)
            assertFalse(dao.eliminarSiNoTieneHistorial(point))

            val emptyPointId = dao.insertarPunto(
                PuntoRoomEntity(
                    granjaId = 1,
                    nombre = "Sin historial",
                    codigo = "P-EMPTY",
                    galpon = "Galpón 2",
                    tipo = "Bebedero",
                    nivel = 4.0,
                    actualizado = "2026-10-29T12:00:00"
                )
            ).toInt()
            val emptyPoint = requireNotNull(dao.obtenerPunto(emptyPointId))
            assertTrue(dao.eliminarSiNoTieneHistorial(emptyPoint))
            assertEquals(null, dao.obtenerPunto(emptyPointId))
        } finally {
            database.close()
        }
    }
}
