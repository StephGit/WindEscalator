package ch.stephgit.windescalator.data.repo


import android.util.Log
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.data.FbAlert
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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

private const val COLLECTION_NAME = "alert"

class AlertRepository @Inject constructor(var db: FirebaseFirestore) {

    private val collectionReference: CollectionReference = db.collection(COLLECTION_NAME)

    private val user = Firebase.auth.currentUser!!

    fun getAlerts(): Flow<List<FbAlert>> = callbackFlow {
        val listener = object : EventListener<QuerySnapshot> {
            override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                if (exception != null) {
                    // An error occurred
                    cancel()
                    return
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    var tmpList = ArrayList<FbAlert>();
                    for (document in snapshot) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        var alert = document.toObject(FbAlert::class.java)
                        alert.id = document.id
                        tmpList.add(alert)
                    }
                    trySend(tmpList)
                } else {
                    // The alert document does not exist or has no data
                }
            }
        }

        val registration = collectionReference.whereEqualTo("userId", user.uid).addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }

    fun get(id: String, callback: (fbAlert: FbAlert) -> Unit) {
        val documentReference = collectionReference.document(id)
        Log.i(
            TAG,
            "Getting '$id' in '$COLLECTION_NAME'."
        )
        documentReference.get()
            .addOnSuccessListener { snapshot ->
                Log.d(TAG, snapshot.toString())
                snapshot.toObject<FbAlert>()?.let { it: FbAlert ->
                    it.id = id
                    callback(it)
                }
            }
            .addOnFailureListener { failure ->
                Log.d(TAG, failure.stackTrace.toString())
                Log.d(
                    TAG,
                    "Reading alert `$id` from db failed: $failure.toString()"
                )
            }
    }

    fun create(alert: FbAlert) {
        val documentReference = collectionReference.add(alert)
            .addOnSuccessListener {
                alert.id = it.id
            }
            .addOnFailureListener { e ->
            Log.d(
                TAG,
                "There was an error creating '${alert.name}' in '$COLLECTION_NAME'!", e
            )
        }
    }

    fun update(alert: FbAlert): Task<Void> {
        val documentName: String = alert.id
        val documentReference = collectionReference.document(documentName)
        Log.i(
            TAG,
            "Updating '$documentName' in '$COLLECTION_NAME'."
        )
        return documentReference.set(alert).addOnFailureListener { e ->
            Log.d(
                TAG,
                "There was an error updating '$documentName' in '$COLLECTION_NAME'.", e
            )
        }
    }

    fun delete(id: String): Task<Void> {
        val documentReference = collectionReference.document(id)
        Log.i(
            TAG,
            "Deleting '$id' in '$COLLECTION_NAME'."
        )
        return documentReference.delete().addOnFailureListener { e ->
            Log.d(
                TAG,
                "There was an error deleting '$id' in '$COLLECTION_NAME'.", e
            )
        }
    }

    // generic extension
    // collectionReference.document(someId).addaddSnapshotListenerFlow(Some::class.java)
    fun <T> DocumentReference.addSnapshotListenerFlow(dataType: Class<T>): Flow<T?> = callbackFlow {
        val listener = object : EventListener<DocumentSnapshot> {
            override fun onEvent(snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
                if (exception != null) {
                    // An error occurred
                    cancel()
                    return
                }

                if (snapshot != null && snapshot.exists()) {
                    // The document has data
                    val data = snapshot.toObject(dataType)
                    trySend(data)
                } else {
                    // The document does not exist or has no data
                }
            }
        }

        val registration = addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }

}