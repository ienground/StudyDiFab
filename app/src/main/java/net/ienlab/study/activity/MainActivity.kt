package net.ienlab.study.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.*
import android.graphics.Typeface
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
import net.ienlab.study.receiver.AlarmReceiver
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
    lateinit var am: AlarmManager

    lateinit var gmSansBold: Typeface
    lateinit var gmSansMedium: Typeface

    var time = 0L
    var todayTime = 0L
    var isTimerOn = false
    var startAngle = -999
    var count = 0

    var connectedThread: ConnectedThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        gmSansMedium = Typeface.createFromAsset(assets, "fonts/gmsans_medium.otf")
        gmSansBold = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")

        binding.time.typeface = gmSansBold
        binding.todayTime.typeface = gmSansMedium

        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val data = dbHelper.getAllData() as ArrayList
        data.sortBy { it.dateTime }
        for (value in data) {
            val date = Calendar.getInstance().apply {
                timeInMillis = value.dateTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (c.timeInMillis == date.timeInMillis) {
                todayTime += value.studyTime
            }
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

        val lastCalendar = Calendar.getInstance().apply { timeInMillis = 0 }
        for (i in data.lastIndex downTo 0) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = data[i].dateTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (lastCalendar.timeInMillis == 0L) lastCalendar.timeInMillis = calendar.timeInMillis
            if (lastCalendar.timeInMillis != calendar.timeInMillis || i == data.lastIndex) {
                data.add(i + 1, TimeData(-1, -1, calendar.timeInMillis, 0, TimeData.VIEWTYPE_DATE))
                lastCalendar.timeInMillis = calendar.timeInMillis
            }
        }

        data.reverse()

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

                        if (abs(gap) >= sharedPreferences.getInt(SharedKey.NECK_ANGLE, 15)) {
                            count++
                        }
                    }

                    if (count >= 10 * 4 && isTimerOn) { // 4초동안 졸았다, 알림 예약
                        // 시간 기록 종료
                        if (connectedThread != null) {
                            connectedThread?.write("x")
                        }

                        isTimerOn = false

                        val item = TimeData(-1, TimeData.TYPE_STUDY_TIME, System.currentTimeMillis() - 1000, time, TimeData.VIEWTYPE_NOTI)
                        val sleepItem = TimeData(-1, TimeData.TYPE_SNOOZE, System.currentTimeMillis(), -1, TimeData.VIEWTYPE_NOTI)
                        adapter.addItem(item, dbHelper)
                        adapter.addItem(sleepItem, dbHelper)
                        binding.recyclerView.scrollToPosition(0)

                        if (sharedPreferences.getBoolean(SharedKey.ALARM_WHEN_SNOOZE, true)) {
                            val alarmIntent = Intent(applicationContext, AlarmReceiver::class.java)
                            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_HOUR, PendingIntent.getBroadcast(applicationContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                        }
                    }

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

                    if (sharedPreferences.getBoolean(SharedKey.PIEZO, true)) {
                        connectedThread?.write("p")
                    }
                }

                val item = TimeData(-1, TimeData.TYPE_STUDY_TIME, System.currentTimeMillis() - 1000, time, TimeData.VIEWTYPE_NOTI)
                adapter.addItem(item, dbHelper)
                binding.recyclerView.scrollToPosition(0)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

