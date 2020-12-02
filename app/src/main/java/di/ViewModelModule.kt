package di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import util.ViewModelFactory


@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}