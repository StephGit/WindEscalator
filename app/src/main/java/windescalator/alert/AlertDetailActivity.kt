package windescalator.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import windescalator.data.entity.Alert
import windescalator.data.repo.AlertRepo
import windescalator.di.Injector
import javax.inject.Inject
import windescalator.R

class AlertDetailActivity : AppCompatActivity() {

    private lateinit var alert: Alert

    @Inject
    lateinit var alertService: AlertService

    @Inject
    lateinit var alertRepo: AlertRepo

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
        alertId?.let {
            getAlertFromRepo(alertId)
        }
    }

    private fun getAlertFromRepo(alertId: Long): Any {
        alertRepo.getAlert(alertId)?.let {
            this.alert = it
        }
    }
}

