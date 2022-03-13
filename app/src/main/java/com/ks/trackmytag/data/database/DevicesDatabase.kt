package com.ks.trackmytag.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ks.trackmytag.data.Device

@Database(entities = [Device::class], version = 1, exportSchema = false)
abstract class DevicesDatabase : RoomDatabase() {

    abstract val devicesDao: DevicesDao

}