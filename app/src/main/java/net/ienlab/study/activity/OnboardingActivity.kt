package net.ienlab.study.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import net.ienlab.study.R
import net.ienlab.study.databinding.ActivityOnboardingBinding
import net.ienlab.study.utils.ConnectedThread
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.collections.ArrayList

class OnboardingActivity : AppCompatActivity() {

    lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        binding.activity = this

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        val btArrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1)
        val deviceAddressArray: ArrayList<String> = arrayListOf()
        var pairedDevices: Set<BluetoothDevice>
        var connectedThread: ConnectedThread? = null

        binding.list.adapter = btArrayAdapter

        binding.btnPair.setOnClickListener {
            btArrayAdapter.clear()

            if (deviceAddressArray.isNotEmpty()) {
                deviceAddressArray.clear()
            }
            pairedDevices = btAdapter.bondedDevices

            if (pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {
                    val deviceName = device.name
                    val deviceAddress = device.address

                    btArrayAdapter.add(deviceName)
                    deviceAddressArray.add(deviceAddress)
                }
            }
        }

        binding.list.setOnItemClickListener { parent, view, position, id ->
            val name = btArrayAdapter.getItem(position)
            val address = deviceAddressArray[position]
            var flag = true
            val device = btAdapter.getRemoteDevice(address)
            val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

            binding.tvConnectStatus.text = "trying to connect ${name}.."

            var btSocket: BluetoothSocket? = null
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                btSocket.connect()
            } catch (e: IOException) {
                flag = false
                binding.tvConnectStatus.text = "Connection Failed"
            }

            if (flag) {
                binding.tvConnectStatus.text = "connected to $name"
                Log.d(TAG, "connectedThread start")
                connectedThread = ConnectedThread(btSocket!!, this)
                connectedThread?.start()
            }
        }

        binding.btnSend.setOnClickListener {
            if (connectedThread != null) {
                Log.d(TAG, "Successfully Send")
                connectedThread?.write(binding.etData.text.toString())
            } else {
                Log.d(TAG, "connectedThread null")
            }
        }
    }
}