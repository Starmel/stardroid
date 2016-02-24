// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.stardroid.units;

import com.google.android.stardroid.provider.ephemeris.OrbitalElements;
import com.google.android.stardroid.provider.ephemeris.Planet;
import com.google.android.stardroid.util.MathUtil;

import java.util.Date;

public class HeliocentricCoordinates extends Vector3 {
  public double radius;  // Radius. (AU)

  // Value of the obliquity of the ecliptic for J2000
  private static final double OBLIQUITY = 23.439281 * MathUtil.DEGREES_TO_RADIANS;

  public HeliocentricCoordinates(double radius, double xh, double yh, double zh) {
    super(xh, yh, zh);
    this.radius = radius;
  }

  /**
   * Subtracts the values of the given heliocentric coordinates from this
   * object.
   */
  public void Subtract(HeliocentricCoordinates other) {
    this.x -= other.x;
    this.y -= other.y;
    this.z -= other.z;
  }

  public HeliocentricCoordinates CalculateEquatorialCoordinates() {
    return new HeliocentricCoordinates(this.radius,
        this.x,
        this.y * Math.cos(OBLIQUITY) - this.z * Math.sin(OBLIQUITY),
        this.y * Math.sin(OBLIQUITY) + this.z * Math.cos(OBLIQUITY));
  }

  public double DistanceFrom(HeliocentricCoordinates other) {
    double dx = this.x - other.x;
    double dy = this.y - other.y;
    double dz = this.z - other.z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public static HeliocentricCoordinates getInstance(Planet planet, Date date) {
    return getInstance(planet.getOrbitalElements(date));
  }

  public static HeliocentricCoordinates getInstance(OrbitalElements elem) {
    double anomaly = elem.getAnomaly();
    double ecc = elem.eccentricity;
    double radius = elem.distance * (1 - ecc * ecc) / (1 + ecc * Math.cos(anomaly));

    // heliocentric rectangular coordinates of planet
    double per = elem.perihelion;
    double asc = elem.ascendingNode;
    double inc = elem.inclination;
    double xh = radius *
        (Math.cos(asc) * Math.cos(anomaly + per - asc) -
         Math.sin(asc) * Math.sin(anomaly + per - asc) *
         Math.cos(inc));
    double yh = radius *
        (Math.sin(asc) * Math.cos(anomaly + per - asc) +
        Math.cos(asc) * Math.sin(anomaly + per - asc) *
        Math.cos(inc));
    double zh = radius * (Math.sin(anomaly + per - asc) * Math.sin(inc));

    return new HeliocentricCoordinates(radius, xh, yh, zh);
  }

  @Override public String toString() {
    return String.format("(%d, %d, %d, %d)", x, y, z, radius);
  }
}
