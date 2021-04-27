package ch.stephgit.windescalator.alert.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimeViewModel : ViewModel() {
    val time = MutableLiveData<String>()

    fun sendTime(selectedTime: String) {
        time.value = selectedTime
    }
}