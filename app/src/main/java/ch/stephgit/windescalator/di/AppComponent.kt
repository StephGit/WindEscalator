package ch.stephgit.windescalator.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import ch.stephgit.windescalator.WebcamFragment
import ch.stephgit.windescalator.WindEscalatorActivity
import ch.stephgit.windescalator.wind.WindFragment
import ch.stephgit.windescalator.alert.AlertFragment
import ch.stephgit.windescalator.alert.AlertNotificationActivity
import ch.stephgit.windescalator.alert.detail.AlertDetailActivity
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
import ch.stephgit.windescalator.alert.service.AlertMessagingService
import ch.stephgit.windescalator.alert.service.NoiseHandler
import ch.stephgit.windescalator.log.LogFragment
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance // Dagger injects in DependencyGraph
        fun application(application: Application): Builder
        fun build(): AppComponent
    }

    fun inject(windEscalatorActivity: WindEscalatorActivity)

    fun inject(alertFragment: AlertFragment)
    fun inject(alertDetailActivity: AlertDetailActivity)
    fun inject(alertNotificationActivity: AlertNotificationActivity)
    fun inject(alertMessagingService: AlertMessagingService)
    fun inject(noiseHandler: NoiseHandler)

    fun inject(windFragment: WindFragment)
    fun inject(webcamFragment: WebcamFragment)
    fun inject(logFragment: LogFragment)

    fun inject(alertBroadcastReceiver: AlertBroadcastReceiver)

    fun inject(firebaseDb: FirebaseFirestore)
}