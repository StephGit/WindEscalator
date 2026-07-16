package ch.stephgit.windescalator.data

import android.util.Log
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.detail.WindResource
import ch.stephgit.windescalator.alert.detail.extractWindData
import ch.stephgit.windescalator.alert.detail.isWindDataFresh
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

class WindResourceRepository @Inject constructor(db: FirebaseFirestore) {

    private val collectionReference: CollectionReference = db.collection("windResource")

    fun getResourceAvailability(): Flow<Map<Int, Boolean>> = callbackFlow {
        val listener = object : EventListener<QuerySnapshot> {
            override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                if (exception != null) {
                    cancel()
                    return
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val availabilityMap = mutableMapOf<Int, Boolean>()
                    for (document in snapshot) {
                        val localId = document.getLong("localId")?.toInt() ?: continue
                        val dataAvailable = document.getBoolean("online") ?: false
                        availabilityMap[localId] = dataAvailable
                    }
                    trySend(availabilityMap)
                }
            }
        }

        val registration = collectionReference.addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }

    fun getWindResources(): Flow<List<WindResource>> = callbackFlow {
        val listener = object : EventListener<QuerySnapshot> {
            override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                if (exception != null) {
                    Log.e(TAG, "Error listening to windResource collection", exception)
                    cancel()
                    return
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val resources = mutableListOf<WindResource>()
                    for (document in snapshot) {
                        val resource = document.toObject<WindResource>()
                        resource.id = document.id
                        resources.add(resource)
                    }
                    trySend(resources.sortedBy { it.localId })
                } else {
                    trySend(emptyList())
                }
            }
        }

        val registration = collectionReference.addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }

    suspend fun refreshWindResource(resource: WindResource): WindResource = withContext(Dispatchers.IO) {
        try {
            val data = Jsoup.connect(resource.url)
                .ignoreContentType(true)
                .execute()
                .body()
            val windData = extractWindData(data, resource.localId)
            resource.copy(
                latestForce = windData.force,
                latestGust = windData.gust,
                latestDirection = windData.direction,
                latestTime = windData.time,
                online = isWindDataFresh(windData.time),
                lastChecked = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh resource ${resource.displayName}", e)
            resource // Return original on failure
        }
    }
}
