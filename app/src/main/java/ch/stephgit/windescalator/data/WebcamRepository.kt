package ch.stephgit.windescalator.data

import android.util.Log
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.webcam.Webcam
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class WebcamRepository @Inject constructor(db: FirebaseFirestore) {

    private val collectionReference: CollectionReference = db.collection("webcam")

    fun getWebcams(): Flow<List<Webcam>> = callbackFlow {
        val listener = object : EventListener<QuerySnapshot> {
            override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                if (exception != null) {
                    Log.e(TAG, "Error listening to webcam collection", exception)
                    cancel()
                    return
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val webcams = mutableListOf<Webcam>()
                    for (document in snapshot) {
                        val webcam = document.toObject<Webcam>()
                        webcam.id = document.id
                        webcams.add(webcam)
                    }
                    trySend(webcams.sortedBy { it.displayName })
                } else {
                    trySend(emptyList())
                }
            }
        }

        val registration = collectionReference.addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }
}
