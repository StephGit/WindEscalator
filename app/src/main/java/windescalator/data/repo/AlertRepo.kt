package windescalator.data.repo

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import windescalator.data.dao.AlertDao
import windescalator.data.entity.Alert
import javax.inject.Inject

class AlertRepo @Inject constructor(private var alertDao: AlertDao) {

    var alerts: LiveData<List<Alert>> = alertDao.getAlerts()

    fun hasAlert(): Boolean {
        return GetAsyncTask(alertDao).execute().get()
    }

    fun getActiveAlerts(): List<Alert> {
        return GetActiveAsyncTask(alertDao).execute().get()
    }

    fun getAlert(alertId: Long): Alert? {
        return GetByIdAsyncTask(alertDao).execute(alertId).get()
    }

    fun insert(alert: Alert) {
        InsertAsyncTask(alertDao).execute(alert)
    }

    fun update(alert: Alert) {
        UpdateAsyncTask(alertDao).execute(alert)
    }

    fun delete(alert: Alert) {
        DeleteAsyncTask(alertDao).execute(alert)
    }

    private class InsertAsyncTask internal constructor(private val alertDao: AlertDao) :
            AsyncTask<Alert, Void, Void>() {
        override fun doInBackground(vararg params: Alert): Void? {
            alertDao.insert(params[0])
            return null
        }
    }

    private class GetAsyncTask internal constructor(private val alertDao: AlertDao) :
            AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void): Boolean {
            return (alertDao.getAnyAlert() != null)
        }
    }

    private class GetActiveAsyncTask internal constructor(private val alertDao: AlertDao) :
            AsyncTask<Void, Void, List<Alert>>() {
        override fun doInBackground(vararg params: Void): List<Alert> {
            return (alertDao.getActiveAlerts())
        }
    }

    private class GetByIdAsyncTask internal constructor(private val alertDao: AlertDao) :
            AsyncTask<Long, Void, Alert?>() {
        override fun doInBackground(vararg params: Long?): Alert? {
            return (alertDao.getAlertById(params[0]!!))
        }
    }

    private class UpdateAsyncTask internal constructor(private val alertDao: AlertDao) :
            AsyncTask<Alert, Void, Void>() {
        override fun doInBackground(vararg params: Alert): Void? {
            alertDao.update(params[0])
            return null
        }
    }

    private class DeleteAsyncTask internal constructor(private val alertDao: AlertDao) :
            AsyncTask<Alert, Void, Void>() {
        override fun doInBackground(vararg params: Alert): Void? {
            alertDao.delete(params[0])
            return null
        }
    }
}