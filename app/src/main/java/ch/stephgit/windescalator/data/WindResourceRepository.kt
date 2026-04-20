package ch.stephgit.windescalator.data
 
import android.util.Log
import ch.stephgit.windescalator.TAG
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
}
