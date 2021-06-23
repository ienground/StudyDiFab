package net.ienlab.study.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.util.Log
import java.util.*

class DeviceBTService(private val mActivity: Activity) {
    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    var mDevices: Set<BluetoothDevice>? = null

    /** * Check the Bluetooth support * @return boolean  */
    val deviceState: Boolean
        get() {
            Log.i(TAG, "Check the Bluetooth support")
            return if (btAdapter == null) {
                Log.d(TAG, "Bluetooth is not available")
                false
            } else {
                Log.d(TAG, "Bluetooth is available")
                true
            }
        }

    /** * Check the enabled Bluetooth  */
    fun enableBluetooth() {
        Log.i(TAG, "Check the enabled Bluetooth")
        if (btAdapter!!.isEnabled) {
//                기기의 블루투스 상태가 On인 경우
            Log.d(TAG, "Bluetooth Enable Now")
            //                Next Step
        } else {
//                기기의 블루투스 상태가 Off인 경우
            Log.d(TAG, "Bluetooth Enable Request")
            val i = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT)
        }
    }

    fun BluetoothDevice(): ArrayList<String> {
        val listItems: MutableList<String> = ArrayList()
        mDevices = btAdapter!!.bondedDevices
        for (device in mDevices!!) {
            listItems.add(device.name)
        }
        return ArrayList(listItems)
    }

    companion object {
        //    Debugging
        private const val TAG = "BluetoothService"

        //Intent request code
        private const val REQUEST_CONNECT_DEVICE = 1
        private const val REQUEST_ENABLE_BT = 2
    }

    //    Constructors
    init {
        //        BluetoothAdapter 얻기
    }
}