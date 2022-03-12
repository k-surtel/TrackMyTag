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


    companion object {
        @Volatile
        private var INSTANCE: DevicesDatabase? = null

        fun getInstance(context: Context): DevicesDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if(instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, DevicesDatabase::class.java, "devices_database")
                        .fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}