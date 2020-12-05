package oberescalator.di

import android.app.Application

object Injector {
    lateinit var appComponent : AppComponent
    fun init(application: Application) {
        appComponent = builder()
            .application(application)
            .build()
    }
}