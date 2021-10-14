package by.filipau.parking.workManager

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import by.filipau.parking.R
import by.filipau.parking.ui.StartFragment

class ParkingWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.e("!@#", "Worker done. Thread: " + Thread.currentThread().name)
        return Result.success()
    }
}

