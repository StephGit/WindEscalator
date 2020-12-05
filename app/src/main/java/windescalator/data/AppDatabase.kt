package windescalator.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import windescalator.data.dao.AlertDao
import windescalator.data.entity.Alert

@Database(entities = [Alert::class], version = 1)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun alertDao(): AlertDao
}