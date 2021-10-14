package by.filipau.parking.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ParkingService : Service() {

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
        return super.onStartCommand(intent, flags, startId)
    }
}