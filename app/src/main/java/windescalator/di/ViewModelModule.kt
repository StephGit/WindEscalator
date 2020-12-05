package windescalator.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import windescalator.alert.AlertViewModel
import util.ViewModelFactory


@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AlertViewModel::class)
    internal abstract fun bindAlertViewModel(alertViewModel: AlertViewModel): ViewModel


    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}