package by.filipau.parking.utils

import android.app.Activity
import android.app.AlertDialog
import by.filipau.parking.R

fun Activity.showAlertDialogWithMessage(message: String) {
    val alertDialog = AlertDialog.Builder(this)
        .setTitle(getString(R.string.alert_title))
        .setMessage(message)
        .setPositiveButton(getString(R.string.OK)) { dialog, _ ->
            dialog.dismiss()
        }
    alertDialog?.show()
}