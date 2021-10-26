package by.filipau.parking.ui.handler

import android.os.*
import android.os.HandlerThread

class MyWorkerThread(name: String) : HandlerThread(name) {
    private var mWorkerHandler: Handler? = null
    fun postTask(task: Runnable?) {
        task?.let { mWorkerHandler?.post(it) }
    }

    fun prepareHandler() {
        mWorkerHandler = Handler(looper)
    }
}