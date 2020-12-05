package windescalator.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import windescalator.data.entity.Alert

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert LIMIT 1")
    fun getAnyAlert(): Alert?

    @Query("SELECT * FROM alert where active = 1")
    fun getActiveAlerts(): List<Alert>

    @Query("SELECT * FROM alert")
    fun getAlerts(): LiveData<List<Alert>>

    @Query("SELECT * FROM alert WHERE id == :alertId")
    fun getAlertById(alertId: Long): Alert?

    @Insert
    fun insert(alert: Alert): Long

    @Update
    fun update(alert: Alert)

    @Delete
    fun delete(alert: Alert)
}