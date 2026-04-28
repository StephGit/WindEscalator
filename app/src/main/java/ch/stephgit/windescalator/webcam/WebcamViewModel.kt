package ch.stephgit.windescalator.webcam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.stephgit.windescalator.data.WebcamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WebcamViewModel @Inject constructor(
    private val webcamRepo: WebcamRepository
) : ViewModel() {

    private val _webcams = MutableStateFlow<List<Webcam>>(emptyList())

    val webcams: StateFlow<List<Webcam>>
        get() = _webcams.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                webcamRepo.getWebcams().collect {
                    _webcams.value = it
                }
            }
        }
    }
}
