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

public class Vector3 {

  public double x;
  public double y;
  public double z;

  public Vector3(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Constructs a Vector3 from a float[2] object.
   * Checks for length. This is probably inefficient, so if you're using this
   * you should already be questioning your use of float[] instead of Vector3.
   * @param xyz
   */
  public Vector3(double[] xyz) throws IllegalArgumentException {
    if (xyz.length != 3) {
      throw new IllegalArgumentException("Trying to create 3 vector from array of length: " + xyz.length);
    }
    this.x = xyz[0];
    this.y = xyz[1];
    this.z = xyz[2];
  }

  public Vector3 copy() {
    return new Vector3(x, y, z);
  }

  /**
   * Assigns these values to the vector's components.
   */
  public void assign(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Assigns the values of the other vector to this one.
   */
  public void assign(Vector3 other) {
    this.x = other.x;
    this.y = other.y;
    this.z = other.z;
  }

  public double length() {
    return Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
  }

  /**
   * Normalize the vector in place, i.e., map it to the corresponding unit vector.
   */
  public void normalize() {
    double norm = this.length();
    this.x = this.x / norm;
    this.y = this.y / norm;
    this.z = this.z / norm;
  }

  /**
   * Scale the vector in place.
   */
  public void scale(double scale) {
    this.x = this.x * scale;
    this.y = this.y * scale;
    this.z = this.z * scale;
  }

  public double[] toDoubleArray() {
    return new double[] {x, y, z};
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Vector3)) return false;
    Vector3 other = (Vector3) object;
    // float equals is a bit of a dodgy concept
    return other.x == x && other.y == y && other.z == z;
  }

  @Override
  public int hashCode() {
    // This is dumb, but it will do for now.
    return (int) (Double.doubleToLongBits(x)
        + Double.doubleToLongBits(y) + Double.doubleToLongBits(z));
  }

  @Override
  public String toString() {
    return String.format("x=%d, y=%d, z=%d", x, y, z);
  }
}
