package com.example.medicindecloroenlnea.data.local

internal object CloroSchemaMigration {
    fun upgradeFromVersionOne(executeSql: (String) -> Unit) {
        executeSql(
            """CREATE TABLE _room_v2_granjas (
                id INTEGER NOT NULL PRIMARY KEY,
                nombre TEXT NOT NULL
            )"""
        )
        executeSql(
            """CREATE TABLE _room_v2_puntos (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                granja_id INTEGER NOT NULL,
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
        executeSql(
            """CREATE TABLE _room_v2_mediciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                punto_id INTEGER NOT NULL,
                nivel REAL NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL,
                direccion TEXT,
                sincronizada INTEGER NOT NULL DEFAULT 0,
                usuario_id INTEGER
            )"""
        )
        executeSql(
            """CREATE TABLE _room_v2_alertas (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                punto_id INTEGER NOT NULL,
                medicion_id INTEGER NOT NULL,
                nivel REAL NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL,
                direccion TEXT NOT NULL,
                atendida INTEGER NOT NULL DEFAULT 0,
                cerrada_por TEXT,
                medicion_verificada_id INTEGER
            )"""
        )
        executeSql(
            """CREATE TABLE _room_v2_acciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alerta_id INTEGER NOT NULL,
                medicion_id INTEGER NOT NULL,
                descripcion TEXT NOT NULL,
                responsable TEXT NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL DEFAULT 'ABIERTA'
            )"""
        )
        executeSql(
            """CREATE TABLE _room_v2_usuario_granjas (
                usuario_id INTEGER NOT NULL,
                granja_id INTEGER NOT NULL,
                PRIMARY KEY(usuario_id, granja_id)
            )"""
        )

        executeSql("INSERT INTO _room_v2_granjas SELECT id, nombre FROM granjas")
        executeSql(
            """INSERT INTO _room_v2_puntos
                (id, granja_id, nombre, codigo, galpon, tipo, nivel, actualizado,
                 critico_bajo, normal_bajo, normal_alto, critico_alto)
                SELECT id, granja_id, nombre, codigo, galpon, tipo, nivel, actualizado,
                       critico_bajo, normal_bajo, normal_alto, critico_alto
                FROM puntos"""
        )
        executeSql(
            """INSERT INTO _room_v2_mediciones
                (id, punto_id, nivel, fecha_hora, estado, direccion, sincronizada, usuario_id)
                SELECT id, punto_id, nivel, fecha_hora, estado, direccion, sincronizada, usuario_id
                FROM mediciones"""
        )
        executeSql(
            """INSERT INTO _room_v2_alertas
                (id, punto_id, medicion_id, nivel, fecha_hora, estado, direccion, atendida,
                 cerrada_por, medicion_verificada_id)
                SELECT id, punto_id, medicion_id, nivel, fecha_hora, estado, direccion, atendida,
                       cerrada_por, medicion_verificada_id
                FROM alertas"""
        )
        executeSql(
            """INSERT INTO _room_v2_acciones
                (id, alerta_id, medicion_id, descripcion, responsable, fecha_hora, estado)
                SELECT id, alerta_id, medicion_id, descripcion, responsable, fecha_hora, estado
                FROM acciones"""
        )
        executeSql(
            """INSERT INTO _room_v2_usuario_granjas (usuario_id, granja_id)
                SELECT usuario_id, granja_id FROM usuario_granjas"""
        )

        executeSql("DROP TABLE acciones")
        executeSql("DROP TABLE usuario_granjas")
        executeSql("DROP TABLE alertas")
        executeSql("DROP TABLE mediciones")
        executeSql("DROP TABLE puntos")
        executeSql("DROP TABLE granjas")

        executeSql(
            """CREATE TABLE granjas (
                id INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                PRIMARY KEY(id)
            )"""
        )
        executeSql(
            """CREATE TABLE puntos (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                granja_id INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                codigo TEXT NOT NULL,
                galpon TEXT NOT NULL,
                tipo TEXT NOT NULL,
                nivel REAL NOT NULL,
                actualizado TEXT NOT NULL,
                critico_bajo REAL NOT NULL DEFAULT 2,
                normal_bajo REAL NOT NULL DEFAULT 3,
                normal_alto REAL NOT NULL DEFAULT 5,
                critico_alto REAL NOT NULL DEFAULT 6,
                FOREIGN KEY(granja_id) REFERENCES granjas(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
            )"""
        )
        executeSql(
            """CREATE TABLE mediciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                punto_id INTEGER NOT NULL,
                nivel REAL NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL,
                direccion TEXT,
                sincronizada INTEGER NOT NULL DEFAULT 0,
                usuario_id INTEGER,
                FOREIGN KEY(punto_id) REFERENCES puntos(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
            )"""
        )
        executeSql(
            """CREATE TABLE alertas (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                punto_id INTEGER NOT NULL,
                medicion_id INTEGER NOT NULL,
                nivel REAL NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL,
                direccion TEXT NOT NULL,
                atendida INTEGER NOT NULL DEFAULT 0,
                cerrada_por TEXT,
                medicion_verificada_id INTEGER,
                FOREIGN KEY(punto_id) REFERENCES puntos(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(medicion_id) REFERENCES mediciones(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(medicion_verificada_id) REFERENCES mediciones(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
            )"""
        )
        executeSql(
            """CREATE TABLE acciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alerta_id INTEGER NOT NULL,
                medicion_id INTEGER NOT NULL,
                descripcion TEXT NOT NULL,
                responsable TEXT NOT NULL,
                fecha_hora TEXT NOT NULL,
                estado TEXT NOT NULL DEFAULT 'ABIERTA',
                FOREIGN KEY(alerta_id) REFERENCES alertas(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(medicion_id) REFERENCES mediciones(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
            )"""
        )
        executeSql(
            """CREATE TABLE usuario_granjas (
                usuario_id INTEGER NOT NULL,
                granja_id INTEGER NOT NULL,
                PRIMARY KEY(usuario_id, granja_id),
                FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
                    ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(granja_id) REFERENCES granjas(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
            )"""
        )

        executeSql("INSERT INTO granjas SELECT id, nombre FROM _room_v2_granjas")
        executeSql(
            """INSERT INTO puntos
                (id, granja_id, nombre, codigo, galpon, tipo, nivel, actualizado,
                 critico_bajo, normal_bajo, normal_alto, critico_alto)
                SELECT id, granja_id, nombre, codigo, galpon, tipo, nivel, actualizado,
                       critico_bajo, normal_bajo, normal_alto, critico_alto
                FROM _room_v2_puntos"""
        )
        executeSql(
            """INSERT INTO mediciones
                (id, punto_id, nivel, fecha_hora, estado, direccion, sincronizada, usuario_id)
                SELECT id, punto_id, nivel, fecha_hora, estado, direccion, sincronizada, usuario_id
                FROM _room_v2_mediciones"""
        )
        executeSql(
            """INSERT INTO alertas
                (id, punto_id, medicion_id, nivel, fecha_hora, estado, direccion, atendida,
                 cerrada_por, medicion_verificada_id)
                SELECT id, punto_id, medicion_id, nivel, fecha_hora, estado, direccion, atendida,
                       cerrada_por, medicion_verificada_id
                FROM _room_v2_alertas"""
        )
        executeSql(
            """INSERT INTO acciones
                (id, alerta_id, medicion_id, descripcion, responsable, fecha_hora, estado)
                SELECT id, alerta_id, medicion_id, descripcion, responsable, fecha_hora, estado
                FROM _room_v2_acciones"""
        )
        executeSql(
            """INSERT INTO usuario_granjas (usuario_id, granja_id)
                SELECT usuario_id, granja_id FROM _room_v2_usuario_granjas"""
        )

        executeSql("DROP TABLE _room_v2_usuario_granjas")
        executeSql("DROP TABLE _room_v2_acciones")
        executeSql("DROP TABLE _room_v2_alertas")
        executeSql("DROP TABLE _room_v2_mediciones")
        executeSql("DROP TABLE _room_v2_puntos")
        executeSql("DROP TABLE _room_v2_granjas")

        executeSql("CREATE INDEX idx_mediciones_punto_fecha ON mediciones(punto_id, fecha_hora DESC)")
        executeSql("CREATE INDEX idx_alertas_estado_fecha ON alertas(atendida, fecha_hora DESC)")
        executeSql("CREATE INDEX idx_puntos_granja_id ON puntos(granja_id)")
        executeSql("CREATE INDEX idx_alertas_punto_id ON alertas(punto_id)")
        executeSql("CREATE INDEX idx_alertas_medicion_id ON alertas(medicion_id)")
        executeSql("CREATE INDEX idx_alertas_medicion_verificada_id ON alertas(medicion_verificada_id)")
    }
}
