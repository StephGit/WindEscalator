package ch.stephgit.windescalator.wind

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.stephgit.windescalator.alert.detail.WindResource
import ch.stephgit.windescalator.data.WindResourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WindViewModel @Inject constructor(
    private val windResourceRepo: WindResourceRepository
) : ViewModel() {

    private val _windResources = MutableStateFlow<List<WindResource>>(emptyList())

    val windResources: StateFlow<List<WindResource>>
        get() = _windResources.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                windResourceRepo.getWindResources().collect {
                    _windResources.value = it
                }
            }
        }
    }
}
