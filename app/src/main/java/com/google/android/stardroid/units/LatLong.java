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

package com.google.android.stardroid.units;

import com.google.android.stardroid.util.Geometry;
import com.google.android.stardroid.util.MathUtil;

/**
 * A simple struct for latitude and longitude.
 * 
 */
public class LatLong {
  private double latitude;
  private double longitude;

  public LatLong(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    // Silently enforce reasonable limits
    if (this.latitude > 90f) {
      this.latitude = 90f;
    }
    if (this.latitude < -90f) {
      this.latitude = -90f;
    }
    this.longitude = MathUtil.flooredMod(this.longitude + 180, 360) - 180;
  }

  /**
   * Angular distance between the two points.
   * @param other
   * @return degrees
   */
  public double distanceFrom(LatLong other) {
    // Some misuse of the astronomy math classes
    GeocentricCoordinates otherPnt = GeocentricCoordinates.getInstance(other.getLongitude(),
            other.getLatitude());
    GeocentricCoordinates thisPnt = GeocentricCoordinates.getInstance(this.getLongitude(),
            this.getLatitude());
    double cosTheta = Geometry.cosineSimilarity(thisPnt, otherPnt);
    return Math.acos(cosTheta) * MathUtil.RADIANS_TO_DEGREES;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

}
