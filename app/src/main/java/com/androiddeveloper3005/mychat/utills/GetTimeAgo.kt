package com.androiddeveloper3005.mychat.utills

import android.app.Application
import android.content.Context

open class GetTimeAgo: Application() {
    private var SECOND_MILLIS = 1000
    private var MINUTE_MILLIS = 60 * SECOND_MILLIS
    private var HOUR_MILLIS = 60 * MINUTE_MILLIS
    private var DAY_MILLIS = 24 * HOUR_MILLIS
    private var time_ = 1L

    fun getTimeAgo(time:Long, ctx: Context):String {
        this.time_ = time
        if (time < 1000000000000L)
        {
            // if timestamp given in seconds, convert to millis
            time_ *= 1000
        }
        val now = System.currentTimeMillis()
        if (time > now || time <= 0)
        {
            return null.toString()
        }
        // TODO: localize
        val diff = now - time
        if (diff < MINUTE_MILLIS)
        {
            return "just now"
        }
        else if (diff < 2 * MINUTE_MILLIS)
        {
            return "a minute ago"
        }
        else if (diff < 50 * MINUTE_MILLIS)
        {
            return ""+ diff / MINUTE_MILLIS + " minutes ago"
        }
        else if (diff < 90 * MINUTE_MILLIS)
        {
            return "an hour ago"
        }
        else if (diff < 24 * HOUR_MILLIS)
        {
            return ""+ diff / HOUR_MILLIS + " hours ago"
        }
        else if (diff < 48 * HOUR_MILLIS)
        {
            return "yesterday"
        }
        else
        {
            return ""+ diff / DAY_MILLIS + " days ago"
        }
    }

}