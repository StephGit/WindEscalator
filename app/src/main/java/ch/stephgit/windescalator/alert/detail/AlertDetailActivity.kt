package ch.stephgit.windescalator.alert.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import ch.stephgit.windescalator.R
import ch.stephgit.windescalator.alert.detail.direction.Direction
import ch.stephgit.windescalator.alert.detail.direction.DirectionChart
import ch.stephgit.windescalator.alert.detail.direction.DirectionChartData
import ch.stephgit.windescalator.data.entity.Alert
import ch.stephgit.windescalator.data.repo.AlertRepo
import ch.stephgit.windescalator.di.Injector
import javax.inject.Inject


class AlertDetailActivity : AppCompatActivity() {

    private lateinit var alert: Alert
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

    @Inject
    lateinit var alertRepo: AlertRepo

    @Inject
    lateinit var windResourceAdapter: WindResourceAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, AlertDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_detail)
        title = getString(R.string.alert_detail_activity_title)

        Injector.appComponent.inject(this)
        initViewElements()

        timeViewModel = ViewModelProvider(this).get(TimeViewModel::class.java)

        val extras = intent.extras
        val alertId = extras?.get("ALERT_ID") as Long?
        alertId?.let {
            getAlertFromRepo(alertId)
        }
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
            showTimePickerDialog(it)
        }
        endTime.setOnClickListener {
            subscribeViewModel(endTime)
            showTimePickerDialog(it)
        }
    }

    private fun subscribeViewModel(text: EditText) {
        timeViewModel.time.removeObservers(this)
        timeViewModel.time.observe(this, Observer {
            text.setText(it)
        })
    }

//
//    private fun getTimePickerDialog(text: EditText) {
//        val cldr = Calendar.getInstance()
//        val hour = cldr[Calendar.HOUR_OF_DAY]
//        val minutes = cldr[Calendar.MINUTE]
//        val timePickerDialog = TimePickerDialog(
//            this@AlertDetailActivity,
//            { _, sHour, sMinute -> text.setText(String.format("%02d:%02d", sHour, sMinute)) },
//            hour,
//            minutes,
//            true
//        )
//        timePickerDialog.show()
//    }

    private fun initSeekBar() {
        seekBar = findViewById(R.id.sb_alert_threshold)
        labelSeekBar = findViewById(R.id.sb_label)
        labelSeekBar.x = 15F
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                labelSeekBar.text = "$progress kts"
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
        directionChart.setInitialData(windDirectionData)
    }

    private fun setViewElementsData() {
        alertName.setText(getAlertName())
        windResourceSpinner.setSelection(WindResource.valueOf(alert.resource!!).id)
        startTime.setText(alert.startTime.toString())
        endTime.setText(alert.endTime.toString())
        seekBar.progress = getWindForce()
        if (!alert.directions.isNullOrEmpty()) alert.directions?.let { directionChart.setData(it) }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        labelSeekBar.x = (seekBar.width) * seekBar.progress / seekBar.max + 0F
    }

    private fun initAlert() {
        this.alert = Alert(null, false, null, null, null, null, listOf())
    }

    private fun getAlertName(): String {
        if (::alert.isInitialized && this.alert.name != null) return this.alert.name!!
        return ""
    }

    private fun getWindForce(): Int {
        return if (alert.windForceKts != null) alert.windForceKts!! else 1
    }

    private fun getAlertFromRepo(alertId: Long) {
        alertRepo.getAlert(alertId)?.let {
            this.alert = it
        }
    }

    private fun isValid(): Boolean {
        return when {
            alertName.text.trim().isBlank() -> {
                alertName.error = getString(R.string.alert_detail_activity_error_no_name)
                false
            }
            (windResourceSpinner.selectedItemId == 0L) -> {
                Toast.makeText(
                    this,
                    getString(R.string.alert_detail_activity_toast_error_missing_resource),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            startTime.text.isNullOrBlank() -> {
                startTime.error = getString(R.string.alert_detail_activity_error_missing_starttime)
                false
            }
            endTime.text.isNullOrBlank() -> {
                endTime.error = getString(R.string.alert_detail_activity_error_missing_endtime)
                false
            }
            (seekBar.progress == 0) -> {
                Toast.makeText(
                    this,
                    getString(R.string.alert_detail_activity_toast_error_missing_threshold),
                    Toast.LENGTH_LONG
                ).show()
                false
            }
            (directionChart.getSelectedData().isNullOrEmpty()) -> {
                Toast.makeText(
                    this,
                    getString(R.string.alert_detail_activity_toast_error_missing_directions),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            else -> true
        }
    }

    private fun saveOrUpdate() {
        if (isValid()) {
            alert.name = alertName.text.toString().trim()
            alert.resource = windResourceSpinner.selectedItem.toString()
            alert.startTime = startTime.text.toString()
            alert.endTime = endTime.text.toString()
            alert.windForceKts = seekBar.progress
            alert.directions = directionChart.getSelectedData()

            alert.id?.let {
                alertRepo.update(alert)
            } ?: run {
                alert.active = true
                alertRepo.insert(alert)
                // TODO needs start of alert?
//                val alertServiceIntent = Intent(this.applicationContext, AlertService::class.java)
//                startService(alertServiceIntent)
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

    private fun showTimePickerDialog(v: View) {
        TimePickerFragment().show(supportFragmentManager, TimePickerFragment.TAG)
    }
}

