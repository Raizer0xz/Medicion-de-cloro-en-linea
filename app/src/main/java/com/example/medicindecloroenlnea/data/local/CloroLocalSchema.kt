package com.example.medicindecloroenlnea.data.local

import android.database.sqlite.SQLiteDatabase

internal object CloroLocalSchema {
    fun create(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE granjas (
                id INTEGER NOT NULL PRIMARY KEY,
                nombre TEXT NOT NULL
            )"""
        )
        db.execSQL(
            """CREATE TABLE puntos (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
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
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
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
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
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
                estado TEXT NOT NULL DEFAULT 'ABIERTA',
                foto_uri TEXT
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
        db.execSQL("CREATE INDEX idx_puntos_granja_id ON puntos(granja_id)")
        db.execSQL("CREATE INDEX idx_alertas_punto_id ON alertas(punto_id)")
        db.execSQL("CREATE INDEX idx_alertas_medicion_id ON alertas(medicion_id)")
        db.execSQL("CREATE INDEX idx_alertas_medicion_verificada_id ON alertas(medicion_verificada_id)")
    }
}
