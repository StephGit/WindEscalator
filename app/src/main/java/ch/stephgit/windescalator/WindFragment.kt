package ch.stephgit.windescalator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.di.Injector

class WindFragment : androidx.fragment.app.Fragment() {

    private lateinit var noWindMessureInfo: ConstraintLayout
    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        fun newFragment(): androidx.fragment.app.Fragment = WindFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_wind, container, false)
        Injector.appComponent.inject(this)
        requireActivity().title = getString(R.string.wind_fragment_title)
        noWindMessureInfo = view.findViewById(R.id.wind_no_messures_exists)
        linearLayoutManager = LinearLayoutManager(context)

        return view
    }
}
