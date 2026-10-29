package com.example.medicindecloroenlnea.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.medicindecloroenlnea.data.local.CloroSchemaMigration
import com.example.medicindecloroenlnea.data.local.CloroLocalDatabase

@Database(
    entities = [
        GranjaRoomEntity::class,
        PuntoRoomEntity::class,
        MedicionRoomEntity::class,
        AlertaRoomEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CloroRoomDatabase : RoomDatabase() {
    abstract fun puntoDao(): PuntoRoomDao

    companion object {
        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                CloroSchemaMigration.upgradeFromVersionOne { db.execSQL(it) }
            }
        }

        @Volatile
        private var instance: CloroRoomDatabase? = null

        fun getInstance(context: Context): CloroRoomDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CloroRoomDatabase::class.java,
                    CloroLocalDatabase.DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
