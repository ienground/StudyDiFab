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