package com.ks.trackmytag.dependency

import android.content.Context
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.data.DeviceRepository
import com.ks.trackmytag.data.DeviceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideBleManager(@ApplicationContext context: Context): BleManager {
        return BleManager(context)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(bleManager: BleManager): DeviceRepository {
        return DeviceRepositoryImpl(bleManager)
    }
}