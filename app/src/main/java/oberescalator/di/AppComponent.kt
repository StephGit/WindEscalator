package oberescalator.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import oberescalator.OberEscalatorActivity
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

    fun inject(oberEscalatorActivity: OberEscalatorActivity)

}