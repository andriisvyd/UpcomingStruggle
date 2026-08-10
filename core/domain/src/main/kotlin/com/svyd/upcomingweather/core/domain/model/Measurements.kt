package com.svyd.upcomingweather.core.domain.model

/**
 * The quantities a forecast is made of, each carrying its unit in the type.
 *
 * A bare `Double` cannot say whether it is Celsius or Fahrenheit, and a bare `Int` cannot say
 * whether 40 is a humidity or a temperature. Conversion belongs to whoever reads the response; past
 * that boundary the unit is fixed and stated here.
 */
@JvmInline
value class Temperature(val celsius: Double) : Comparable<Temperature> {
    override fun compareTo(other: Temperature): Int = celsius.compareTo(other.celsius)
}

@JvmInline
value class Percentage(val value: Int)

@JvmInline
value class Speed(val kilometresPerHour: Double)

@JvmInline
value class Pressure(val hectopascals: Double)

@JvmInline
value class Millimetres(val value: Double)
