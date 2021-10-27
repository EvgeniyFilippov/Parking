package by.filipau.parking.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import by.filipau.parking.R
import by.filipau.parking.ui.StartFragment

class ParkingService : Service() {

    companion object {
        const val CHANNEL_ID = "444"
        const val ONGOING_NOTIFICATION_ID = 333
    }

    /** The system calls this method when another component wants to bind with the service by calling bindService().
    If you implement this method, you must provide an interface that clients use to communicate
    with the service, by returning an IBinder object. You must always implement this method,
    but if you don't want to allow binding, then you should return null. */
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /* Система вызовет этот метод, когда другой компонент, такой как activity, запросит запуск службы
     вызовом startService(). Как только этот метод завершится, служба будет запущена, и может
     бесконечно работать в фоне. Если Вы реализовали этот метод, то в Вашей ответственности
     остановить службу, когда она завершит работу, путем вызова stopSelf() или stopService()
     (если Вы решили использовать привязку - binding, то Вам не нужно реализовывать этот метод).*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val pendingIntent: PendingIntent =
            Intent(this, StartFragment::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel name"
            val importance = NotificationManager.IMPORTANCE_MIN
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, importance)

            val notificationManager = getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("title")
            .setContentText("message")
            .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
            .setContentIntent(pendingIntent)
            .setTicker("Ticker")
            .build()

// Notification ID cannot be 0.
        startForeground(ONGOING_NOTIFICATION_ID, notification)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killSelf() {
//        stopListening()
        stopForeground(true)
        stopSelf()
    }

    //    private fun stopListening() {
//        if (mLocationManager != null) {
//            mLocationManager?.let { manager ->
//                applicationContext?.let {
//                    manager.removeUpdates(this@LocationTrackingService)
//                }
//            }
//        }
//    }

    //kill WITH lifecycle app
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        killSelf()
    }

}