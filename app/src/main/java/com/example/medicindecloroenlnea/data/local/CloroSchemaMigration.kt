package com.example.medicindecloroenlnea.data.local

internal object CloroSchemaMigration {
    fun upgradeFromVersionOne(executeSql: (String) -> Unit) {
        executeSql("PRAGMA defer_foreign_keys = ON")
        executeSql(
            """CREATE TABLE _room_v2_granjas (
                id INTEGER NOT NULL,
                nombre TEXT NOT NULL,
                PRIMARY KEY(id)
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
                critico_alto REAL NOT NULL DEFAULT 6,
                FOREIGN KEY(granja_id) REFERENCES _room_v2_granjas(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
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
                usuario_id INTEGER,
                FOREIGN KEY(punto_id) REFERENCES _room_v2_puntos(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
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
                medicion_verificada_id INTEGER,
                FOREIGN KEY(punto_id) REFERENCES _room_v2_puntos(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(medicion_id) REFERENCES _room_v2_mediciones(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION,
                FOREIGN KEY(medicion_verificada_id) REFERENCES _room_v2_mediciones(id)
                    ON UPDATE NO ACTION ON DELETE NO ACTION
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

        executeSql("DROP TABLE alertas")
        executeSql("DROP TABLE mediciones")
        executeSql("DROP TABLE puntos")
        executeSql("DROP TABLE granjas")
        executeSql("ALTER TABLE _room_v2_granjas RENAME TO granjas")
        executeSql("ALTER TABLE _room_v2_puntos RENAME TO puntos")
        executeSql("ALTER TABLE _room_v2_mediciones RENAME TO mediciones")
        executeSql("ALTER TABLE _room_v2_alertas RENAME TO alertas")

        executeSql("CREATE INDEX idx_mediciones_punto_fecha ON mediciones(punto_id, fecha_hora DESC)")
        executeSql("CREATE INDEX idx_alertas_estado_fecha ON alertas(atendida, fecha_hora DESC)")
        executeSql("CREATE INDEX idx_puntos_granja_id ON puntos(granja_id)")
        executeSql("CREATE INDEX idx_alertas_punto_id ON alertas(punto_id)")
        executeSql("CREATE INDEX idx_alertas_medicion_id ON alertas(medicion_id)")
        executeSql("CREATE INDEX idx_alertas_medicion_verificada_id ON alertas(medicion_verificada_id)")
    }
}
