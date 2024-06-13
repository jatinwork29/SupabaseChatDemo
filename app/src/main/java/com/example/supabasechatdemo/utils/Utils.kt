package com.example.supabasechatdemo.utils

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.ViewGroup
import com.example.supabasechatdemo.R
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

open class Utils {

    companion object {
        private var loaderDialog: Dialog? = null

        var isOnline = false

        fun initLoader(context: Context){
            loaderDialog = Dialog(context)
            loaderDialog?.setContentView(R.layout.dialog_loader)
            loaderDialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            loaderDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            loaderDialog?.setCancelable(false)
        }

        fun startLoader(){
            stopLoader()
            loaderDialog?.show()
        }

        fun stopLoader(){
            if(loaderDialog != null && loaderDialog!!.isShowing){
                loaderDialog?.dismiss()
            }
        }

        private fun getFormattedDatesForOlderAPIs(): Pair<String, String> {
            // Define the desired date format
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // Get the current date
            val today = Calendar.getInstance()

            // Format today's date
            val formattedToday = dateFormatter.format(today.time)

            // Get yesterday's date
            today.add(Calendar.DATE, -1)
            val formattedYesterday = dateFormatter.format(today.time)

            // Return to original date
            today.add(Calendar.DATE, 1)

            // Return as a pair
            return Pair(formattedToday, formattedYesterday)
        }

        fun formatChange(isoTimestamp: String): String {
            // Parse the timestamp into an OffsetDateTime
            val parsedDateTime = OffsetDateTime.parse(isoTimestamp)
            // Define the desired output format
            val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
            // Format the parsed date-time into the desired format
            return parsedDateTime.atZoneSameInstant(ZoneId.systemDefault()).format(formatter)
        }

        fun checkDate(date: String): String {

            // Return today's and yesterday's date
            val (today, yesterday) = getFormattedDatesForOlderAPIs()

            // checking date if today's or yesterday's
            if (date == today) {
                return "Today"
            } else if (date == yesterday){
                return "Yesterday"
            }

            return date
        }

    }
}