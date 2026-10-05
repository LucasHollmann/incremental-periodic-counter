package com.lucashollmann.incrementalperiodiccounter.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Counter::class, CounterHistoryEntry::class], version = 8, exportSchema = false)
abstract class CounterDatabase : RoomDatabase() {
    abstract fun counterDao(): CounterDao

    companion object {
        @Volatile
        private var instance: CounterDatabase? = null

        fun getInstance(context: Context): CounterDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CounterDatabase::class.java,
                    "counters.db",
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                    )
                    .build()
                    .also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE counters ADD COLUMN normal_increment INTEGER NOT NULL DEFAULT 1",
                )
                db.execSQL(
                    "ALTER TABLE counters ADD COLUMN secondary_increment INTEGER NOT NULL DEFAULT 2",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE counters ADD COLUMN allow_negative INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE counters_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        value INTEGER NOT NULL,
                        normal_increment INTEGER NOT NULL DEFAULT 1,
                        secondary_increment INTEGER,
                        allow_negative INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO counters_new (
                        id, name, value, normal_increment, secondary_increment, allow_negative
                    )
                    SELECT
                        id,
                        name,
                        value,
                        normal_increment,
                        CASE WHEN secondary_increment = 2 THEN NULL ELSE secondary_increment END,
                        allow_negative
                    FROM counters
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE counters")
                db.execSQL("ALTER TABLE counters_new RENAME TO counters")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE counters ADD COLUMN notification_times TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE counters
                    ADD COLUMN notification_days TEXT NOT NULL DEFAULT '1,2,3,4,5,6,7'
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE counters ADD COLUMN notification_schedules TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE counter_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        counter_id INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        new_value INTEGER NOT NULL,
                        step INTEGER NOT NULL,
                        FOREIGN KEY(counter_id) REFERENCES counters(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX index_counter_history_counter_id_timestamp ON counter_history(counter_id, timestamp)",
                )
            }
        }
    }
}
