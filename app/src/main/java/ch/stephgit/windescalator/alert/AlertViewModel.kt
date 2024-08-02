package ch.stephgit.windescalator.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ch.stephgit.windescalator.data.FbAlert
import ch.stephgit.windescalator.data.repo.AlertRepository
import javax.inject.Inject

class AlertViewModel @Inject constructor(var alertRepo: AlertRepository) : ViewModel() {

    val alertItems: LiveData<List<FbAlert>> = alertRepo.getFbAlerts()

    fun insert(alert: FbAlert) {
        //TODO Firebase Alert
//        alertRepo.insert(alert)
    }

    fun delete(alert: FbAlert) {
        //TODO Firebase Alert
//        alertRepo.delete(alert)
    }

    fun update(alert: FbAlert) {
        //TODO Firebase Alert
//        alertRepo.update(alert)
    }

}