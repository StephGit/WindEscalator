package windescalator.alert.detail

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import ch.stephgit.windescalator.R
import org.joda.time.LocalDateTime
import windescalator.alert.AlertService
import windescalator.alert.detail.direction.DirectionChart
import windescalator.alert.detail.direction.DirectionChartData
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import windescalator.di.Injector
import java.util.*
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


    @Inject
    lateinit var alertService: AlertService

    @Inject
    lateinit var alertRepo: AlertRepo

    @Inject
    lateinit var windResourceAdapter: WindResourceAdapter

    companion object {
        fun newIntent(ctx: Context) = Intent(ctx, AlertDetailActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_detail)
        title = getString(R.string.alert_detail_activity_title)

        Injector.appComponent.inject(this)
        initViewElements()

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
            getTimePickerDialog(startTime)
        }
        endTime.setOnClickListener {
            getTimePickerDialog(endTime)
        }
    }

    private fun getTimePickerDialog(text: EditText) {
        val cldr = Calendar.getInstance()
        val hour = cldr[Calendar.HOUR_OF_DAY]
        val minutes = cldr[Calendar.MINUTE]
        val timePickerDialog = TimePickerDialog(
                this@AlertDetailActivity,
                { _, sHour, sMinute -> text.setText("$sHour:$sMinute") }, hour, minutes, true)
        timePickerDialog.show()
    }

    private fun initSeekBar() {
        seekBar = findViewById(R.id.sb_alert_threshold)
        labelSeekBar = findViewById(R.id.sb_label)
        labelSeekBar.x = 15F
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                labelSeekBar.text = "$progress kts"
                //Get the thumb bound and get its left value
                val x = seekBar.thumb.bounds.left.toFloat()
                //set the left value to textview x value
                labelSeekBar.x = x
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    @SuppressLint("ResourceType")
    private fun initChartData() {
        directionChart = findViewById(R.id.btn_alert_wind_direction)
        val directions: Array<String> = arrayOf("E", "SE", "S", "SW", "W", "NW", "N", "NE")
        val initColorSlice = resources.getString(R.color.windEscalator_colorSelectedLight)
        directions.forEach {
            windDirectionData.add(it, initColorSlice)
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
                Toast.makeText(this,
                        getString(R.string.alert_detail_activity_toast_error_missing_resource),
                        Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this,
                        getString(R.string.alert_detail_activity_toast_error_missing_threshold),
                        Toast.LENGTH_LONG).show()
                false
            }
            (directionChart.getSelectedData().isNullOrEmpty()) -> {
                Toast.makeText(this,
                        getString(R.string.alert_detail_activity_toast_error_missing_directions),
                        Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun saveOrUpdate() {
        if (isValid()) {
            alert.name = alertName.text.toString().trim()
            alert.resource = windResourceSpinner.selectedItem.toString()
            alert.startTime = LocalDateTime(startTime.text)
            alert.endTime = LocalDateTime(endTime.text)
            alert.windForceKts = seekBar.progress
            alert.directions = directionChart.getSelectedData()

            alert.id?.let {
                alertRepo.update(alert)
                if (alert.active) {
                    alertService.addOrUpdate(alert)
                }
            } ?: run {
                alert.active = true
                alertRepo.insert(alert)
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
}

