package com.sawacorp.displaysharepro.feature.connectToBroadcast.di

import android.content.Context
import com.sawacorp.displaysharepro.feature.connectToBroadcast.database.AppDatabase
import com.sawacorp.displaysharepro.feature.connectToBroadcast.repository.ConnectRepository
import com.sawacorp.displaysharepro.feature.connectToBroadcast.useCase.ConnectUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class ConnectModule {

    @Provides
    fun providesConnectUseCase(repository: ConnectRepository) = ConnectUseCase(repository)

    @Provides
    fun providesConnectRepository(
        @ApplicationContext
        context: Context,
        database: AppDatabase
    ) = ConnectRepository(context,database)

    @Provides
    fun providesAppDatabase(
        @ApplicationContext
        application: Context
    ) = AppDatabase.getDatabase(application)

}