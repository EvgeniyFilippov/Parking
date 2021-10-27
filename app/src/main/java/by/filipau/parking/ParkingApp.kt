package by.filipau.parking

import android.app.Application
import timber.log.Timber

open class ParkingApp : Application(){

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, "!@#$tag", message, t)
                }
            })
        }
    }

}