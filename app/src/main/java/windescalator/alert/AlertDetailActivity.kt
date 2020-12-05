package windescalator.alert

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class AlertDetailActivity : AppCompatActivity() {

    companion object{
        fun newIntent(ctx: Context)= Intent(ctx, AlertDetailActivity::class.java)
    }

}
