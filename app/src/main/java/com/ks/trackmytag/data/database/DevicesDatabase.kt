package com.ks.trackmytag.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ks.trackmytag.data.Device

@Database(entities = [Device::class], version = 2, exportSchema = false)
abstract class DevicesDatabase : RoomDatabase() {

    abstract val devicesDao: DevicesDao

}