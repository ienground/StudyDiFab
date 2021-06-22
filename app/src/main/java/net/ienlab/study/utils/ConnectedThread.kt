package net.ienlab.study.utils

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import net.ienlab.study.activity.TAG
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ConnectedThread(var socket: BluetoothSocket, var context: Context) : Thread() {
    var inputStream: InputStream?
    var outputStream: OutputStream?
    var handler: Handler?
    val strDelimiter = "\n"
    val charDelimiter = '\n'

    init {
        var tempInputStream: InputStream? = null
        var tempOutputStream: OutputStream? = null
        try {
            tempInputStream = socket.inputStream
            tempOutputStream = socket.outputStream
        } catch (e: IOException) {}

        inputStream = tempInputStream
        outputStream = tempOutputStream
        handler = BluetoothHandler()
    }

    override fun run() {
        var buffer: ByteArray
//        var bytes: Int

        if (inputStream == null || outputStream == null) {
            return
        }

        val readBuffer = ByteArray(1024)
        var readBufferPosition = 0

        while (true) {
            try {
//                bytes = inputStream!!.available()
                val bytesAvailable = inputStream?.available() ?: -1
                if (bytesAvailable > 0) {
                    val packetBytes = ByteArray(bytesAvailable)
                    inputStream!!.read(packetBytes)

                    for (data in packetBytes) {
                        if (data.toChar() == charDelimiter) {
                            val encodedBytes = ByteArray(readBufferPosition)
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.size)

                            val str = String(encodedBytes, Charsets.UTF_8)
                            readBufferPosition = 0

                            handler!!.obtainMessage(0, str).sendToTarget()

                        } else {
                            readBuffer[readBufferPosition++] = data
                        }
                    }
                }
//                if (bytes != 0) {
//                    buffer = ByteArray(1024)
//                    SystemClock.sleep(100)
//                    bytes = inputStream!!.available()
//                    bytes = inputStream!!.read(buffer, 0, bytes)
//                    handler!!.obtainMessage(0, bytes, -1, buffer).sendToTarget()
//
//                }

            } catch (e: IOException) {
                return
            }
        }
    }

    fun write(data: String) {
        val bytes = data.toByteArray()
        if (outputStream == null) return
        try {
            outputStream!!.write(bytes)
        } catch (e: IOException) {}
    }

    fun cancel() {
        try {
            socket.close()
        } catch (e: IOException) {}
    }





}