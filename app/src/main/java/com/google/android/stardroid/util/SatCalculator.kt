package com.google.android.stardroid.util

import java.util.*

object SatCalculator {


    val toRad = 2.0 * Math.PI / 360.0
    val toDeg = 360.0 / (2.0 * Math.PI)
    val Earth_equatorial_radius = 6378.135


    fun calculatePosition(tleParams: TLEParams, date: Date? = null): Position {
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

        val Qx = -Math.sin(toRad * arg_per) * Math.cos(RAAN * toRad) - Math.cos(toRad * arg_per) * Math.sin(toRad * RAAN) * Math.cos(toRad * i)
        val Qy = -Math.sin(toRad * arg_per) * Math.sin(RAAN * toRad) + Math.cos(toRad * arg_per) * Math.cos(toRad * RAAN) * Math.cos(toRad * i)
        val Qz = Math.cos(toRad * arg_per) * Math.sin(toRad * i)

        val x = Px * X0 + Qx * Y0
        val y = Py * X0 + Qy * Y0
        val z = Pz * X0 + Qz * Y0

        val Declination = toDeg * Math.atan2(z, Math.sqrt(x * x + y * y))
        val RA = (Rev(toDeg * Math.atan2(y, x)))


        var longitude = Rev(toDeg * Math.atan2(y, x) - getMST(cal, 0.0))
        var latitude = toDeg * Math.atan2(z, Math.sqrt(x * x + y * y))
        if (longitude > 180)
            longitude = 360 - longitude

        if (latitude < 0)
            latitude = -latitude

        val alt = Math.sqrt(X0 * X0 + Y0 * Y0) - Earth_equatorial_radius


        val velocity = Math.sqrt(398600.5 / Math.sqrt(X0 * X0 + Y0 * Y0))

        return Position(RA.toFloat(), Declination.toFloat(), latitude, longitude, alt, velocity)
    }

    fun distance(userLat: Double, userLon: Double, satLat: Double, satLon: Double, satAlt: Double): Double {
        val theta = userLon - satLon
        var dist = Math.sin(deg2rad(userLat)) * Math.sin(deg2rad(satLat)) + Math.cos(deg2rad(userLat)) * Math.cos(deg2rad(satLat)) * Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist *= 60.0 * 1.1515 * 1.609344
        return Math.sqrt(Math.pow(dist, 2.0) + Math.pow(satAlt, 2.0))
    }

    private inline fun deg2rad(deg: Double) = deg * Math.PI / 180.0

    private inline fun rad2deg(rad: Double) = rad * 180 / Math.PI

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

    data class Position(val ra: Float, val dec: Float, val lat: Double, val lng: Double, val alt: Double, val velocity: Double)

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

    fun getMST(now: Calendar, lon: Double): Double {
        val day = now.get(Calendar.DAY_OF_MONTH).toDouble()
        var month = now.get(Calendar.MONTH).toDouble() + 1
        var year = now.get(Calendar.YEAR).toDouble()
        val hour = now.get(Calendar.HOUR_OF_DAY).toDouble()
        val minute = now.get(Calendar.MINUTE).toDouble()
        val second = now.get(Calendar.SECOND).toDouble()

        if (month == 1.0 || month == 2.0) {
            year -= 1
            month += 12
        }

        val a = Math.floor(year / 100);
        val b = 2 - a + Math.floor(a / 4);

        val c = Math.floor(365.25 * year);
        val d = Math.floor(30.6001 * (month + 1));

        // days since J2000.0
        val jd = b + c + d - 730550.5 + day + (hour + minute / 60.0 + second / 3600.0) / 24.0

        val jt = (jd) / 36525.0
        // julian centuries since J2000.0
        var GMST = 280.46061837 + 360.98564736629 * jd + 0.000387933 * jt * jt - jt * jt * jt / 38710000 + lon
        if (GMST > 0.0) {
            while (GMST > 360.0)
                GMST -= 360.0
        } else {
            while (GMST < 0.0)
                GMST += 360.0
        }

        return GMST
    }

}