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
import ch.stephgit.windescalator.alert.receiver.AlarmBroadcastReceiver
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
import ch.stephgit.windescalator.alert.receiver.BootBroadcastReceiver
import ch.stephgit.windescalator.alert.service.AlarmHandler
import ch.stephgit.windescalator.alert.service.AlertJobIntentService
import ch.stephgit.windescalator.alert.service.FirebaseForgroundMessagingService
import ch.stephgit.windescalator.alert.service.NoiseHandler
import ch.stephgit.windescalator.alert.service.WindDataHandler
import ch.stephgit.windescalator.log.LogFragment
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
    fun inject(alertJobIntentService: AlertJobIntentService)
    fun inject(firebaseForgroundMessagingService: FirebaseForgroundMessagingService)
    fun inject(noiseHandler: NoiseHandler)
    fun inject(windDataAdapter: WindDataHandler)
    fun inject(alarmHandler: AlarmHandler)

    fun inject(windFragment: WindFragment)
    fun inject(webcamFragment: WebcamFragment)
    fun inject(logFragment: LogFragment)

    fun inject(bootBroadcastReceiverTask: BootBroadcastReceiver.BootReceiverTask)
    fun inject(alertBroadcastReceiver: AlertBroadcastReceiver)
    fun inject(alarmBroadcastReceiver: AlarmBroadcastReceiver)

}