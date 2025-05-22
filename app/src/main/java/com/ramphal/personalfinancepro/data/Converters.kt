package com.ramphal.personalfinancepro.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? =
        dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(millis: Long?): LocalDateTime? =
        millis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }
}