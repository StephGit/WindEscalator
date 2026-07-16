package ch.stephgit.windescalator.wind

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.stephgit.windescalator.alert.detail.WindResource
import ch.stephgit.windescalator.data.WindResourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class WindViewModel @Inject constructor(
    private val windResourceRepo: WindResourceRepository
) : ViewModel() {

    private val _windResources = MutableStateFlow<List<WindResource>>(emptyList())

    val windResources: StateFlow<List<WindResource>>
        get() = _windResources.asStateFlow()

    init {
        viewModelScope.launch {
            windResourceRepo.getWindResources().collect {
                _windResources.value = it
            }
        }
    }

    fun refreshResource(resource: WindResource) {
        if (resource.url.isBlank()) return
        viewModelScope.launch {
            val refreshedResource = windResourceRepo.refreshWindResource(resource)
            _windResources.value = _windResources.value.map {
                if (it.id == resource.id) refreshedResource else it
            }
        }
    }
}
