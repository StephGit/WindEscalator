package ch.stephgit.windescalator.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import ch.stephgit.windescalator.alert.AlertRecyclerAdapter
import ch.stephgit.windescalator.alert.detail.WindResourceAdapter
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
import ch.stephgit.windescalator.alert.service.AlarmHandler
import ch.stephgit.windescalator.alert.service.NoiseHandler
import ch.stephgit.windescalator.alert.service.WindDataHandler
import ch.stephgit.windescalator.data.AppDatabase
import ch.stephgit.windescalator.data.dao.AlertDao
import ch.stephgit.windescalator.data.repo.AlertRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


private const val DB_NAME = "WindEscalatorDB"

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application


    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build()


    @Provides
    @Singleton
    fun provideAlertDao(database: AppDatabase): AlertDao = database.alertDao()

    @Provides
    @Singleton
    fun provideAlertRepository(alertDao: AlertDao): AlertRepo =
            AlertRepo(alertDao)

    @Provides
    @Singleton
    fun provideAlertRecyclerAdapter(alarmHandler: AlarmHandler):
            AlertRecyclerAdapter = AlertRecyclerAdapter(alarmHandler)

    @Provides
    @Singleton
    fun provideWindResourceAdapter(context: Context):
            WindResourceAdapter = WindResourceAdapter(context)

    @Provides
    @Singleton
    fun provideNoiseHandler(context: Context):
            NoiseHandler = NoiseHandler(context)

    @Provides
    @Singleton
    fun provideWindDataHandler(context: Context):
            WindDataHandler = WindDataHandler(context)


    @Provides
    @Singleton
    fun provideAlarmHandler(
            context: Context,
            alertRepo: AlertRepo):
            AlarmHandler = AlarmHandler(context, alertRepo)

    @Provides
    @Singleton
    fun provideAlertBroadcastReceiver(): AlertBroadcastReceiver = AlertBroadcastReceiver()

}