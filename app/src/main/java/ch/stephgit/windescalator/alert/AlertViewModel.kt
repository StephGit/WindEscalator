package ch.stephgit.windescalator.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.stephgit.windescalator.data.Alert
import ch.stephgit.windescalator.data.AlertRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlertViewModel @Inject constructor(var alertRepo: AlertRepository) : ViewModel() {

    private val _alerts = MutableStateFlow<List<Alert>?>(emptyList())

    val alerts: StateFlow<List<Alert>?>
        get() = _alerts.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                alertRepo.getAlerts().collect {
                    _alerts.value = it
                }
            }
        }
    }

    fun insert(alert: Alert) {
       alertRepo.create(alert)
    }

    fun delete(alert: Alert) {
        alertRepo.delete(alert.id)
    }

    fun update(alert: Alert) {
        alertRepo.update(alert)
    }

}