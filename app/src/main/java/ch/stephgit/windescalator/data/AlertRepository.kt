package ch.stephgit.windescalator.data

import android.util.Log
import ch.stephgit.windescalator.TAG
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

private const val COLLECTION_NAME = "alert"
class AlertRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val collectionReference: CollectionReference = db.collection(COLLECTION_NAME)

    private val user get() = FirebaseAuth.getInstance().currentUser

    fun getAlerts(): Flow<List<Alert>> = callbackFlow {
        val currentUser = user
        if (currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : EventListener<QuerySnapshot> {
            override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                if (exception != null) {
                    this@callbackFlow.close(exception)
                    return
                }

                val alerts = snapshot?.mapNotNull { document ->
                    Log.d(TAG, "${document.id} => ${document.data}")
                    document.toObject<Alert>()?.apply { this.id = document.id }
                } ?: emptyList()
                trySend(alerts)
            }
        }

        val registration =
            collectionReference.whereEqualTo("userId", currentUser.uid).addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }

    fun get(id: String): Flow<Alert> = callbackFlow {
        val listener = object : EventListener<DocumentSnapshot> {
            override fun onEvent(
                document: DocumentSnapshot?,
                exception: FirebaseFirestoreException?
            ) {
                if (exception != null) {
                    this@callbackFlow.close(exception)
                    return
                }

                if (document != null) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    document.toObject<Alert>()?.let { alert ->
                        alert.id = id
                        trySend(alert)
                    } ?: Log.e(TAG, "Reading alert `$id` from db failed: data is null")
                } else {
                    Log.e(TAG, "Reading alert `$id` from db failed")
                }
            }
        }
        // TODO create interface for boilerplate
        val registration = collectionReference.document(id).addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }

    fun create(alert: Alert): Task<DocumentReference> {
        return collectionReference.add(alert)
            .addOnSuccessListener {
                alert.id = it.id
            }
            .addOnFailureListener { e ->
                Log.e(
                    TAG,
                    "There was an error creating '${alert.name}' in '$COLLECTION_NAME'!", e
                )
            }
    }

    fun update(alert: Alert): Task<Void> {
        val documentName: String = alert.id
        val documentReference = collectionReference.document(documentName)
        Log.i(
            TAG,
            "Updating '$documentName' in '$COLLECTION_NAME'."
        )
        return documentReference.set(alert).addOnFailureListener { e ->
            Log.e(
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
            Log.e(
                TAG,
                "There was an error deleting '$id' in '$COLLECTION_NAME'.", e
            )
        }
    }

    // generic extension
    // collectionReference.document(someId).addaddSnapshotListenerFlow(Some::class.java)
    fun <T> DocumentReference.addSnapshotListenerFlow(dataType: Class<T>): Flow<T?> = callbackFlow {
        val listener = object : EventListener<DocumentSnapshot> {
            override fun onEvent(
                snapshot: DocumentSnapshot?,
                exception: FirebaseFirestoreException?
            ) {
                if (exception != null) {
                    this@callbackFlow.close(exception)
                    return
                }

                if (snapshot != null && snapshot.exists()) {
                    // The document has data
                    val data = snapshot.toObject(dataType)
                    trySend(data)
                } else {
                    trySend(null)
                }
            }
        }

        val registration = addSnapshotListener(listener)
        awaitClose { registration.remove() }
    }

}
