package com.jb.leitnerbox.di

import com.jb.leitnerbox.core.domain.usecase.settings.RescheduleNotificationUseCase
import com.jb.leitnerbox.worker.RescheduleNotificationUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    @Binds
    @Singleton
    abstract fun bindRescheduleNotificationUseCase(
        impl: RescheduleNotificationUseCaseImpl
    ): RescheduleNotificationUseCase
}
