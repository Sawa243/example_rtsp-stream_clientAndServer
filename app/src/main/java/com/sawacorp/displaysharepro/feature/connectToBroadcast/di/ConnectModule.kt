package com.sawacorp.displaysharepro.feature.connectToBroadcast.di

import android.content.Context
import com.sawacorp.displaysharepro.feature.connectToBroadcast.database.AppDatabase
import com.sawacorp.displaysharepro.feature.connectToBroadcast.repository.ConnectRepository
import com.sawacorp.displaysharepro.feature.connectToBroadcast.storages.ActiveStreamStorage
import com.sawacorp.displaysharepro.feature.connectToBroadcast.storages.RtspUrlStorage
import com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase.ConnectUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ConnectModule {

    @Provides
    fun providesConnectUseCase(repository: ConnectRepository) = ConnectUseCase(repository)

    @Provides
    fun providesConnectRepository(
        @ApplicationContext
        context: Context,
        database: AppDatabase,
        rtspUrlStorage: RtspUrlStorage,
        activeStreamStorage: ActiveStreamStorage
    ) = ConnectRepository(context, database, rtspUrlStorage, activeStreamStorage)

    @Provides
    fun providesAppDatabase(
        @ApplicationContext
        application: Context
    ) = AppDatabase.getDatabase(application)

    @Provides
    @Singleton
    fun providesRtspUrlStorage(
    ) = RtspUrlStorage()

    @Provides
    @Singleton
    fun providesActiveStreamStorage(
    ) = ActiveStreamStorage()
}