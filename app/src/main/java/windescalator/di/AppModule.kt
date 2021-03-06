package windescalator.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import windescalator.alert.AlertRecyclerAdapter
import windescalator.alert.detail.WindResourceAdapter
import windescalator.alert.receiver.AlertBroadcastReceiver
import windescalator.alert.service.NoiseControl
import windescalator.data.AppDatabase
import windescalator.data.dao.AlertDao
import windescalator.data.repo.AlertRepo
import windescalator.remote.NotificationHandler
import javax.inject.Singleton


@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application


    @Provides
    fun provideAppDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "WindEscalatorDB").build()


    @Provides
    @Singleton
    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()

    @Provides
    @Singleton
    fun provideAlertRepository(alertDao: AlertDao): AlertRepo =
            AlertRepo(alertDao)

    @Provides
    @Singleton
    fun provideAlertRecyclerAdapter(
            context: Context):
            AlertRecyclerAdapter = AlertRecyclerAdapter(context)

    @Provides
    @Singleton
    fun provideWindResourceAdapter(context: Context):
            WindResourceAdapter = WindResourceAdapter(context)

    @Provides
    @Singleton
    fun provideNotificationHandler(context: Context):
            NotificationHandler = NotificationHandler(context)

    @Provides
    @Singleton
    fun provideNoiseControl(context: Context):
            NoiseControl = NoiseControl(context)

    @Provides
    @Singleton
    fun provideAlertBroadcastReceiver(): AlertBroadcastReceiver = AlertBroadcastReceiver()

}