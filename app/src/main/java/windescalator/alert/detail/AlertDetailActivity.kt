package windescalator.alert.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import windescalator.di.Injector
import javax.inject.Inject
import ch.stephgit.windescalator.R
import windescalator.alert.AlertService
import windescalator.alert.detail.direction.DirectionChartData
import windescalator.alert.detail.direction.DirectionChart
import java.util.*

class AlertDetailActivity : AppCompatActivity() {

    private val windDirectionData = DirectionChartData()
    private lateinit var directionChart: DirectionChart
    private lateinit var windResourceSpinner: Spinner
    private lateinit var alert: Alert
    private lateinit var alertName: EditText

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

        val extras = intent.extras
        val alertId = extras?.get("ALERT_ID") as Long?
        alertName = findViewById(R.id.et_add_alert_name)
        alertId?.let {
            getAlertFromRepo(alertId)
        }

        if (!::alert.isInitialized) initAlert()
        val saveButton: Button = findViewById(R.id.btn_alert_save)
        saveButton.setOnClickListener { saveOrUpdate() }
        alertName.setText(getAlertName())
        initWindResourceSpinner()
        initChartData()

    }

    private fun initAlert() {
        this.alert = Alert(null, false, null, null,null,null)
    }

    private fun getAlertName(): String {
        if (::alert.isInitialized && this.alert.name != null) return this.alert.name!!
        return ""
    }

    private fun initWindResourceSpinner() {
        windResourceSpinner = findViewById(R.id.sp_select_alert_resource)
        windResourceSpinner.adapter = windResourceAdapter
    }

    @SuppressLint("ResourceType")
    private fun initChartData() {
        directionChart = findViewById(R.id.btn_alert_wind_direction)
        val directions: Array<String> = arrayOf<String>("E", "SE", "S", "SW", "W", "NW", "N", "NE")
        val initColorSlice = resources.getString(R.color.windEscalator_colorSelectedLight)
        directions.forEach {
            windDirectionData.add(it, initColorSlice)
        }
        directionChart.setData(windDirectionData)
    }

    private fun getAlertFromRepo(alertId: Long) {
        alertRepo.getAlert(alertId)?.let {
            this.alert = it
        }
    }

    private fun isValid(): Boolean {
        return when {
            alertName.text.trim().isBlank() -> {
                alertName.error = getString(R.string.alert_edit_error_no_name)
                false
            }
            else -> true
        }
    }

    private fun saveOrUpdate() {
        alert.name = alertName.text.toString().trim()
        if (isValid()) {
            alert.id?.let {
                alertRepo.update(alert)
                if (alert.active) {
                    alertService.addOrUpdate(alert)
                }
            } ?: run {
                alert.requestId = alert.name + UUID.randomUUID()
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

