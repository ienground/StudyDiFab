package net.ienlab.study.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.databinding.DataBindingUtil
import net.ienlab.study.R
import net.ienlab.study.adapter.MainDataAdapter
import net.ienlab.study.data.TimeData
import net.ienlab.study.database.DBHelper
import net.ienlab.study.databinding.ActivityMainBinding

val TAG = "StudyTAG"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var dbHelper: DBHelper

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
        binding.recyclerView.adapter = MainDataAdapter(data as ArrayList<TimeData>)




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

