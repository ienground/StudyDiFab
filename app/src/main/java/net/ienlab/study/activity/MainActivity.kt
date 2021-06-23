package net.ienlab.study.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ienlab.study.R
import net.ienlab.study.adapter.MainDataAdapter
import net.ienlab.study.constant.SharedKey
import net.ienlab.study.data.TimeData
import net.ienlab.study.database.DBHelper
import net.ienlab.study.databinding.ActivityMainBinding
import net.ienlab.study.utils.ConnectedThread
import java.io.IOException
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

val TAG = "StudyTAG"
val TILT_NEW_VALUE = "tiltNewValue" // Intent Name
val TILT_VALUE = "tiltValue"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var dbHelper: DBHelper
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>

    var time = 0L
    var todayTime = 0L
    var isTimerOn = false
    val tiltValueArray: ArrayList<Int> = arrayListOf()
    var gapThreshold = 1f
    var beforeTiltValue = -9999
    var beforeTiltAvgValue = -9999f
    var startAngle = -999
    var count = 0
    var gapAngle = 0

    var connectedThread: ConnectedThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        gapAngle = (sharedPreferences.getString(SharedKey.NECK_ANGLE, "15") ?: "15      ").toInt()
//        gapThreshold = sharedPreferences.getInt(SharedKey.GAP_THRESHOLD, 7) / 2f

        settingsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            gapThreshold = sharedPreferences.getInt(SharedKey.GAP_THRESHOLD, 7) / 2f
        }

        val data = dbHelper.getAllData() as ArrayList
        data.sortByDescending { it.dateTime }
        for (value in data) {
            todayTime += value.studyTime
        }

        if (todayTime >= 3600) {
            binding.todayTime.text = String.format("%02d:%02d:%02d", todayTime / 3600, (todayTime % 3600) / 60, todayTime % 60)
        } else {
            binding.todayTime.text = String.format("%02d:%02d", todayTime / 60, todayTime % 60)
        }


        CoroutineScope(Dispatchers.Main).launch {
            val btAdapter = BluetoothAdapter.getDefaultAdapter()
            val bluetoothDevice: MutableMap<String, String> = mutableMapOf()

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

            val name = sharedPreferences.getString(SharedKey.CONNECT_DEVICE, "IEN_DUINO")
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
                    connectedThread?.start()
                }
            } catch (e: IOException) {
                flag = false
                Toast.makeText(applicationContext, "Connection Failed", Toast.LENGTH_SHORT).show()
            }

        }

        val adapter = MainDataAdapter(data)
        binding.recyclerView.adapter = adapter

        val timer = Timer()
        val timerTask = object: TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (time >= 3600) {
                        binding.time.text = String.format("%02d:%02d:%02d", time / 3600, (time % 3600) / 60, time % 60)
                    } else {
                        binding.time.text = String.format("%02d:%02d", time / 60, time % 60)
                    }

                    if (todayTime >= 3600) {
                        binding.todayTime.text = String.format("%02d:%02d:%02d", todayTime / 3600, (todayTime % 3600) / 60, todayTime % 60)
                    } else {
                        binding.todayTime.text = String.format("%02d:%02d", todayTime / 60, todayTime % 60)
                    }
                }

                if (isTimerOn) {
                    time++
                    todayTime++
                }
            }
        }

        timer.schedule(timerTask, 0,1000)

        LocalBroadcastManager.getInstance(this).registerReceiver(object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    val value = Integer.parseInt((intent.getStringExtra(TILT_VALUE) ?: "0").split(13.toChar()).first())

                    if (isTimerOn) {
                        if (startAngle == -999) startAngle = value

                        val gap = (abs(startAngle - value) + 180) % 360 - 180

                        if (abs(gap) >= gapAngle) {
                            count++
                        }

                        Log.d(TAG, "current: $value, $gap, $count")
                    } else {
                        Log.d(TAG, "current: $value, off, $count")
                    }





                    if (count >= 10 * 4 && isTimerOn) {
                        // 시간 기록 종료
                        if (connectedThread != null) {
                            connectedThread?.write("x")
                        }

                        isTimerOn = false

                        val item = TimeData(-1, TimeData.TYPE_STUDY_TIME, System.currentTimeMillis() - 1000, time)
                        val sleepItem = TimeData(-1, TimeData.TYPE_SNOOZE, System.currentTimeMillis(), -1)
                        adapter.addItem(item, dbHelper)
                        adapter.addItem(sleepItem, dbHelper)
                        binding.recyclerView.scrollToPosition(0)
                    }

//                    beforeTiltValue = value
//                    tiltValueArray.add(gap)

//                    val avg = if (tiltValueArray.size >= 10) {
//                        var sum = 0
//                        for (i in 0 until 10) {
//                            sum += tiltValueArray[tiltValueArray.lastIndex - i]
//                        }
//                        sum / 20f
//                    } else {
//                        var sum = 0
//                        for (v in tiltValueArray) {
//                            sum += v
//                        }
//                        sum / tiltValueArray.size.toFloat()
//                    }

//                    val avgGap = if (beforeTiltAvgValue != -9999f) abs(avg - beforeTiltAvgValue) else 0f
//                    Log.d(TAG, "current: $value, $avg, $beforeTiltAvgValue, $avgGap")
//                    beforeTiltAvgValue = avg

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }, IntentFilter(TILT_NEW_VALUE))

        binding.time.setOnClickListener {
            if (!isTimerOn) {
                isTimerOn = true
                time = 0
                count = 0
                startAngle = -999

                if (connectedThread != null) {
                    connectedThread?.write("o")
                }
            } else {
                isTimerOn = false

                if (connectedThread != null) {
                    connectedThread?.write("x")
                }

                val item = TimeData(-1, TimeData.TYPE_STUDY_TIME, System.currentTimeMillis() - 1000, time)
                val sleepItem = TimeData(-1, TimeData.TYPE_SNOOZE, System.currentTimeMillis(), -1)
                adapter.addItem(item, dbHelper)
                adapter.addItem(sleepItem, dbHelper)
                binding.recyclerView.scrollToPosition(0)
            }
        }

        binding.time.setOnLongClickListener {
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                settingsActivityLauncher.launch(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

