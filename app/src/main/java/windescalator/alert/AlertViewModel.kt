package windescalator.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import javax.inject.Inject

class AlertViewModel @Inject constructor(var alertRepo: AlertRepo) : ViewModel() {

    val alertItems: LiveData<List<Alert>> = alertRepo.alerts

    fun insert(alert: Alert) {
        alertRepo.insert(alert)
    }

    fun delete(alert: Alert) {
        alertRepo.delete(alert)
    }

    fun update(alert: Alert) {
        alertRepo.update(alert)
    }

}