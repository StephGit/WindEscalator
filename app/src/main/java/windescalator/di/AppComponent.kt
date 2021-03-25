package windescalator.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import windescalator.WebcamFragment
import windescalator.WindEscalatorActivity
import windescalator.WindFragment
import windescalator.alert.AlertFragment
import windescalator.alert.AlertNotificationActivity
import windescalator.alert.detail.AlertDetailActivity
import windescalator.alert.receiver.AlarmBroadcastReceiver
import windescalator.alert.receiver.AlertBroadcastReceiver
import windescalator.alert.receiver.BootBroadcastReceiver
import windescalator.alert.service.AlarmHandler
import windescalator.alert.service.AlertService
import windescalator.alert.service.NoiseControl
import windescalator.alert.service.WindDataHandler
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
    fun inject(alertService: AlertService)
    fun inject(noiseControl: NoiseControl)
    fun inject(windDataAdapter: WindDataHandler)
    fun inject(alarmHandler: AlarmHandler)

    fun inject(windFragment: WindFragment)
    fun inject(webcamFragment: WebcamFragment)

    fun inject(bootBroadcastReceiverTask: BootBroadcastReceiver.BootReceiverTask)
    fun inject(alertBroadcastReceiver: AlertBroadcastReceiver)
    fun inject(alarmBroadcastReceiver: AlarmBroadcastReceiver)

}