package ch.stephgit.windescalator.log



import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.AlertViewModel
import ch.stephgit.windescalator.di.Injector
import javax.inject.Inject

class LogFragment : androidx.fragment.app.Fragment() {

    private lateinit var noWindMessureInfo: ConstraintLayout
    private lateinit var logMessageTextView: TextView

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var viewModel: LogCatViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    companion object {
        fun newFragment(): androidx.fragment.app.Fragment = LogFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_log, container, false)
        Injector.appComponent.inject(this)
        requireActivity().title = getString(R.string.log_fragment_title)
        noWindMessureInfo = view.findViewById(R.id.log_no_logs_exists)
        logMessageTextView = view.findViewById(R.id.tv_logs)
        linearLayoutManager = LinearLayoutManager(context)


        viewModel = ViewModelProvider(this, viewModelFactory)[LogCatViewModel::class.java]



        viewModel.logCatOutput().observe(viewLifecycleOwner, Observer{ logMessage ->
            noWindMessureInfo.visibility = GONE
            logMessageTextView.append("$logMessage\n")
        })

        return view
    }
}
