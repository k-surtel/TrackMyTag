package com.ks.trackmytag.data.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.ks.trackmytag.data.Device
import com.ks.trackmytag.data.State
import com.ks.trackmytag.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DevicesDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DevicesDatabase
    private lateinit var devicesDao: DevicesDao

    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DevicesDatabase::class.java).build()

        devicesDao = database.devicesDao
    }

    @After fun after() { database.close() }


    @Test
    fun insertDevice() = runTest {
        val device = Device(1, "device1", "address1")
        devicesDao.insertDevice(device)

        val allDevices = devicesDao.getAllDevices().getOrAwaitValue()
        assertThat(allDevices).contains(device)
    }

    @Test
    fun insertDeviceIgnoreFields() = runTest {
        val device = Device(1, "device1", "address1")
        device.state = State.DISCONNECTED
        devicesDao.insertDevice(device)

        val allDevices = devicesDao.getAllDevices().getOrAwaitValue()
        assertThat(allDevices).contains(device)
    }

    @Test
    fun updateDevice() = runTest {
        val device = Device(1, "device1", "address1")
        devicesDao.insertDevice(device)
        val updatedDevice = Device(1, "device_updated", "address1")
        devicesDao.insertDevice(updatedDevice)

        val allDevices = devicesDao.getAllDevices().getOrAwaitValue()
        assertThat(allDevices).doesNotContain(device)
        assertThat(allDevices).contains(updatedDevice)
    }

    @Test
    fun deleteDevice() = runTest {
        val device = Device(1, "device1", "address1")
        devicesDao.insertDevice(device)
        devicesDao.deleteDevice(device)

        val allDevices = devicesDao.getAllDevices().getOrAwaitValue()
        assertThat(allDevices).doesNotContain(device)
    }
}