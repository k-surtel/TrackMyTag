package com.ks.trackmytag.data.database

import androidx.room.*
import com.ks.trackmytag.data.Device
import kotlinx.coroutines.flow.Flow

@Dao
interface DevicesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device)

    @Delete
    suspend fun deleteDevice(device: Device)

    @Query("SELECT * FROM devices")
    fun getSavedDevices(): Flow<List<Device>>

}