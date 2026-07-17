package ch.stephgit.windescalator.di

import android.app.Application
import ch.stephgit.windescalator.di.DaggerAppComponent.builder

object Injector {
    lateinit var appComponent: AppComponent
        private set

    fun init(application: Application) {
        if (::appComponent.isInitialized) return

        synchronized(this) {
            if (::appComponent.isInitialized) return

            appComponent = builder()
                .application(application)
                .build()
        }
    }
}