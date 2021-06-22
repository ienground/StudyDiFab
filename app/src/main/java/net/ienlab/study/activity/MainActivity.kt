package net.ienlab.study.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.ienlab.study.R
import net.ienlab.study.adapter.MainDataAdapter
import net.ienlab.study.data.TimeData
import net.ienlab.study.database.DBHelper
import net.ienlab.study.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList

val TAG = "StudyTAG"
val TILT_NEW_VALUE = "tiltNewValue"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var dbHelper: DBHelper

    var time = 0
    var isTimerOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)

        val data = dbHelper.getAllData() as ArrayList
        data.add(TimeData(0, TimeData.TYPE_SNOOZE, System.currentTimeMillis() - 12 * 60 * 1000))
        data.add(TimeData(1, TimeData.TYPE_STUDY_TIME, System.currentTimeMillis() - 6 * 60 * 1000))
        data.add(TimeData(2, TimeData.TYPE_SNOOZE, System.currentTimeMillis()))

        data.sortByDescending { it.dateTime }
        binding.recyclerView.adapter = MainDataAdapter(data)

        val timer = Timer()
        val timerTask = object: TimerTask() {
            override fun run() {
                // 깜빡 거리는 거 해야 함
                runOnUiThread {
                    if (time >= 3600) {
                        binding.time.text = String.format("%02d:%02d:%02d", time / 3600, (time % 3600) / 60, time % 60)
                    } else {
                        binding.time.text = String.format("%02d:%02d", time / 60, time % 60)
                    }
                }

                if (isTimerOn) {
                    time++
                }
            }
        }

        timer.schedule(timerTask, 0,1000)

        LocalBroadcastManager.getInstance(this).registerReceiver(object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                TODO("Not yet implemented")
            }
        }, IntentFilter())

        binding.time.setOnClickListener {
            isTimerOn = !isTimerOn
            if (isTimerOn) {
                time = 0
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

