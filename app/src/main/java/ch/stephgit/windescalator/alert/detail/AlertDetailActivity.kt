package ch.stephgit.windescalator.alert.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.TAG
import ch.stephgit.windescalator.alert.detail.direction.Direction
import ch.stephgit.windescalator.alert.detail.direction.DirectionChart
import ch.stephgit.windescalator.alert.detail.direction.DirectionChartData
import ch.stephgit.windescalator.data.FbAlert
import ch.stephgit.windescalator.data.repo.AlertRepository
import ch.stephgit.windescalator.di.Injector
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import javax.inject.Inject


class AlertDetailActivity : AppCompatActivity() {

    private lateinit var alert: FbAlert
    private lateinit var alertName: EditText
    private lateinit var windResourceSpinner: Spinner
    private lateinit var startTime: EditText
    private lateinit var endTime: EditText
    private lateinit var seekBar: SeekBar
    private lateinit var labelSeekBar: TextView
    private val windDirectionData = DirectionChartData()
    private lateinit var directionChart: DirectionChart
    private lateinit var saveButton: Button
    private lateinit var timeViewModel: TimeViewModel
    private lateinit var user: FirebaseUser

    @Inject
    lateinit var alertRepo: AlertRepository

    @Inject
    lateinit var windResourceAdapter: WindResourceAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var db: FirebaseFirestore


    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, AlertDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_detail)
        title = getString(R.string.alert_detail_activity_title)

        Injector.appComponent.inject(this)
        initViewElements()

        this.user = Firebase.auth.currentUser!!
        Log.d(TAG, "user id ${user.uid}")

        timeViewModel = ViewModelProvider(this).get(TimeViewModel::class.java)

        val extras = intent.extras
        val alertId = extras?.getLong("ALERT_ID")
//        alertId?.let {
            getAlertFromRepo("testalert1")
