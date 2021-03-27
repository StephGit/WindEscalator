package ch.stephgit.windescalator.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.stephgit.windescalator.data.dao.AlertDao
import ch.stephgit.windescalator.data.entity.Alert

@Database(entities = [Alert::class], version = 1)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun alertDao(): AlertDao
}