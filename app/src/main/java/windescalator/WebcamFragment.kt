package windescalator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import ch.stephgit.windescalator.R
import windescalator.di.Injector

class WebcamFragment : androidx.fragment.app.Fragment() {

    private lateinit var noWebcamsInfo: ConstraintLayout
    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        fun newFragment(): androidx.fragment.app.Fragment = WebcamFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_webcam, container, false)
        Injector.appComponent.inject(this)
        activity!!.title = getString(R.string.webcam_fragment_title)
        noWebcamsInfo = view.findViewById(R.id.webcam_no_cams_exists)
        linearLayoutManager = LinearLayoutManager(context)

        return view
    }
}
