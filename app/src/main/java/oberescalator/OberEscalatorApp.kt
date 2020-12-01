package oberescalator

import android.app.Application

val Any.TAG: String
    get() {
        return "OberEscalator"
    }
class OberEscalatorApp : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}