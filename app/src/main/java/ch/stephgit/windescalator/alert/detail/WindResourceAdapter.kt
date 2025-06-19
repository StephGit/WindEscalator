package ch.stephgit.windescalator.alert.detail

import android.content.Context
import android.database.Cursor
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import ch.stephgit.windescalator.R
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import javax.inject.Inject

class WindResourceAdapter @Inject constructor(
    context: Context, query: CollectionReference) :
        ArrayAdapter<WindResource>(context, 0) {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    // FIXME use flow to ensure loaded resource after setViewElementsData in AlertDetailActivity
    private val results = query.orderBy("localId").get().addOnSuccessListener { result ->
        for (document in result) {
            var resource = document.toObject(WindResource::class.java)
            resource.id = document.id
            add(resource)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View = if (convertView == null && position == 0) {
            layoutInflater.inflate(R.layout.header_wind_resource, parent, false)
        } else {
            layoutInflater.inflate(R.layout.item_wind_resource, parent, false)
        }
        getItem(position)?.let { resource ->
            setItemForResource(view, resource)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        if (position == 0) {
            view = layoutInflater.inflate(R.layout.header_wind_resource, parent, false)
            view.setOnClickListener {
                val root = parent.rootView
                root.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
                root.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
            }
        } else {
            view = layoutInflater.inflate(R.layout.item_wind_resource, parent, false)
            getItem(position)?.let { resource ->
                setItemForResource(view, resource)
            }
        }
        return view
    }


    override fun getItem(position: Int): WindResource? {
        if (position == 0) {
            return null
        }

        return super.getItem(position - 1)
    }

    override fun getCount() = super.getCount() + 1
    override fun isEnabled(position: Int) = position != 0
    private fun setItemForResource(view: View, resource: WindResource) {
        val tvResource = view.findViewById<TextView>(R.id.tvAlertResource)
        val ivResource = view.findViewById<ImageView>(R.id.ivAlertResource)
        tvResource.text = resource.displayName
        resource.icon.let { icon -> ivResource.setBackgroundResource(icon) }
    }
}