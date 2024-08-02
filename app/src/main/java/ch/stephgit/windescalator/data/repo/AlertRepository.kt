package ch.stephgit.windescalator.data.repo


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.data.FbAlert
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import javax.inject.Inject

private const val collectionName = "alert"

class AlertRepository @Inject constructor(var db: FirebaseFirestore) {

    private val collectionReference: CollectionReference = db.collection(collectionName)

    var alerts: MutableLiveData<List<FbAlert>> = MutableLiveData()
    private val user = Firebase.auth.currentUser!!

    fun getFbAlerts(): LiveData<List<FbAlert>> {
        if (alerts.value == null) {
            collectionReference.whereEqualTo("userId", user.uid).get().addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    alerts.postValue(listOf(document.toObject(FbAlert::class.java)))
                }
            }
        }
        return alerts
    }

    fun get(id: String, callback: (fbAlert: FbAlert) -> Unit) {
        val documentReference = collectionReference.document(id)
        Log.i(
            TAG,
            "Getting '$id' in '$collectionName'."
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
//        val documentName: String = entity.getEntityKey()
//        val documentReference = collectionReference.document(documentName)
//        Log.i(
//            TAG,
//            "Creating '$documentName' in '$collectionName'."
//        )
//        return documentReference.set(entity).addOnFailureListener { e ->
//            Log.d(
//                TAG,
//                "There was an error creating '$documentName' in '$collectionName'!", e
//            )
//        }
    }

    fun update(alert: FbAlert) {
//        val documentName: String = entity.getEntityKey()
//        val documentReference = collectionReference.document(documentName)
//        Log.i(
//            TAG,
//            "Updating '$documentName' in '$collectionName'."
//        )
//        return documentReference.set(entity).addOnFailureListener { e ->
//            Log.d(
//                TAG,
//                "There was an error updating '$documentName' in '$collectionName'.", e
//            )
//        }
    }

    fun delete(id: String) {
//        val documentReference = collectionReference.document(documentName)
//        Log.i(
//            TAG,
//            "Deleting '$documentName' in '$collectionName'."
//        )
//        return documentReference.delete().addOnFailureListener { e ->
//            Log.d(
//                TAG,
//                "There was an error deleting '$documentName' in '$collectionName'.", e
//            )
//        }
    }

}