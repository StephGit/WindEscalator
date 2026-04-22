package ch.stephgit.windescalator.wind

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.detail.WindResource
import ch.stephgit.windescalator.alert.detail.extractWindData
import ch.stephgit.windescalator.data.WindResourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
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

    fun refreshResource(resource: WindResource) {
        if (resource.url.isBlank()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val data = Jsoup.connect(resource.url)
                        .ignoreContentType(true)
                        .execute()
                        .body()
                    val windData = extractWindData(data, resource.localId)
                    val updated = resource.copy(
                        latestForce = windData.force,
                        latestDirection = windData.direction,
                        latestTime = windData.time,
                        online = windData.force > 0 && windData.direction.isNotEmpty(),
                        lastChecked = System.currentTimeMillis()
                    )
                    _windResources.value = _windResources.value.map {
                        if (it.id == resource.id) updated else it
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to refresh resource ${resource.displayName}", e)
                }
            }
        }
    }
}
