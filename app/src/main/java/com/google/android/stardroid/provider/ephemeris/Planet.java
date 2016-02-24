// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.stardroid.provider.ephemeris;

import android.util.Log;

import com.google.android.stardroid.R;
import com.google.android.stardroid.base.TimeConstants;
import com.google.android.stardroid.base.VisibleForTesting;
import com.google.android.stardroid.units.GeocentricCoordinates;
import com.google.android.stardroid.units.HeliocentricCoordinates;
import com.google.android.stardroid.units.LatLong;
import com.google.android.stardroid.units.RaDec;
import com.google.android.stardroid.util.Geometry;
import com.google.android.stardroid.util.MathUtil;
import com.google.android.stardroid.util.MiscUtil;
import com.google.android.stardroid.util.TimeUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

// TODO(jontayler): see if we can get more sig figs now we've switched to floats
public enum Planet {
  // The order here is the order in which they are drawn.  To ensure that during
  // conjunctions they display "naturally" order them in reverse distance from Earth.
  Pluto(R.drawable.pluto, R.string.pluto, 1L * TimeConstants.MILLISECONDS_PER_WEEK),
  Neptune(R.drawable.neptune, R.string.neptune, 1L * TimeConstants.MILLISECONDS_PER_WEEK),
  Uranus(R.drawable.uranus, R.string.uranus, 1L * TimeConstants.MILLISECONDS_PER_WEEK),
  Jupiter(R.drawable.jupiter, R.string.jupiter, 1L * TimeConstants.MILLISECONDS_PER_WEEK),
  Saturn(R.drawable.saturn, R.string.saturn, 1L * TimeConstants.MILLISECONDS_PER_WEEK),
  Mars(R.drawable.mars, R.string.mars, 1L * TimeConstants.MILLISECONDS_PER_DAY),
  Sun(R.drawable.sun, R.string.sun, 1L * TimeConstants.MILLISECONDS_PER_DAY),
  Mercury(R.drawable.mercury, R.string.mercury, 1L * TimeConstants.MILLISECONDS_PER_DAY),
  Venus(R.drawable.venus, R.string.venus, 1L * TimeConstants.MILLISECONDS_PER_DAY),
  Moon(R.drawable.moon4, R.string.moon, 1L * TimeConstants.MILLISECONDS_PER_HOUR);

  private static final String TAG = MiscUtil.getTag(Planet.class);

  // Resource ID to use for a planet's image.
  private int imageResourceId;

  // String ID
  private int nameResourceId;

  private final long updateFreqMs;

  Planet(int imageResourceId, int nameResourceId, long updateFreqMs) {
    this.imageResourceId = imageResourceId;
    this.nameResourceId = nameResourceId;
    this.updateFreqMs = updateFreqMs;
    // Add Color, magnitude, etc.
  }

  public long getUpdateFrequencyMs() {
    return updateFreqMs;
  }

  /**
   * Returns the resource id for the string corresponding to the name of this
   * planet.
   */
  public int getNameResourceId() {
    return nameResourceId;
  }

  /** Returns the resource id for the planet's image. */
  public int getImageResourceId(Date time) {
    if (this.equals(Planet.Moon)) {
      return getLunarPhaseImageId(time);
    }
    return this.imageResourceId;
  }

  /**
   * Determine the Moon's phase and return the resource ID of the correct
   * image.
   */
  private int getLunarPhaseImageId(Date time) {
    // First, calculate phase angle:
    double phase = calculatePhaseAngle(time);
    Log.d(TAG, "Lunar phase = " + phase);

    // Next, figure out what resource id to return.
    if (phase < 22.5) {
      // New moon.
      return R.drawable.moon0;
    } else if (phase > 150.0) {
      // Full moon.
      return R.drawable.moon4;
    }

    // Either crescent, quarter, or gibbous. Need to see whether we are
    // waxing or waning. Calculate the phase angle one day in the future.
    // If phase is increasing, we are waxing. If not, we are waning.
    Date tomorrow = new Date(time.getTime() + 24 * 3600 * 1000);
    double phase2 = calculatePhaseAngle(tomorrow);
    Log.d(TAG, "Tomorrow's phase = " + phase2);

    if (phase < 67.5) {
      // Crescent
      return (phase2 > phase) ? R.drawable.moon1 : R.drawable.moon7;
    } else if (phase < 112.5) {
      // Quarter
      return (phase2 > phase) ? R.drawable.moon2 : R.drawable.moon6;
    }

    // Gibbous
    return (phase2 > phase) ? R.drawable.moon3 : R.drawable.moon5;
  }


