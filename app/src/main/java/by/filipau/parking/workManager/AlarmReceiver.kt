package by.filipau.parking.workManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat.startActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        Log.e("!@#", "AlarmManager message. Thread: ${Thread.currentThread().name}")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:124"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intent)
    }
}