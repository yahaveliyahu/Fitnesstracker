package dev.yahaveliyahu.common.data

import androidx.room.TypeConverter

class SportTypeConverter {
    @TypeConverter
    fun fromSportType(value: SportType): String = value.name

    @TypeConverter
    fun toSportType(value: String): SportType = SportType.valueOf(value)
}
