package ch.stephgit.windescalator.alert

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.detail.AlertDetailActivity
import ch.stephgit.windescalator.di.Injector
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 view to show alerts in alertRecyclerAdater, add or remove alerts
 */
class AlertFragment : androidx.fragment.app.Fragment() {

    private lateinit var noAlertInfo: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: AlertViewModel
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var swipeBackgroundColor: ColorDrawable
    private lateinit var deleteIcon: Drawable

    // TODO set permissions
//    private lateinit var permissionHandler: PermissionHandler
    private lateinit var prefs: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var recyclerAdapter: AlertRecyclerAdapter


    companion object {
        fun newFragment(): androidx.fragment.app.Fragment = AlertFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alert, container, false)
        Injector.appComponent.inject(this)
        requireActivity().title = getString(R.string.alert_fragment_title)
        prefs = requireActivity().getSharedPreferences("windescalator", Context.MODE_PRIVATE)
        noAlertInfo = view.findViewById(R.id.alert_no_alerts_exists)
        recyclerView = view.findViewById(R.id.lv_alerts)

        view.findViewById<FloatingActionButton>(R.id.btn_addAlert).setOnClickListener {
            addAlert()
        }

        linearLayoutManager = LinearLayoutManager(context)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = recyclerAdapter
        }

        recyclerAdapter.apply {
            onItemClick = { showAlertDetail(it) }
            onSwitch = { onSwitch(it) }
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[AlertViewModel::class.java]
        subscribeViewModel(recyclerAdapter)

        initSwipe()

        return view
    }


    private fun subscribeViewModel(recyclerAdapter: AlertRecyclerAdapter) {

        lifecycleScope.launch {
            viewModel.alerts.collect { alerts ->

                if (alerts != null) {
                    recyclerAdapter.submitList(alerts)
                    noAlertInfo.visibility = View.GONE
                }
            }
        }
    }

    private fun initSwipe() {
        swipeBackgroundColor = ColorDrawable(
            ResourcesCompat.getColor(
                resources,
                R.color.windEscalator_colorDelete,
                null
            )
        )
        deleteIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_delete, null)!!

        val simpleItemTouchCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                val removedAlert = recyclerAdapter.removeItem(viewHolder)
                if (recyclerAdapter.itemCount <= 1) {
                    noAlertInfo.visibility = View.VISIBLE
                }
                viewModel.delete(removedAlert)
                Snackbar.make(
                    viewHolder.itemView,
                    "${removedAlert.name} " + context!!.getString(R.string.alert_removed),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(context!!.getString(R.string.rollback)) {
                        noAlertInfo.visibility = View.GONE
                        viewModel.insert(removedAlert)
                    }
                    .setActionTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.windEscalator_colorSnackAction,
                            null
                        )
                    )
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                with(itemView) {
                    val iconMargin = (height - deleteIcon.intrinsicHeight) / 2
                    if (dX > 0) {
                        swipeBackgroundColor.setBounds(left, top, dX.toInt(), bottom)
                        deleteIcon.setBounds(
                            left + iconMargin,
                            top + iconMargin,
                            left + iconMargin + deleteIcon.intrinsicWidth,
                            bottom - iconMargin
                        )
                    } else {
                        swipeBackgroundColor.setBounds(right + dX.toInt(), top, right, bottom)
                        deleteIcon.setBounds(
                            right - iconMargin - deleteIcon.intrinsicWidth,
                            top + iconMargin,
                            right - iconMargin,
                            bottom - iconMargin
                        )
                    }

                    swipeBackgroundColor.draw(c)
                    c.save()

                    if (dX > 0)
                        c.clipRect(left, top, dX.toInt(), bottom)
                    else
                        c.clipRect(right + dX.toInt(), top, right, bottom)
                    deleteIcon.draw(c)
                    c.restore()
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun onSwitch(alert: ch.stephgit.windescalator.data.Alert) {
        viewModel.update(alert)
    }

    private fun addAlert() {
        startActivity(AlertDetailActivity.newIntent(requireContext()))
    }

    private fun showAlertDetail(item: ch.stephgit.windescalator.data.Alert) {
        val intent = Intent(activity?.baseContext, AlertDetailActivity::class.java)
        intent.putExtra("ALERT", item)
        startActivity(intent)
    }
}