//        }
        if (!::alert.isInitialized) initAlert() else setViewElementsData()
    }

    private fun initViewElements() {
        alertName = findViewById(R.id.et_add_alert_name)
        initWindResourceSpinner()
        initTimePickers()
        initSeekBar()
        initChartData()
        saveButton = findViewById(R.id.btn_alert_save)
        saveButton.setOnClickListener { saveOrUpdate() }
    }

    private fun initWindResourceSpinner() {
        windResourceSpinner = findViewById(R.id.sp_select_alert_resource)
        windResourceSpinner.adapter = windResourceAdapter
    }

    private fun initTimePickers() {
        startTime = findViewById(R.id.et_alert_start_time)
        startTime.inputType = InputType.TYPE_NULL
        endTime = findViewById(R.id.et_alert_end_time)
        endTime.inputType = InputType.TYPE_NULL
        startTime.setOnClickListener {
            subscribeViewModel(startTime)
            showTimePickerDialog()
        }
        endTime.setOnClickListener {
            subscribeViewModel(endTime)
            showTimePickerDialog()
        }
    }

    private fun subscribeViewModel(text: EditText) {
        timeViewModel.time.removeObservers(this)
        timeViewModel.time.observe(this, Observer {
            text.setText(it)
        })
    }

    private fun initSeekBar() {
        seekBar = findViewById(R.id.sb_alert_threshold)
        labelSeekBar = findViewById(R.id.sb_label)
        labelSeekBar.x = 15F
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("StringFormatMatches")
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                labelSeekBar.text = getString(R.string.progress_kts, progress)
                //Get the thumb bound and get its right value
                val x = seekBar.thumb.bounds.right.toFloat()
                //set the value to textview x value
                labelSeekBar.x = x
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    @SuppressLint("ResourceType")
    private fun initChartData() {
        directionChart = findViewById(R.id.btn_alert_wind_direction)
        val directions: Array<Direction> = Direction.values()
        val initColorSlice = resources.getString(R.color.windEscalator_colorSelectedLight)
        directions.forEach {
            windDirectionData.add(it.name, initColorSlice)
        }
        directionChart.setInitialData(windDirectionData, true)
    }

    private fun setViewElementsData() {
        alertName.setText(getAlertName())

        getWindResourceById { windResource: WindResource ->
            windResourceSpinner.setSelection(
                windResource.position
            )
        }

        startTime.setText(alert.startTime.toString())
        endTime.setText(alert.endTime.toString())
        seekBar.progress = getWindForce()
        if (!alert.directions.isNullOrEmpty()) alert.directions?.let { directionChart.setData(it) }
    }

    private fun getWindResourceById(callback: (windResource: WindResource) -> Unit) {
        db.collection("windResource").document(alert.resource).get().addOnSuccessListener {
            Log.d(TAG, it.data.toString())
            it.toObject<WindResource>()?.let { it1 -> callback(it1) }
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        labelSeekBar.x = (seekBar.width) * seekBar.progress / seekBar.max + 0F
    }

    private fun initAlert() {
        this.alert = FbAlert()
    }

    private fun getAlertName(): String {
        if (::alert.isInitialized && this.alert.name != null) return this.alert.name!!
        return ""
    }

    private fun getWindForce(): Int {
        return if (alert.windForceKts != null) alert.windForceKts!! else 1
    }

    private fun getAlertFromRepo(alertId: String) {
        alertRepo.get(alertId) { it: FbAlert ->
            this.alert = it
        }
        if (this.alert.name == "") {
            showErrorToast("Couldn't load alert from db")
        }
    }

    private fun isValid(): Boolean {
        return when {
            alertName.text.trim().isBlank() -> {
                alertName.error = getString(R.string.alert_detail_activity_error_no_name)
                false
            }

            (windResourceSpinner.selectedItemId == 0L) -> {
                showErrorToast(getString(R.string.alert_detail_activity_toast_error_missing_resource))
                false
            }

            startTime.text.isNullOrBlank() -> {
                showErrorToast(getString(R.string.alert_detail_activity_error_missing_starttime))
                false
            }

            endTime.text.isNullOrBlank() -> {
                showErrorToast(getString(R.string.alert_detail_activity_error_missing_endtime))
                false
            }

            startTime.text.equals(endTime.text) -> {
                showErrorToast(getString(R.string.alert_detail_activity_error_same_start_and_endtime))
                false
            }

            (startTime.text.toString() > endTime.text.toString()) -> {
                showErrorToast(getString(R.string.alert_detail_activity_error_endtime_before_starttime))
                false
            }

            (seekBar.progress == 0) -> {
                showErrorToast(getString(R.string.alert_detail_activity_toast_error_missing_threshold))
                false
            }

            (directionChart.getSelectedData().isNullOrEmpty()) -> {
                showErrorToast(getString(R.string.alert_detail_activity_toast_error_missing_directions))
                false
            }

            else -> true
        }
    }

    private fun showErrorToast(error: String) {
        Toast.makeText(
            this,
            error,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun saveOrUpdate() {
        if (isValid()) {
            alert.name = alertName.text.toString().trim()
            alert.resource = (windResourceSpinner.selectedItem as WindResource).id
            alert.startTime = startTime.text.toString()
            alert.endTime = endTime.text.toString()
            alert.windForceKts = seekBar.progress
            alert.directions = directionChart.getSelectedData()

            (alert.id != "")?.let {
                    alertRepo.update(alert)
                    Log.d(TAG, "AlertDetailActivity: update alert -> $alert")

            } ?: run {
                    Log.d(TAG, "AlertDetailActivity: add alert -> $alert")
                    alert.active = true
                    alertRepo.create(alert)

            }
            finish()
            Toast.makeText(
                this,
                getString(R.string.alert_detail_activity_toast_saved_alert),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun showTimePickerDialog() {
        TimePickerFragment().show(supportFragmentManager, TimePickerFragment.TAG)
    }
}

