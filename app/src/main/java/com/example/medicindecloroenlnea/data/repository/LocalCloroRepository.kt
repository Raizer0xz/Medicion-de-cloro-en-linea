package com.example.medicindecloroenlnea.data.repository

import android.content.Context
import com.example.medicindecloroenlnea.data.local.AccionLocal
import com.example.medicindecloroenlnea.data.local.AlertaLocal
import com.example.medicindecloroenlnea.data.local.CloroLocalDatabase
import com.example.medicindecloroenlnea.data.local.MedicionLocal
import com.example.medicindecloroenlnea.data.local.UsuarioAutenticado
import com.example.medicindecloroenlnea.domain.model.Accion
import com.example.medicindecloroenlnea.domain.model.Alerta
import com.example.medicindecloroenlnea.domain.model.Granja
import com.example.medicindecloroenlnea.domain.model.Medicion
import com.example.medicindecloroenlnea.domain.model.PuntoMedicion
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.domain.model.calcularEstado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

class LocalCloroRepository(context: Context) : CloroRepository {
    private val database = CloroLocalDatabase.getInstance(context)

    override fun obtenerGranjas(): Flow<List<Granja>> = observar {
        database.obtenerGranjas(null).map { Granja(it.first, it.second) }
    }

    fun obtenerGranjas(granjasPermitidas: List<Int>): Flow<List<Granja>> = observar {
        database.obtenerGranjas(granjasPermitidas).map { Granja(it.first, it.second) }
    }

    override fun obtenerPuntos(granjaId: Int): Flow<List<PuntoMedicion>> = observar {
        database.obtenerPuntos(granjaId, null).map { point ->
            PuntoMedicion(
                id = point.id,
                granjaId = point.granjaId,
                nombre = point.nombre,
                nivelActual = point.nivel,
                estado = calcularEstado(point.nivel, point.rango),
                ultimaActualizacion = point.actualizado,
                codigo = point.codigo,
                galpon = point.galpon,
                tipo = point.tipo,
                rango = point.rango
            )
        }
    }

    fun obtenerPuntos(granjaId: Int, granjasPermitidas: List<Int>): Flow<List<PuntoMedicion>> = observar {
        database.obtenerPuntos(granjaId, granjasPermitidas).map { point ->
            PuntoMedicion(
                id = point.id,
                granjaId = point.granjaId,
                nombre = point.nombre,
                nivelActual = point.nivel,
                estado = calcularEstado(point.nivel, point.rango),
                ultimaActualizacion = point.actualizado,
                codigo = point.codigo,
                galpon = point.galpon,
                tipo = point.tipo,
                rango = point.rango
            )
        }
    }

    override fun obtenerMediciones(puntoId: Int): Flow<List<Medicion>> = observar {
        database.obtenerMediciones(puntoId).map { it.toDomain() }
    }

    fun obtenerMediciones(puntoId: Int, granjasPermitidas: List<Int>): Flow<List<Medicion>> = observar {
        database.obtenerMediciones(puntoId, granjasPermitidas).map { it.toDomain() }
    }

    override fun obtenerAlertas(): Flow<List<Alerta>> = observar {
        database.obtenerAlertas().map { it.toDomain() }
    }

    fun obtenerAlertasLocales(): Flow<List<AlertaLocal>> = observar(database::obtenerAlertas)

    fun obtenerPendientesSincronizacion(granjasPermitidas: List<Int>): Flow<Int> =
        observar { database.contarPendientesSincronizacion(granjasPermitidas) }

    fun obtenerAcciones(alertaId: Long): Flow<List<Accion>> = observar {
        database.obtenerAcciones(alertaId).map { it.toDomain() }
    }

    fun registrarUsuario(
        nombre: String,
        correo: String,
        direccion: String,
        clave: String,
        rol: RolUsuario,
        granjasIds: List<Int>
    ): UsuarioAutenticado? = database.registrarUsuario(nombre, correo, direccion, clave, rol, granjasIds)

    fun autenticar(correo: String, clave: String): UsuarioAutenticado? =
        database.autenticar(correo, clave)

    fun registrarMedicion(
        puntoId: Int,
        nivel: Double,
        usuarioId: Long?,
        granjasPermitidas: List<Int>
    ): Boolean {
        if (usuarioId != null && database.obtenerGranjasUsuario(usuarioId).none { it in granjasPermitidas }) {
            return false
        }
        return database.registrarMedicion(puntoId, nivel, usuarioId)
    }

    fun registrarAccion(
        alertaId: Long,
        descripcion: String,
        responsable: String,
        usuarioId: Long,
        granjasPermitidas: List<Int>
    ): Boolean = database.registrarAccion(
        alertaId,
        descripcion,
        responsable,
        usuarioId,
        granjasPermitidas
    )

    fun cerrarAlerta(alertaId: Long, usuarioId: Long): Boolean =
        database.cerrarAlerta(alertaId, usuarioId)

    fun simularMediciones() = database.simularMediciones()

    private fun <T> observar(query: () -> T): Flow<T> = flow {
        emit(query())
        database.cambios.collect { emit(query()) }
    }.flowOn(Dispatchers.IO)

    private fun MedicionLocal.toDomain() = Medicion(
        id = id.toInt(),
        puntoId = puntoId,
        nivelCloro = valor,
        fechaHora = fechaHora,
        estado = estado,
        direccion = direccion,
        sincronizada = sincronizada
    )

    private fun AlertaLocal.toDomain() = Alerta(
        id = id.toInt(),
        puntoId = puntoId,
        nivelCloro = valor,
        estado = estado,
        fechaHora = fechaHora,
        atendida = atendida,
        direccion = direccion
    )

    private fun AccionLocal.toDomain() = Accion(
        id = id.toInt(),
        puntoId = puntoId,
        descripcion = descripcion,
        responsable = responsable,
        fechaHora = fechaHora,
        alertaId = alertaId.toInt(),
        estado = estado,
        medicionId = medicionId.toInt()
    )
}
