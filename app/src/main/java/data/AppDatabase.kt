package data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [], version = 1)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
}