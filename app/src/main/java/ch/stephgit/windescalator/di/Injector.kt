package ch.stephgit.windescalator.di

import android.app.Application
import ch.stephgit.windescalator.di.DaggerAppComponent.builder

object Injector {
    lateinit var appComponent : AppComponent
    fun init(application: Application) {
        appComponent = builder()
            .application(application)
            .build()
    }
}