package ch.stephgit.windescalator.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.stephgit.windescalator.data.dao.AlertDao
import ch.stephgit.windescalator.data.entity.Alert

@Database(entities = [Alert::class], version = 2, exportSchema = false)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun alertDao(): AlertDao
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Alert ADD COLUMN nextRun INTEGER")
            }
        }
    }

}

