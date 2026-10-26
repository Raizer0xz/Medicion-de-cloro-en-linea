package com.example.medicindecloroenlnea.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import com.example.medicindecloroenlnea.domain.model.DireccionDesviacion
import com.example.medicindecloroenlnea.domain.model.EstadoCloro
import com.example.medicindecloroenlnea.domain.model.RangoCloro
import com.example.medicindecloroenlnea.domain.model.RolUsuario
import com.example.medicindecloroenlnea.domain.model.calcularDireccion
import com.example.medicindecloroenlnea.domain.model.calcularEstado
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.flow.MutableSharedFlow

data class UsuarioAutenticado(
    val id: Long,
    val nombre: String,
    val correo: String,
    val direccion: String,
    val rol: RolUsuario,
    val granjasIds: List<Int>
)

data class MedicionLocal(
    val id: Long,
    val puntoId: Int,
    val valor: Double,
    val fechaHora: String,
    val estado: EstadoCloro,
    val direccion: DireccionDesviacion?,
    val sincronizada: Boolean
)

data class AlertaLocal(
    val id: Long,
    val puntoId: Int,
    val granjaId: Int,
    val puntoNombre: String,
    val valor: Double,
    val fechaHora: String,
    val estado: EstadoCloro,
    val direccion: DireccionDesviacion,
    val atendida: Boolean,
    val cerradaPor: String?,
    val tieneAccion: Boolean,
    val ultimaMedicionNormal: Boolean
)

data class AccionLocal(
    val id: Long,
    val alertaId: Long,
    val puntoId: Int,
    val medicionId: Long,
    val descripcion: String,
    val responsable: String,
    val fechaHora: String,
    val estado: String
)

class CloroLocalDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    val cambios = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE granjas (
                id INTEGER PRIMARY KEY,
                nombre TEXT NOT NULL
            )"""
        )
        db.execSQL(
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
        db.execSQL(
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
        db.execSQL(
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
        db.execSQL(
            """CREATE TABLE acciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alerta_id INTEGER NOT NULL REFERENCES alertas(id),
                medicion_id INTEGER NOT NULL REFERENCES mediciones(id),
                descripcion TEXT NOT NULL,
                responsable TEXT NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL DEFAULT 'ABIERTA'
            )"""
        )
        db.execSQL(
            """CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL UNIQUE COLLATE NOCASE,
                direccion TEXT NOT NULL,
                rol TEXT NOT NULL,
                sal TEXT NOT NULL,
                clave_hash TEXT NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE usuario_granjas (
                usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                granja_id INTEGER NOT NULL REFERENCES granjas(id),
                PRIMARY KEY(usuario_id, granja_id)
            )"""
        )
        db.execSQL("CREATE INDEX idx_mediciones_punto_fecha ON mediciones(punto_id, fecha_hora DESC)")
        db.execSQL("CREATE INDEX idx_alertas_estado_fecha ON alertas(atendida, fecha_hora DESC)")
        sembrarDatos(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        throw IllegalStateException("Falta definir la migración de la base local $oldVersion -> $newVersion.")
    }

    fun obtenerGranjas(ids: List<Int>?): List<Pair<Int, String>> {
        val db = readableDatabase
        val seleccion = ids?.takeIf { it.isNotEmpty() }?.let { values ->
            "id IN (${values.joinToString(",") { "?" }})" to values.map(Int::toString).toTypedArray()
        } ?: if (ids == null) null else return emptyList()
        val cursor = db.query(
            "granjas",
            arrayOf("id", "nombre"),
            seleccion?.first,
            seleccion?.second,
            null,
            null,
            "id"
        )
        return cursor.use { result ->
            buildList {
                while (result.moveToNext()) add(result.getInt(0) to result.getString(1))
            }
        }
    }

    fun obtenerPuntos(granjaId: Int, granjasPermitidas: List<Int>?): List<PuntoLocal> {
        if (granjasPermitidas != null && granjaId !in granjasPermitidas) return emptyList()
        val cursor = readableDatabase.query(
            "puntos",
            arrayOf(
                "id", "granja_id", "nombre", "codigo", "galpon", "tipo", "nivel", "actualizado",
                "critico_bajo", "normal_bajo", "normal_alto", "critico_alto"
            ),
            "granja_id = ?",
            arrayOf(granjaId.toString()),
            null,
            null,
            "id"
        )
        return cursor.use { result ->
            buildList {
                while (result.moveToNext()) {
                    add(
                        PuntoLocal(
                            id = result.getInt(0),
                            granjaId = result.getInt(1),
                            nombre = result.getString(2),
                            codigo = result.getString(3),
                            galpon = result.getString(4),
                            tipo = result.getString(5),
                            nivel = result.getDouble(6),
                            actualizado = result.getString(7),
                            rango = RangoCloro(
                                criticoBajo = result.getDouble(8),
                                normalBajo = result.getDouble(9),
                                normalAlto = result.getDouble(10),
                                criticoAlto = result.getDouble(11)
                            )
                        )
                    )
                }
            }
        }
    }

    fun obtenerMediciones(puntoId: Int, granjasPermitidas: List<Int>? = null): List<MedicionLocal> {
        if (granjasPermitidas != null && granjasPermitidas.isEmpty()) return emptyList()
        val query = if (granjasPermitidas == null) {
            "SELECT id, punto_id, nivel, fecha_hora, estado, direccion, sincronizada FROM mediciones WHERE punto_id = ? ORDER BY fecha_hora DESC, id DESC LIMIT 500" to
                arrayOf(puntoId.toString())
        } else {
            """SELECT m.id, m.punto_id, m.nivel, m.fecha_hora, m.estado, m.direccion, m.sincronizada
                FROM mediciones m JOIN puntos p ON p.id = m.punto_id
                WHERE m.punto_id = ? AND p.granja_id IN (${granjasPermitidas.joinToString(",") { "?" }})
                ORDER BY m.fecha_hora DESC, m.id DESC LIMIT 500""" to
                (listOf(puntoId.toString()) + granjasPermitidas.map(Int::toString)).toTypedArray()
        }
        val cursor = readableDatabase.rawQuery(query.first, query.second)
        return cursor.use { result ->
            buildList {
                while (result.moveToNext()) {
                    add(
                        MedicionLocal(
                            id = result.getLong(0),
                            puntoId = result.getInt(1),
                            valor = result.getDouble(2),
                            fechaHora = result.getString(3),
                            estado = EstadoCloro.valueOf(result.getString(4)),
                            direccion = result.getString(5)?.let(DireccionDesviacion::valueOf),
                            sincronizada = result.getInt(6) == 1
                        )
                    )
                }
            }
        }
    }

    fun obtenerAlertas(): List<AlertaLocal> {
        val cursor = readableDatabase.rawQuery(
            """SELECT a.id, a.punto_id, p.granja_id, p.nombre, a.nivel, a.fecha_hora, a.estado, a.direccion,
                a.atendida, a.medicion_verificada_id,
                EXISTS(SELECT 1 FROM acciones x WHERE x.alerta_id = a.id),
                (SELECT m.estado FROM mediciones m WHERE m.id = a.medicion_verificada_id),
                (SELECT m.estado FROM mediciones m WHERE m.punto_id = a.punto_id
                    ORDER BY m.fecha_hora DESC, m.id DESC LIMIT 1),
                (SELECT u.nombre FROM usuarios u WHERE u.id = CAST(a.cerrada_por AS INTEGER))
                FROM alertas a JOIN puntos p ON p.id = a.punto_id
                ORDER BY a.atendida ASC, a.fecha_hora DESC""",
            null
        )
        return cursor.use { result ->
            buildList {
                while (result.moveToNext()) {
                    add(
                        AlertaLocal(
                            id = result.getLong(0),
                            puntoId = result.getInt(1),
                            granjaId = result.getInt(2),
                            puntoNombre = result.getString(3),
                            valor = result.getDouble(4),
                            fechaHora = result.getString(5),
                            estado = EstadoCloro.valueOf(result.getString(6)),
                            direccion = DireccionDesviacion.valueOf(result.getString(7)),
                            atendida = result.getInt(8) == 1,
                            cerradaPor = result.getString(13),
                            tieneAccion = result.getInt(10) == 1,
                            ultimaMedicionNormal = result.getString(12) == EstadoCloro.NORMAL.name
                        )
                    )
                }
            }
        }
    }

    fun obtenerAcciones(alertaId: Long): List<AccionLocal> {
        val cursor = readableDatabase.query(
            "acciones",
            arrayOf("id", "alerta_id", "medicion_id", "descripcion", "responsable", "fecha_hora", "estado"),
            "alerta_id = ?",
            arrayOf(alertaId.toString()),
            null,
            null,
            "fecha_hora DESC"
        )
        return cursor.use { result ->
            buildList {
                while (result.moveToNext()) {
                    val pointId = readableDatabase.rawQuery(
                        "SELECT punto_id FROM alertas WHERE id = ?",
                        arrayOf(result.getLong(1).toString())
                    ).use { alert ->
                        if (alert.moveToFirst()) alert.getInt(0) else 0
                    }
                    add(
                        AccionLocal(
                            result.getLong(0),
                            result.getLong(1),
                            pointId,
                            result.getLong(2),
                            result.getString(3),
                            result.getString(4),
                            result.getString(5),
                            result.getString(6)
                        )
                    )
                }
            }
        }
    }

    fun registrarUsuario(
        nombre: String,
        correo: String,
        direccion: String,
        clave: String,
        rol: RolUsuario,
        granjasIds: List<Int>
    ): UsuarioAutenticado? {
        if (granjasIds.isEmpty()) return null
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val values = ContentValues().apply {
            put("nombre", nombre.trim())
            put("correo", correo.trim())
            put("direccion", direccion.trim())
            put("rol", rol.name)
            put("sal", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("clave_hash", hashPassword(clave, salt))
        }
        val db = writableDatabase
        db.beginTransaction()
        val id: Long
        try {
            id = db.insertWithOnConflict("usuarios", null, values, SQLiteDatabase.CONFLICT_IGNORE)
            if (id == -1L) return null
            granjasIds.distinct().forEach { granjaId ->
                db.insertOrThrow(
                    "usuario_granjas",
                    null,
                    ContentValues().apply {
                        put("usuario_id", id)
                        put("granja_id", granjaId)
                    }
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        cambios.tryEmit(Unit)
        return UsuarioAutenticado(id, nombre.trim(), correo.trim(), direccion.trim(), rol, granjasIds.distinct())
    }

    fun autenticar(correo: String, clave: String): UsuarioAutenticado? {
        val db = readableDatabase
        val cursor = db.query(
            "usuarios",
            arrayOf("id", "nombre", "correo", "direccion", "rol", "sal", "clave_hash"),
            "correo = ?",
            arrayOf(correo.trim()),
            null,
            null,
            null,
            "1"
        )
        val user = cursor.use { result ->
            if (!result.moveToFirst()) return null
            val salt = Base64.decode(result.getString(5), Base64.NO_WRAP)
            val expectedHash = result.getString(6)
            if (!constantTimeEquals(expectedHash, hashPassword(clave, salt))) return null
            UsuarioAutenticado(
                id = result.getLong(0),
                nombre = result.getString(1),
                correo = result.getString(2),
                direccion = result.getString(3),
                rol = RolUsuario.valueOf(result.getString(4)),
                granjasIds = emptyList()
            )
        }
        return user.copy(granjasIds = obtenerGranjasUsuario(user.id))
    }

    fun obtenerGranjasUsuario(usuarioId: Long): List<Int> {
        val cursor = readableDatabase.query(
            "usuario_granjas",
            arrayOf("granja_id"),
            "usuario_id = ?",
            arrayOf(usuarioId.toString()),
            null,
            null,
            "granja_id"
        )
        return cursor.use { result ->
            buildList {
                while (result.moveToNext()) add(result.getInt(0))
            }
        }
    }

    fun registrarMedicion(puntoId: Int, nivel: Double, usuarioId: Long?): Boolean {
        if (!nivel.isFinite() || nivel < 0.0 || nivel > MAX_NIVEL) return false
        val db = writableDatabase
        if (usuarioId != null) {
            val permitted = db.rawQuery(
                """SELECT EXISTS(SELECT 1 FROM usuario_granjas ug
                    JOIN puntos p ON p.granja_id = ug.granja_id
                    WHERE ug.usuario_id = ? AND p.id = ?)""",
                arrayOf(usuarioId.toString(), puntoId.toString())
            ).use { it.moveToFirst() && it.getInt(0) == 1 }
            if (!permitted) return false
        }
        val pointCursor = db.query(
            "puntos",
            arrayOf("critico_bajo", "normal_bajo", "normal_alto", "critico_alto"),
            "id = ?",
            arrayOf(puntoId.toString()),
            null,
            null,
            null
        )
        val range = pointCursor.use { result ->
            if (!result.moveToFirst()) return false
            RangoCloro(result.getDouble(0), result.getDouble(1), result.getDouble(2), result.getDouble(3))
        }
        val currentTime = now()
        val status = calcularEstado(nivel, range)
        db.beginTransaction()
        try {
            val measurementId = db.insertOrThrow(
                "mediciones",
                null,
                ContentValues().apply {
                    put("punto_id", puntoId)
                    put("nivel", nivel)
                    put("fecha_hora", currentTime)
                    put("estado", status.name)
                    put("direccion", calcularDireccion(nivel, range)?.name)
                    put("sincronizada", 0)
                    if (usuarioId == null) putNull("usuario_id") else put("usuario_id", usuarioId)
                }
            )
            db.update(
                "puntos",
                ContentValues().apply {
                    put("nivel", nivel)
                    put("actualizado", currentTime)
                },
                "id = ?",
                arrayOf(puntoId.toString())
            )
            if (status != EstadoCloro.NORMAL) {
                val openAlertId = db.query(
                    "alertas",
                    arrayOf("id"),
                    "punto_id = ? AND atendida = 0",
                    arrayOf(puntoId.toString()),
                    null,
                    null,
                    null,
                    "1"
                ).use { if (it.moveToFirst()) it.getLong(0) else null }
                val latestAlert = ContentValues().apply {
                    put("medicion_id", measurementId)
                    put("nivel", nivel)
                    put("fecha_hora", currentTime)
                    put("estado", status.name)
                    put("direccion", calcularDireccion(nivel, range)!!.name)
                }
                if (openAlertId == null) {
                    db.insertOrThrow(
                        "alertas",
                        null,
                        latestAlert.apply { put("punto_id", puntoId) }
                    )
                } else {
                    db.update("alertas", latestAlert, "id = ?", arrayOf(openAlertId.toString()))
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        cambios.tryEmit(Unit)
        return true
    }

    fun registrarAccion(
        alertaId: Long,
        descripcion: String,
        responsable: String,
        usuarioId: Long,
        granjasPermitidas: List<Int>
    ): Boolean {
        if (descripcion.isBlank() || responsable.isBlank()) return false
        if (!alertaPerteneceAGranja(alertaId, granjasPermitidas, usuarioId)) return false
        val db = writableDatabase
        db.beginTransaction()
        val result: Long
        try {
            val alertMeasurementId = db.rawQuery(
                "SELECT medicion_id FROM alertas WHERE id = ? AND atendida = 0",
                arrayOf(alertaId.toString())
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else return false
            }
            result = db.insert(
                "acciones",
                null,
                ContentValues().apply {
                    put("alerta_id", alertaId)
                    put("medicion_id", alertMeasurementId)
                    put("descripcion", descripcion.trim())
                    put("responsable", responsable.trim())
                    put("fecha_hora", now())
                }
            )
            if (result != -1L) db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        if (result == -1L) return false
        cambios.tryEmit(Unit)
        return true
    }

    fun cerrarAlerta(alertaId: Long, usuarioId: Long): Boolean {
        val db = writableDatabase
        val roleCursor = db.query(
            "usuarios",
            arrayOf("rol"),
            "id = ?",
            arrayOf(usuarioId.toString()),
            null,
            null,
            null
        )
        val role = roleCursor.use { cursor ->
            if (!cursor.moveToFirst()) return false
            RolUsuario.valueOf(cursor.getString(0))
        }
        if (role != RolUsuario.SUPERVISOR && role != RolUsuario.JEFATURA) return false
        if (!alertaPerteneceAGranja(alertaId, null, usuarioId)) return false

        val alertCursor = db.rawQuery(
            """SELECT a.punto_id,
                (SELECT estado FROM mediciones WHERE punto_id = a.punto_id ORDER BY fecha_hora DESC, id DESC LIMIT 1),
                EXISTS(SELECT 1 FROM acciones WHERE alerta_id = a.id)
                FROM alertas a WHERE a.id = ? AND a.atendida = 0""",
            arrayOf(alertaId.toString())
        )
        val canClose = alertCursor.use { cursor ->
            cursor.moveToFirst() &&
                cursor.getString(1) == EstadoCloro.NORMAL.name &&
                cursor.getInt(2) == 1
        }
        if (!canClose) return false
        val verified = db.rawQuery(
            "SELECT id FROM mediciones WHERE punto_id = (SELECT punto_id FROM alertas WHERE id = ?) ORDER BY fecha_hora DESC, id DESC LIMIT 1",
            arrayOf(alertaId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLong(0) else return false
        }
        db.beginTransaction()
        val updated: Int
        try {
            updated = db.update(
                "alertas",
                ContentValues().apply {
                    put("atendida", 1)
                    put("cerrada_por", usuarioId.toString())
                    put("medicion_verificada_id", verified)
                },
                "id = ? AND atendida = 0",
                arrayOf(alertaId.toString())
            )
            if (updated > 0) {
                db.update(
                    "acciones",
                    ContentValues().apply { put("estado", "CERRADA") },
                    "alerta_id = ?",
                    arrayOf(alertaId.toString())
                )
                db.setTransactionSuccessful()
            }
        } finally {
            db.endTransaction()
        }
        if (updated > 0) cambios.tryEmit(Unit)
        return updated > 0
    }

    fun contarPendientesSincronizacion(granjasPermitidas: List<Int>): Int {
        if (granjasPermitidas.isEmpty()) return 0
        val cursor = readableDatabase.rawQuery(
            """SELECT COUNT(*) FROM mediciones m JOIN puntos p ON p.id = m.punto_id
                WHERE m.sincronizada = 0 AND p.granja_id IN (${granjasPermitidas.joinToString(",") { "?" }})""",
            granjasPermitidas.map(Int::toString).toTypedArray()
        )
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun simularMediciones() {
        val ids = readableDatabase.rawQuery("SELECT id, nivel FROM puntos ORDER BY id", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getInt(0) to cursor.getDouble(1))
            }
        }
        ids.forEach { (id, current) ->
            val next = (current + kotlin.random.Random.nextDouble(-0.45, 0.45)).coerceIn(0.0, MAX_NIVEL)
            registrarMedicion(id, next, null)
        }
    }

    private fun sembrarDatos(db: SQLiteDatabase) {
        listOf("Granja Los Aromos", "Granja El Roble", "Granja Santa Rosa").forEachIndexed { index, name ->
            db.insertOrThrow(
                "granjas",
                null,
                ContentValues().apply {
                    put("id", index + 1)
                    put("nombre", name)
                }
            )
        }
        val seed = listOf(
            Triple(1, "Galpón 1 - Bebederos", 4.12), Triple(1, "Galpón 2 - Bebederos", 2.71),
            Triple(1, "Estanque principal", 6.24), Triple(1, "Sala de proceso", 3.86),
            Triple(2, "Galpón 1 - Bebederos", 4.83), Triple(2, "Galpón 2 - Bebederos", 5.54),
            Triple(2, "Estanque principal", 1.76), Triple(2, "Sala de proceso", 3.28),
            Triple(3, "Galpón 1 - Bebederos", 5.23), Triple(3, "Galpón 2 - Bebederos", 2.84),
            Triple(3, "Estanque principal", 6.11), Triple(3, "Sala de proceso", 4.37)
        )
        val start = LocalDateTime.now().minusMinutes(20)
        seed.forEachIndexed { index, (farmId, name, current) ->
            val pointId = index + 1
            db.insertOrThrow(
                "puntos",
                null,
                ContentValues().apply {
                    put("id", pointId)
                    put("granja_id", farmId)
                    put("nombre", name)
                    put("codigo", "P-%02d".format(pointId))
                    put("galpon", name.substringBefore(" - "))
                    put("tipo", if (name.contains("Estanque")) "Estanque" else "Bebedero")
                    put("nivel", current)
                    put("actualizado", start.plusMinutes(20).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                }
            )
            listOf(-0.18, 0.09, -0.07, 0.14, 0.0).forEachIndexed { sample, delta ->
                val timestamp = start.plusMinutes(sample * 5L).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val value = (current + delta).coerceAtLeast(0.0)
                val state = calcularEstado(value)
                val measurementId = db.insertOrThrow(
                    "mediciones",
                    null,
                    ContentValues().apply {
                        put("punto_id", pointId)
                        put("nivel", value)
                        put("fecha_hora", timestamp)
                        put("estado", state.name)
                        put("direccion", calcularDireccion(value)?.name)
                        put("sincronizada", 1)
                    }
                )
                if (sample == 4 && state != EstadoCloro.NORMAL) {
                    db.insertOrThrow(
                        "alertas",
                        null,
                        ContentValues().apply {
                            put("punto_id", pointId)
                            put("medicion_id", measurementId)
                            put("nivel", value)
                            put("fecha_hora", timestamp)
                            put("estado", state.name)
                            put("direccion", calcularDireccion(value)!!.name)
                        }
                    )
                }
            }
        }
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BITS)
        return try {
            Base64.encodeToString(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,
                Base64.NO_WRAP
            )
        } finally {
            spec.clearPassword()
        }
    }

    private fun constantTimeEquals(first: String, second: String): Boolean {
        val a = first.toByteArray(Charsets.UTF_8)
        val b = second.toByteArray(Charsets.UTF_8)
        var result = a.size xor b.size
        for (index in 0 until maxOf(a.size, b.size)) {
            result = result or ((a.getOrElse(index) { 0 }.toInt()) xor (b.getOrElse(index) { 0 }.toInt()))
        }
        return result == 0
    }

    private fun now(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    private fun alertaPerteneceAGranja(
        alertaId: Long,
        granjasPermitidas: List<Int>?,
        usuarioId: Long
    ): Boolean {
        val farmId = readableDatabase.rawQuery(
            "SELECT p.granja_id FROM alertas a JOIN puntos p ON p.id = a.punto_id WHERE a.id = ?",
            arrayOf(alertaId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else return false
        }
        if (granjasPermitidas != null && farmId !in granjasPermitidas) return false
        return readableDatabase.rawQuery(
            "SELECT EXISTS(SELECT 1 FROM usuario_granjas WHERE usuario_id = ? AND granja_id = ?)",
            arrayOf(usuarioId.toString(), farmId.toString())
        ).use { it.moveToFirst() && it.getInt(0) == 1 }
    }

    companion object {
        const val DATABASE_NAME = "medicion_cloro.db"
        const val DATABASE_VERSION = 1
        const val MAX_NIVEL = 20.0
        const val SALT_BYTES = 16
        const val PBKDF2_ITERATIONS = 120_000
        const val HASH_BITS = 256
        @Volatile
        private var instance: CloroLocalDatabase? = null

        internal fun getInstance(context: Context): CloroLocalDatabase =
            instance ?: synchronized(this) {
                instance ?: CloroLocalDatabase(context).also { instance = it }
            }
    }
}

data class PuntoLocal(
    val id: Int,
    val granjaId: Int,
    val nombre: String,
    val codigo: String,
    val galpon: String,
    val tipo: String,
    val nivel: Double,
    val actualizado: String,
    val rango: RangoCloro
)
