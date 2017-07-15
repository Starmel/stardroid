package com.google.android.stardroid.util

import com.google.android.stardroid.units.GeocentricCoordinates
import java.util.*

object SatCalculator {


    val toRad = 2.0 * Math.PI / 360.0
    val toDeg = 360.0 / (2.0 * Math.PI)
    val Earth_equatorial_radius = 6378.135


    fun calculatePosition(tleParams: TLEParams, date: Date? = null): GeocentricCoordinates {
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        cal.add(Calendar.HOUR_OF_DAY, -3)
        val Epoch_now = daynumber(cal.get(Calendar.DAY_OF_MONTH).toDouble(),
                cal.get(Calendar.MONTH).toDouble() + 1,
                cal.get(Calendar.YEAR).toDouble(),
                cal.get(Calendar.HOUR_OF_DAY).toDouble(),
                cal.get(Calendar.MINUTE).toDouble(),
                cal.get(Calendar.SECOND).toDouble())
        val e = tleParams.eccentricity

        val M = tleParams.meananomaly + (360 * (tleParams.satelliteRevSiderealDay * (Epoch_now - tleParams.epochStart) + 0.5 * tleParams.firstDerativeMeanMotion * (Epoch_now - tleParams.epochStart) * (Epoch_now - tleParams.epochStart)))

        val E = toDeg * (M * toRad + e * Math.sin(M * toRad) + 0.5 * e * e * Math.sin(2 * M * toRad))

        val TCdecimal = (1440.0 / ((tleParams.satelliteRevSiderealDay) + (tleParams.firstDerativeMeanMotion * (Epoch_now - tleParams.epochStart)))) / 60.0
        val RangeA = Math.pow((6028.9 * (TCdecimal * 60)), (2.0 / 3.0))

        val apogee = RangeA * (1 - tleParams.eccentricity)
        val perigee = (RangeA * (1 + tleParams.eccentricity) + apogee) / 2

        val X0 = perigee * (Math.cos(toRad * E) - e)
        val Y0 = perigee * Math.sqrt((1 - e * e)) * Math.sin(toRad * E)

        var arg_per = tleParams.argumentOfPerigee
        val RAAN = tleParams.raAn
        val i = tleParams.inclination

        val perigee_perturbation = (Epoch_now - tleParams.epochStart) * 4.97 * Math.pow((Earth_equatorial_radius / (perigee)), 3.5) * (5 * Math.cos(toRad * i) * Math.cos(toRad * i) - 1) / ((1 - e * e) * (1 - e * e))

        arg_per = (arg_per + perigee_perturbation)


        val Px = Math.cos(toRad * arg_per) * Math.cos(RAAN * toRad) - Math.sin(toRad * arg_per) * Math.sin(toRad * RAAN) * Math.cos(toRad * i)
        val Py = Math.cos(toRad * arg_per) * Math.sin(RAAN * toRad) + Math.sin(toRad * arg_per) * Math.cos(toRad * RAAN) * Math.cos(toRad * i)
        val Pz = Math.sin(toRad * arg_per) * Math.sin(toRad * i)

        //alert(toDeg*Math.atan2(Py,Math.sqrt(Px*Px+Py*Py)))

        val Qx = -Math.sin(toRad * arg_per) * Math.cos(RAAN * toRad) - Math.cos(toRad * arg_per) * Math.sin(toRad * RAAN) * Math.cos(toRad * i)
        val Qy = -Math.sin(toRad * arg_per) * Math.sin(RAAN * toRad) + Math.cos(toRad * arg_per) * Math.cos(toRad * RAAN) * Math.cos(toRad * i)
        val Qz = Math.cos(toRad * arg_per) * Math.sin(toRad * i)

        val x = Px * X0 + Qx * Y0
        val y = Py * X0 + Qy * Y0
        val z = Pz * X0 + Qz * Y0

        val Declination = toDeg * Math.atan2(z, Math.sqrt(x * x + y * y))
        val RA = (Rev(toDeg * Math.atan2(y, x)))
        return GeocentricCoordinates.getInstance(RA.toFloat(), Declination.toFloat())
    }

    fun getTLEInformation(tleString: String): TLEParams? {
        val rows = tleString.split("\n")
        if ((rows[1].substring(0, 1) != "1") || (rows[2].substring(0, 1) != "2"))
            println("error in parameter")
        else {
            val epoch = rows[1].substring(19 - 1, 32).toDouble()
            val epochYear = ("20" + rows[1].substring(19 - 1, 20)).toDouble()
            val epochDay = rows[1].substring(21 - 1, 32).toDouble()
            val firstDerativeMeanMotion = rows[1].substring(34 - 1, 43).toDouble()
            val inclination = rows[2].substring(9 - 1, 16).toDouble()
            val raAn = rows[2].substring(18 - 1, 25).toDouble()
            val eccentricity = ("0." + rows[2].substring(27 - 1, 33)).toDouble()
            val argumentOfPerigee = rows[2].substring(35 - 1, 42).toDouble()
            val meananomaly = rows[2].substring(44 - 1, 51).toDouble()
            val satelliteRevSiderealDay = rows[2].substring(53 - 1, 63).toDouble()
            val epochStart = daynumber_tle(epochYear, epochDay)
            return TLEParams(epoch, epochYear, epochDay, firstDerativeMeanMotion, inclination, raAn, eccentricity, argumentOfPerigee, meananomaly, satelliteRevSiderealDay, epochStart)
        }
        return null
    }

    data class TLEParams(val epoch: Double,
                         val epochYear: Double,
                         val epochDay: Double,
                         val firstDerativeMeanMotion: Double,
                         val inclination: Double,
                         val raAn: Double,
                         val eccentricity: Double,
                         val argumentOfPerigee: Double,
                         val meananomaly: Double,
                         val satelliteRevSiderealDay: Double,
                         val epochStart: Double) {
        override fun toString(): String {
            return "TLEParams(epoch=$epoch, epochYear=$epochYear, epochDay=$epochDay, firstDerativeMeanMotion=$firstDerativeMeanMotion, inclination=$inclination, raAn=$raAn, eccentricity='$eccentricity', argumentOfPerigee=$argumentOfPerigee, meananomaly=$meananomaly, satelliteRevSiderealDay=$satelliteRevSiderealDay, epochStart=$epochStart)"
        }
    }

    fun daynumber_tle(Year: Double, Day: Double): Double {
        return daynumber(1.0, 1.0, Year, 0.0, 0.0, 0.0) + Day - 1

    }

    fun daynumber(dd: Double, mm: Double, yyyy: Double, hh: Double, min: Double, sec: Double): Double {
        var d = 367 * yyyy - Div((7 * (yyyy + (Div((mm + 9), 12.0)))), 4.0) + Div((275 * mm), 9.0) + dd - 730530

        d += hh / 24 + min / (60 * 24) + sec / (24 * 60 * 60)
        return d
    }

    fun Div(a: Double, b: Double): Double {
        return ((a - a % b) / b)
    }

    fun Rev(number: Double): Double {
        var x = number

        if (x > 0.0) {
            while (x > 360.0)
                x -= 360.0
        } else {
            while (x < 0.0)
                x += 360.0
        }
        return x
    }


}