  // Taken from JPL's Planetary Positions page: http://ssd.jpl.nasa.gov/?planet_pos
  // This gives us a good approximation for the years 1800 to 2050 AD.
  // TODO(serafini): Update the numbers so we can extend the approximation to cover 
  // 3000 BC to 3000 AD.
  public OrbitalElements getOrbitalElements(Date date) {
    // Centuries since J2000
    double jc = (double) TimeUtil.julianCenturies(date);

    switch (this) {
      case Mercury: {
        double a = 0.38709927f + 0.00000037f * jc;
        double e = 0.20563593f + 0.00001906f * jc;
        double i = (7.00497902f - 0.00594749f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((252.25032350f + 149472.67411175f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (77.45779628f + 0.16047689f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = (48.33076593f - 0.12534081f * jc) * MathUtil.DEGREES_TO_RADIANS;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      case Venus: {
        double a = 0.72333566f + 0.00000390f * jc;
        double e = 0.00677672f - 0.00004107f * jc;
        double i = (3.39467605f - 0.00078890f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((181.97909950f + 58517.81538729f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (131.60246718f + 0.00268329f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = (76.67984255f - 0.27769418f * jc) * MathUtil.DEGREES_TO_RADIANS;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      // Note that this is the orbital data for Earth.
      case Sun: {
        double a = 1.00000261f + 0.00000562f * jc;
        double e = 0.01671123f - 0.00004392f * jc;
        double i = (-0.00001531f - 0.01294668f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((100.46457166f + 35999.37244981f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (102.93768193f + 0.32327364f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = 0.0;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      case Mars: {
        double a = 1.52371034f + 0.00001847f * jc;
        double e = 0.09339410f + 0.00007882f * jc;
        double i = (1.84969142f - 0.00813131f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((-4.55343205f + 19140.30268499f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (-23.94362959f + 0.44441088f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = (49.55953891f - 0.29257343f * jc) * MathUtil.DEGREES_TO_RADIANS;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      case Jupiter: {
        double a = 5.20288700f - 0.00011607f * jc;
        double e = 0.04838624f - 0.00013253f * jc;
        double i = (1.30439695f - 0.00183714f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((34.39644051f + 3034.74612775f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (14.72847983f + 0.21252668f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = (100.47390909f + 0.20469106f * jc) * MathUtil.DEGREES_TO_RADIANS;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      case Saturn: {
        double a = 9.53667594f - 0.00125060f * jc;
        double e = 0.05386179f - 0.00050991f * jc;
        double i = (2.48599187f + 0.00193609f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((49.95424423f + 1222.49362201f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (92.59887831f - 0.41897216f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = (113.66242448f - 0.28867794f * jc) * MathUtil.DEGREES_TO_RADIANS;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      case Uranus: {
        double a = 19.18916464f - 0.00196176f * jc;
        double e = 0.04725744f - 0.00004397f * jc;
        double i = (0.77263783f - 0.00242939f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((313.23810451f + 428.48202785f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (170.95427630f + 0.40805281f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = (74.01692503f + 0.04240589f * jc) * MathUtil.DEGREES_TO_RADIANS;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      case Neptune: {
        double a = 30.06992276f + 0.00026291f * jc;
        double e = 0.00859048f + 0.00005105f * jc;
        double i = (1.77004347f + 0.00035372f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((-55.12002969f + 218.45945325f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (44.96476227f - 0.32241464f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = (131.78422574f - 0.00508664f * jc) * MathUtil.DEGREES_TO_RADIANS;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      case Pluto: {
        double a = 39.48211675f - 0.00031596f * jc;
        double e = 0.24882730f + 0.00005170f * jc;
        double i = (17.14001206f + 0.00004818f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double l =
            Geometry.mod2pi((238.92903833f + 145.20780515f * jc) * MathUtil.DEGREES_TO_RADIANS);
        double w = (224.06891629f - 0.04062942f * jc) * MathUtil.DEGREES_TO_RADIANS;
        double o = (110.30393684f - 0.01183482f * jc) * MathUtil.DEGREES_TO_RADIANS;
        return new OrbitalElements(a, e, i, o, w, l);
      }

      default:
        throw new RuntimeException("Unknown Planet: " + this);
    }
  }


  // TODO(serafini): We need to correct the Ra/Dec for the user's location. The
  // current calculation is probably accurate to a degree or two, but we can,
  // and should, do better.
  /**
   * Calculate the geocentric right ascension and declination of the moon using
   * an approximation as described on page D22 of the 2008 Astronomical Almanac
   * All of the variables in this method use the same names as those described
   * in the text: lambda = Ecliptic longitude (degrees) beta = Ecliptic latitude
   * (degrees) pi = horizontal parallax (degrees) r = distance (Earth radii)
   *
   * NOTE: The text does not give a specific time period where the approximation
   * is valid, but it should be valid through at least 2009.
   */
  public static RaDec calculateLunarGeocentricLocation(Date time) {
    // First, calculate the number of Julian centuries from J2000.0.
    double t = (double) ((TimeUtil.calculateJulianDay(time) - 2451545.0) / 36525.0);

    // Second, calculate the approximate geocentric orbital elements.
    double lambda =
        218.32f + 481267.881f * t + 6.29f
            * Math.sin((135.0 + 477198.87f * t) * MathUtil.DEGREES_TO_RADIANS) - 1.27f
            * Math.sin((259.3f - 413335.36f * t) * MathUtil.DEGREES_TO_RADIANS) + 0.66f
            * Math.sin((235.7f + 890534.22f * t) * MathUtil.DEGREES_TO_RADIANS) + 0.21f
            * Math.sin((269.9f + 954397.74f * t) * MathUtil.DEGREES_TO_RADIANS) - 0.19f
            * Math.sin((357.5f + 35999.05f * t) * MathUtil.DEGREES_TO_RADIANS) - 0.11f
            * Math.sin((186.5f + 966404.03f * t) * MathUtil.DEGREES_TO_RADIANS);
    double beta =
        5.13f * Math.sin((93.3f + 483202.02f * t) * MathUtil.DEGREES_TO_RADIANS) + 0.28f
            * Math.sin((228.2f + 960400.89f * t) * MathUtil.DEGREES_TO_RADIANS) - 0.28f
            * Math.sin((318.3f + 6003.15f * t) * MathUtil.DEGREES_TO_RADIANS) - 0.17f
            * Math.sin((217.6f - 407332.21f * t) * MathUtil.DEGREES_TO_RADIANS);
    //double pi =
    //    0.9508f + 0.0518f * Math.cos((135.0 + 477198.87f * t) * MathUtil.DEGREES_TO_RADIANS)
    //        + 0.0095f * Math.cos((259.3f - 413335.36f * t) * MathUtil.DEGREES_TO_RADIANS)
    //        + 0.0078f * Math.cos((235.7f + 890534.22f * t) * MathUtil.DEGREES_TO_RADIANS)
    //        + 0.0028f * Math.cos((269.9f + 954397.74f * t) * MathUtil.DEGREES_TO_RADIANS);
    // double r = 1.0 / Math.sin(pi * MathUtil.DEGREES_TO_RADIANS);

    // Third, convert to RA and Dec.
    double l =
        Math.cos(beta * MathUtil.DEGREES_TO_RADIANS)
            * Math.cos(lambda * MathUtil.DEGREES_TO_RADIANS);
    double m =
        0.9175f * Math.cos(beta * MathUtil.DEGREES_TO_RADIANS)
            * Math.sin(lambda * MathUtil.DEGREES_TO_RADIANS) - 0.3978f
            * Math.sin(beta * MathUtil.DEGREES_TO_RADIANS);
    double n =
        0.3978f * Math.cos(beta * MathUtil.DEGREES_TO_RADIANS)
            * Math.sin(lambda * MathUtil.DEGREES_TO_RADIANS) + 0.9175f
            * Math.sin(beta * MathUtil.DEGREES_TO_RADIANS);
    double ra = Geometry.mod2pi(Math.atan2(m, l)) * MathUtil.RADIANS_TO_DEGREES;
    double dec = Math.asin(n) * MathUtil.RADIANS_TO_DEGREES;

    return new RaDec(ra, dec);
  }

  /**
   * Calculates the phase angle of the planet, in degrees.
   */
  @VisibleForTesting
  double calculatePhaseAngle(Date time) {
    // For the moon, we will approximate phase angle by calculating the
    // elongation of the moon relative to the sun. This is accurate to within
    // about 1%.
    if (this == Planet.Moon) {
      RaDec moonRaDec = calculateLunarGeocentricLocation(time);
      GeocentricCoordinates moon = GeocentricCoordinates.getInstance(moonRaDec);

      HeliocentricCoordinates sunCoords = HeliocentricCoordinates.getInstance(Planet.Sun, time);
      RaDec sunRaDec = RaDec.calculateRaDecDist(sunCoords);
      GeocentricCoordinates sun = GeocentricCoordinates.getInstance(sunRaDec);

      return 180.0 -
          Math.acos(sun.x * moon.x + sun.y * moon.y + sun.z * moon.z)
          * MathUtil.RADIANS_TO_DEGREES;
    }

    // First, determine position in the solar system.
    HeliocentricCoordinates planetCoords = HeliocentricCoordinates.getInstance(this, time);

    // Second, determine position relative to Earth
    HeliocentricCoordinates earthCoords = HeliocentricCoordinates.getInstance(Planet.Sun, time);
    double earthDistance = planetCoords.DistanceFrom(earthCoords);

    // Finally, calculate the phase of the body.
    double phase = Math.acos((earthDistance * earthDistance +
        planetCoords.radius * planetCoords.radius -
        earthCoords.radius * earthCoords.radius) /
        (2.0 * earthDistance * planetCoords.radius)) * MathUtil.RADIANS_TO_DEGREES;

    return phase;
  }

  /**
   * Calculate the percent of the body that is illuminated. The value returned
   * is a fraction in the range from 0.0 to 100.0.
   */
  // TODO(serafini): Do we need this method?
  @VisibleForTesting
  public double calculatePercentIlluminated(Date time) {
    double phaseAngle = this.calculatePhaseAngle(time);
    return 50.0 * (1.0 + Math.cos(phaseAngle * MathUtil.DEGREES_TO_RADIANS));
  }


  /**
   * Return the date of the next full moon after today.
   */
  // TODO(serafini): This could also be error prone right around the time
  // of the full and new moons...
  public static Date getNextFullMoon(Date now) {
    // First, get the moon's current phase.
    double phase = Moon.calculatePhaseAngle(now);

    // Next, figure out if the moon is waxing or waning.
    boolean isWaxing = false;
    Date later = new Date(now.getTime() + 1 * 3600 * 1000);
    double phase2 = Moon.calculatePhaseAngle(later);
    isWaxing = phase2 > phase;

    // If moon is waxing, next full moon is (180.0 - phase)/360.0 * 29.53.
    // If moon is waning, next full moon is (360.0 - phase)/360.0 * 29.53.
    final double LUNAR_CYCLE = 29.53f;  // In days.
    double baseAngle = (isWaxing ? 180.0 : 360.0);
    double numDays = (baseAngle - phase) / 360.0 * LUNAR_CYCLE;

    return new Date(now.getTime() + (long) (numDays * 24.0 * 3600.0 * 1000.0));
  }
  
  /**
   * Return the date of the next full moon after today.
   * Slow incremental version, only correct to within an hour.
   */
  public static Date getNextFullMoonSlow(Date now) {
    Date fullMoon = new Date(now.getTime());
    double phase = Moon.calculatePhaseAngle(now);
    boolean waxing = false;
    
    while (true) {
      fullMoon.setTime(fullMoon.getTime() + TimeConstants.MILLISECONDS_PER_HOUR);
      double nextPhase = Moon.calculatePhaseAngle(fullMoon);
      if (waxing && nextPhase < phase) {
        fullMoon.setTime(fullMoon.getTime() - TimeConstants.MILLISECONDS_PER_HOUR);
        return fullMoon;
      }
      waxing = (nextPhase > phase);
      phase = nextPhase;
      Log.d(TAG, "Phase: " + phase + "\tDate:" + fullMoon);
    }
  }

  /**
   * Calculates the planet's magnitude for the given date.
   *
   * TODO(serafini): I need to re-factor this method so it uses the phase
   * calculations above. For now, I'm going to duplicate some code to avoid
   * some redundant calculations at run time.
   */
  public double getMagnitude(Date time) {
    // TODO(serafini): For now, return semi-reasonable values for the Sun and
    // Moon. We shouldn't call this method for those bodies, but we want to do
    // something sane if we do.
    if (this == Planet.Sun) {
      return -27.0;
    }
    if (this == Planet.Moon) {
      return -10.0;
    }

    // First, determine position in the solar system.
    HeliocentricCoordinates planetCoords = HeliocentricCoordinates.getInstance(this, time);

    // Second, determine position relative to Earth
    HeliocentricCoordinates earthCoords = HeliocentricCoordinates.getInstance(Planet.Sun, time);
    double earthDistance = planetCoords.DistanceFrom(earthCoords);

    // Third, calculate the phase of the body.
    double phase = Math.acos((earthDistance * earthDistance +
        planetCoords.radius * planetCoords.radius -
        earthCoords.radius * earthCoords.radius) /
        (2.0 * earthDistance * planetCoords.radius)) * MathUtil.RADIANS_TO_DEGREES;
    double p = phase/100.0;     // Normalized phase angle

    // Finally, calculate the magnitude of the body.
    double mag = -100.0;      // Apparent visual magnitude

    switch (this) {
      case Mercury:
        mag = -0.42f + (3.80f - (2.73f - 2.00f * p) * p) * p;
        break;
      case Venus:
        mag = -4.40f + (0.09f + (2.39f - 0.65f * p) * p) * p;
        break;
      case Mars:
        mag = -1.52f + 1.6f * p;
        break;
      case Jupiter:
        mag = -9.40f + 0.5f * p;
        break;
      case Saturn:
        // TODO(serafini): Add the real calculations that consider the position
        // of the rings. For now, lets assume the following, which gets us a reasonable
        // approximation of Saturn's magnitude for the near future.
        mag = -8.75f;
        break;
      case Uranus:
        mag = -7.19f;
        break;
      case Neptune:
        mag = -6.87f;
        break;
      case Pluto:
        mag = -1.0;
        break;
      default:
        Log.e(MiscUtil.getTag(this), "Invalid planet: " + this);
        // At least make it faint!
        mag = 100f;
        break;
    }
    return (mag + 5.0 * Math.log10(planetCoords.radius * earthDistance));
  }


  // TODO(serafini): This is experimental code used to scale planetary images.
  public double getPlanetaryImageSize() {
    switch(this) {
    case Sun:
    case Moon:
      return 0.02f;
    case Mercury:
    case Venus:
    case Mars:
    case Pluto:
      return 0.01f;
    case Jupiter:
        return 0.025f;
    case Uranus:
    case Neptune:
      return 0.015f;
    case Saturn:
      return 0.035f;
    default:
      return 0.02f;
    }
  }


  /**
   * Enum that identifies whether we are interested in rise or set time.
   */
  public enum RiseSetIndicator { RISE, SET }

  // Maximum number of times to calculate rise/set times. If we cannot
  // converge after this many iteretions, we will fail.
  private final static int MAX_ITERATIONS = 25;

  /**
   * Calculates the next rise or set time of this planet from a given observer.
   * Returns null if the planet doesn't rise or set during the next day.
   * 
   * @param now Calendar time from which to calculate next rise / set time.
   * @param loc Location of observer.
   * @param indicator Indicates whether to look for rise or set time.
   * @return New Calendar set to the next rise or set time if within
   *         the next day, otherwise null.
   */
  public Calendar calcNextRiseSetTime(Calendar now, LatLong loc,
                                      RiseSetIndicator indicator) {
    // Make a copy of the calendar to return.
    Calendar riseSetTime = Calendar.getInstance();
    double riseSetUt = calcRiseSetTime(now.getTime(), loc, indicator);
    // Early out if no nearby rise set time.
    if (riseSetUt < 0) {
      return null;
    }
    
    // Find the start of this day in the local time zone. The (a / b) * b
    // formulation looks weird, it's using the properties of int arithmetic
    // so that (a / b) is really floor(a / b).
    long dayStart = (now.getTimeInMillis() / TimeConstants.MILLISECONDS_PER_DAY)
                    * TimeConstants.MILLISECONDS_PER_DAY - riseSetTime.get(Calendar.ZONE_OFFSET);
    long riseSetUtMillis = (long) (calcRiseSetTime(now.getTime(), loc, indicator)
                                  * TimeConstants.MILLISECONDS_PER_HOUR);
    long newTime = dayStart + riseSetUtMillis + riseSetTime.get(Calendar.ZONE_OFFSET);
    // If the newTime is before the current time, go forward 1 day.
    if (newTime < now.getTimeInMillis()) {
      Log.d(TAG, "Nearest Rise/Set is in the past. Adding one day.");
      newTime += TimeConstants.MILLISECONDS_PER_DAY;
    }
    riseSetTime.setTimeInMillis(newTime);
    if (!riseSetTime.after(now)) {
      Log.e(TAG, "Next rise set time (" + riseSetTime.toString()
                 + ") should be after current time (" + now.toString() + ")");
    }
    return riseSetTime;
  }

  // Internally calculate the rise and set time of an object.
  // Returns a double, the number of hours through the day in UT.
  private double calcRiseSetTime(Date d, LatLong loc,
                                 RiseSetIndicator indicator) {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
    cal.setTime(d);

    double sign = (indicator == RiseSetIndicator.RISE ? 1.0 : -1.0);
    double delta = 5.0;
    double ut = 12.0;

    int counter = 0;
    while ((Math.abs(delta) > 0.008) && counter < MAX_ITERATIONS) {
      cal.set(Calendar.HOUR_OF_DAY, (int) Math.floor(ut));
      double minutes = (ut - Math.floor(ut)) * 60.0;
      cal.set(Calendar.MINUTE, (int) minutes);
      cal.set(Calendar.SECOND, (int) ((minutes - Math.floor(minutes)) * 60.0));

      // Calculate the hour angle and declination of the planet.
      // TODO(serafini): Need to fix this for arbitrary RA/Dec locations.
      Date tmp = cal.getTime();
      HeliocentricCoordinates sunCoordinates =
        HeliocentricCoordinates.getInstance(Planet.Sun, tmp);
      RaDec raDec = RaDec.getInstance(this, tmp, sunCoordinates);

      // GHA = GST - RA. (In degrees.)
      double gst = TimeUtil.meanSiderealTime(tmp, 0);
      double gha = gst - raDec.ra;

      // The value of -0.83 works for the diameter of the Sun and Moon. We
      // assume that other objects are simply points.
      double bodySize = (this == Planet.Sun || this == Planet.Moon) ? -0.83 : 0.0;
      double hourAngle = calculateHourAngle(bodySize, loc.getLatitude(), raDec.dec);

      delta = (gha + loc.getLongitude() + (sign * hourAngle)) / 15.0;
      while (delta < -24.0) {
        delta = delta + 24.0;
      }
      while (delta > 24.0) {
        delta = delta - 24.0;
      }
      ut = ut - delta;

      // I think we need to normalize UT
      while (ut < 0.0) {
        ut = ut + 24.0;
      }
      while (ut > 24.0) {
        ut = ut - 24.0;
      }

      ++counter;
    }

    // Return failure if we didn't converge.
    if (counter == MAX_ITERATIONS) {
      Log.d(TAG, "Rise/Set calculation didn't converge.");
      return -1.0;
    }

    // TODO(serafini): Need to handle latitudes above 60
    // At latitudes above 60, we need to calculate the following:
    // sin h = sin phi sin delta + cos phi cos delta cos (gha + lambda)
    return ut;
  }


  // Calculates the hour angle of a given declination for the given location.
  // This is a helper application for the rise and set calculations. Its
  // probably not worth using as a general purpose method.
  // All values are in degrees.
  //
  // This method calculates the hour angle from the meridian using the
  // following equation from the Astronomical Almanac (p487):
  // cos ha = (sin alt - sin lat * sin dec) / (cos lat * cos dec)
  public static double calculateHourAngle(double altitude, double latitude,
                                         double declination) {
    double altRads = altitude * MathUtil.DEGREES_TO_RADIANS;
    double latRads = latitude * MathUtil.DEGREES_TO_RADIANS;
    double decRads = declination * MathUtil.DEGREES_TO_RADIANS;
    double cosHa = (Math.sin(altRads) - Math.sin(latRads) * Math.sin(decRads)) /
        (Math.cos(latRads) * Math.cos(decRads));

    return MathUtil.RADIANS_TO_DEGREES * Math.acos(cosHa);
  }
}
