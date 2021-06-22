package net.ienlab.study.activity

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kotlinx.coroutines.*
import net.ienlab.study.*
import net.ienlab.study.constant.SharedKey
import net.ienlab.study.databinding.ActivitySplashBinding
import net.ienlab.study.utils.ConnectedThread
import java.io.IOException
import java.util.*

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding
    lateinit var nm: NotificationManager

    // 로딩 화면이 떠있는 시간(밀리초단위)
    private val SPLASH_DISPLAY_LENGTH = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.activity = this

        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val gmSansBold = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        val gmSansMedium = Typeface.createFromAsset(assets, "fonts/gmsans_medium.otf")

        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        val isFirstVisit = sharedPreferences.getBoolean(SharedKey.IS_FIRST_VISIT, true)

        CoroutineScope(Dispatchers.Main).launch {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            val bluetoothDevice: MutableMap<String, String> = mutableMapOf()
            var connectedThread: ConnectedThread?

            if (bluetoothDevice.isNotEmpty()) {
                bluetoothDevice.clear()
            }
            val pairedDevices: Set<BluetoothDevice> = btAdapter.bondedDevices

            if (pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {
                    val deviceName = device.name
                    val deviceAddress = device.address

                    bluetoothDevice[deviceName] = deviceAddress
                }
            }

            val name = "IEN_DUINO"
            val address = bluetoothDevice[name]
            var flag = true
            val device = btAdapter.getRemoteDevice(address)
            val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

            Toast.makeText(applicationContext, "trying to connect ${name}..", Toast.LENGTH_SHORT).show()

            var btSocket: BluetoothSocket? = null

            try {
                withContext(Dispatchers.IO) {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                    btSocket?.connect()
                }

                if (flag) {
                    Toast.makeText(applicationContext, "connected to $name", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "connectedThread start")
                    connectedThread = ConnectedThread(btSocket!!, applicationContext)
                    connectedThread.start()
                }
            } catch (e: IOException) {
                flag = false
                Toast.makeText(applicationContext, "Connection Failed", Toast.LENGTH_SHORT).show()
            }

        }

        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java)
            val welcomeIntent = Intent(this, MainActivity::class.java)
//            val welcomeIntent = Intent(this, OnboardingActivity::class.java)
            if (isFirstVisit) {
                startActivity(welcomeIntent)
            } else {
                startActivity(mainIntent)
            }
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }

}