package ch.stephgit.windescalator

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
import ch.stephgit.windescalator.di.Injector
import ch.stephgit.windescalator.webcam.WebcamAdapter
import ch.stephgit.windescalator.wind.WindViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class WebcamFragment : androidx.fragment.app.Fragment() {

    private lateinit var noWebcamsInfo: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: WindViewModel
    private val webcamAdapter = WebcamAdapter()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    companion object {
        fun newFragment(): androidx.fragment.app.Fragment = WebcamFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_webcam, container, false)
        Injector.appComponent.inject(this)
        requireActivity().title = getString(R.string.webcam_fragment_title)
        noWebcamsInfo = view.findViewById(R.id.webcam_no_cams_exists)
        recyclerView = view.findViewById(R.id.rv_webcams)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = webcamAdapter
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[WindViewModel::class.java]
        subscribeViewModel()

        return view
    }

    private fun subscribeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.windResources.collect { resources ->
                    val withWebcams = resources.filter { it.webcamUrl.isNotBlank() }
                    if (withWebcams.isNotEmpty()) {
                        webcamAdapter.submitList(withWebcams)
                        recyclerView.visibility = View.VISIBLE
                        noWebcamsInfo.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.GONE
                        noWebcamsInfo.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
