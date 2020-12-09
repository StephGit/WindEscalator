package windescalator.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import windescalator.WebcamFragment
import windescalator.WindEscalatorActivity
import windescalator.WindFragment
import windescalator.alert.detail.AlertDetailActivity
import windescalator.alert.AlertFragment
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

    fun inject(oberEscalatorActivity: WindEscalatorActivity)
    fun inject(alertFragment: AlertFragment)
    fun inject(alertDetailActivity: AlertDetailActivity)
    fun inject(windFragment: WindFragment)
    fun inject(webcamFragment: WebcamFragment)

}