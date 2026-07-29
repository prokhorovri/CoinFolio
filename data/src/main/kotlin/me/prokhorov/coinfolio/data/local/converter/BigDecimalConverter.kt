package me.prokhorov.coinfolio.data.local.converter

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverter {
    @TypeConverter
    fun fromString(value: String?): BigDecimal? = value?.let { BigDecimal(it) }

    @TypeConverter
    fun toString(value: BigDecimal?): String? = value?.toPlainString()
}
