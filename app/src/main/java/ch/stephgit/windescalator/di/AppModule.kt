package ch.stephgit.windescalator.di

import android.app.Application
import android.content.Context
import ch.stephgit.windescalator.alert.AlertRecyclerAdapter
import ch.stephgit.windescalator.alert.detail.WindResourceAdapter
import ch.stephgit.windescalator.alert.receiver.AlertBroadcastReceiver
import ch.stephgit.windescalator.alert.service.AlertMessagingService
import ch.stephgit.windescalator.alert.service.NoiseHandler
import ch.stephgit.windescalator.data.AlertRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
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
    fun provideFbAlertRepository(firestore: FirebaseFirestore): AlertRepository =
        AlertRepository(firestore)

    @Provides
    @Singleton
    fun provideAlertRecyclerAdapter():
            AlertRecyclerAdapter = AlertRecyclerAdapter()

    @Provides
    @Singleton
    fun provideWindResourceAdapter(context: Context):
            WindResourceAdapter = WindResourceAdapter(context, provideFirebaseDb().collection("windResource") )

    @Provides
    @Singleton
    fun provideNoiseHandler(context: Context):
            NoiseHandler = NoiseHandler(context)


    @Provides
    @Singleton
    fun provideAlertBroadcastReceiver(): AlertBroadcastReceiver = AlertBroadcastReceiver()

    @Provides
    @Singleton
    fun provideFirebaseDb(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseForgoundMessagingService(): AlertMessagingService = AlertMessagingService()
}