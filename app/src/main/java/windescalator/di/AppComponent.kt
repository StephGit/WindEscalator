package windescalator.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import windescalator.WebcamFragment
import windescalator.WindEscalatorActivity
import windescalator.WindFragment
import windescalator.alert.detail.AlertDetailActivity
import windescalator.alert.AlertFragment
import windescalator.alert.AlertNotificationActivity
import windescalator.alert.receiver.AlertBroadcastReceiver
import windescalator.alert.receiver.BootBroadcastReceiver
import windescalator.alert.service.AlertService
import windescalator.alert.service.NoiseControl
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

    fun inject(windFragment: WindFragment)
    fun inject(webcamFragment: WebcamFragment)

    fun inject(bootBroadcastReceiverTask: BootBroadcastReceiver)
    fun inject(alertBroadcastReceiver: AlertBroadcastReceiver)

}