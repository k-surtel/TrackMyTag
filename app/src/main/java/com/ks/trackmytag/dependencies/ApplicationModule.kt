package com.ks.trackmytag.dependencies

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.ks.trackmytag.bluetooth.BleManager
import com.ks.trackmytag.data.DeviceRepository
import com.ks.trackmytag.data.DeviceRepositoryImpl
import com.ks.trackmytag.data.database.DevicesDao
import com.ks.trackmytag.data.database.DevicesDatabase
import com.ks.trackmytag.data.preferences.PreferencesManager
import com.ks.trackmytag.dataStore
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
    fun provideDevicesDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, DevicesDatabase::class.java, "devices_database").build()

    @Provides
    @Singleton
    fun provideDevicesDao(devicesDatabase: DevicesDatabase) = devicesDatabase.devicesDao

    @Provides
    @Singleton
    fun provideBleManager(@ApplicationContext context: Context) = BleManager(context)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun providePreferencesManager(dataStore: DataStore<Preferences>): PreferencesManager {
        return PreferencesManager(dataStore)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        bleManager: BleManager,
        devicesDao: DevicesDao,
        preferencesManager: PreferencesManager
    ): DeviceRepository {
        return DeviceRepositoryImpl(bleManager, devicesDao, preferencesManager)
    }
}