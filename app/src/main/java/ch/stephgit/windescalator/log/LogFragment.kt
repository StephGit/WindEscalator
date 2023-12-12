package ch.stephgit.windescalator.log



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.di.Injector

class LogFragment : androidx.fragment.app.Fragment() {

    private lateinit var noWindMessureInfo: ConstraintLayout
    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        fun newFragment(): androidx.fragment.app.Fragment = LogFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_log, container, false)
        Injector.appComponent.inject(this)
        requireActivity().title = getString(R.string.wind_fragment_title)
        noWindMessureInfo = view.findViewById(R.id.log_no_logs_exists)
        linearLayoutManager = LinearLayoutManager(context)

        return view
    }
}
