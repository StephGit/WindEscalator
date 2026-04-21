package ch.stephgit.windescalator.wind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.di.Injector
import kotlinx.coroutines.launch
import javax.inject.Inject

class WindFragment : androidx.fragment.app.Fragment() {

    private lateinit var noWindMessureInfo: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: WindViewModel
    private val windDataAdapter = WindDataAdapter()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    companion object {
        fun newFragment(): androidx.fragment.app.Fragment = WindFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_wind, container, false)
        Injector.appComponent.inject(this)
        requireActivity().title = getString(R.string.wind_fragment_title)
        noWindMessureInfo = view.findViewById(R.id.wind_no_messures_exists)
        recyclerView = view.findViewById(R.id.rv_wind_data)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = windDataAdapter
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[WindViewModel::class.java]
        subscribeViewModel()

        return view
    }

    private fun subscribeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.windResources.collect { resources ->
                    if (resources.isNotEmpty()) {
                        windDataAdapter.submitList(resources)
                        recyclerView.visibility = View.VISIBLE
                        noWindMessureInfo.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.GONE
                        noWindMessureInfo.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
