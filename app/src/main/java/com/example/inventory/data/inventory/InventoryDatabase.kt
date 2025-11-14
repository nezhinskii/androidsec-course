package com.example.inventory.data.inventory

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.commonsware.cwac.saferoom.SQLCipherUtils
import com.example.inventory.data.settings.EncryptedPrefsManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory

@Database(entities = [Item::class], version = 3, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN supplierName TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN supplierEmail TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN supplierPhone TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE items ADD COLUMN source TEXT NOT NULL DEFAULT 'MANUAL'")
            }
        }

        private const val DB_NAME = "item_database"

        @Volatile
        private var Instance: InventoryDatabase? = null

        private fun migrateIfNeeded(context: Context, encryptedPrefs: EncryptedPrefsManager) {
            val state = SQLCipherUtils.getDatabaseState(context, DB_NAME)

            if (state == SQLCipherUtils.State.UNENCRYPTED) {
                val originalFile = context.getDatabasePath(DB_NAME)
                val tempFile = context.getDatabasePath("temp_encrypted.db")
                originalFile.copyTo(tempFile, overwrite = true)

                val password = encryptedPrefs.getOrCreateSqlCipherPassphrase()

                try {
                    SQLCipherUtils.encrypt(context, "temp_encrypted.db", password)
                    tempFile.renameTo(originalFile)
                } catch (e: Exception) {
                    tempFile.delete()
                    throw e
                }
            }
        }

        fun getDatabase(context: Context, encryptedPrefs: EncryptedPrefsManager): InventoryDatabase {
            return Instance ?: synchronized(this) {
                migrateIfNeeded(context, encryptedPrefs)

                val hook = object : SQLiteDatabaseHook {
                    override fun preKey(database: SQLiteDatabase) {
                        database.rawExecSQL("PRAGMA cipher_memory_security = OFF")
                    }
                    override fun postKey(database: SQLiteDatabase) {
                    }
                }

                val password = encryptedPrefs.getOrCreateSqlCipherPassphrase()

                val factory = SupportFactory(password, hook)

                Room.databaseBuilder(
                    context,
                    InventoryDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .openHelperFactory(factory)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}