package ch.stephgit.windescalator.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ch.stephgit.windescalator.alert.AlertViewModel
import ch.stephgit.windescalator.log.LogCatViewModel
import ch.stephgit.windescalator.util.ViewModelFactory
import ch.stephgit.windescalator.wind.WindViewModel


@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AlertViewModel::class)
    internal abstract fun bindAlertViewModel(alertViewModel: AlertViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(LogCatViewModel::class)
    internal abstract fun bindLogViewModel(logCatViewModel: LogCatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WindViewModel::class)
    internal abstract fun bindWindViewModel(windViewModel: WindViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
