package ch.stephgit.windescalator

import android.app.Application
import ch.stephgit.windescalator.di.Injector

val Any.TAG: String
    get() {
        return "WindEscalator"
    }
val CHANNEL_ID: String = "WindEscalatorApp"
class WindEscalatorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Injector.init(this)
    }
}