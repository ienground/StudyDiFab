package net.ienlab.study.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.ienlab.study.activity.TAG
import java.io.UnsupportedEncodingException

class BluetoothHandler: Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        try {
//            Log.d(TAG, msg.obj as String)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }
}