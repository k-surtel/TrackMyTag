package com.ks.trackmytag.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ks.trackmytag.data.Device

@Dao
interface DevicesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device)

    @Delete
    suspend fun deleteDevice(device: Device)

    @Query("SELECT * FROM devices")
    fun getAllDevices(): LiveData<List<Device>>

}