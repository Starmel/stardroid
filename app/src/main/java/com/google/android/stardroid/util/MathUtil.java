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

package com.google.android.stardroid.util;

/**
 * Mathematic utilities.
 */
// TODO(jontayler): eliminate this class if we can eliminate floats.
public class MathUtil {
  private MathUtil() {}

  public static final double TWO_PI = 2f * Math.PI;
  public static final double DEGREES_TO_RADIANS = Math.PI / 180;
  public static final double RADIANS_TO_DEGREES = 180 / Math.PI;


  /**
   * Returns x if x <= y, or x-y if not. While this utility performs a role similar to a modulo
   * operation, it assumes x >=0 and that x < 2y.
   */
  private static float quickModulo(float x, float y) {
    if (x > y) return x - y;
    return x;
  }

  /**
   * Returns the 'floored' mod assuming n>0.
   */
  public static double flooredMod(double a, double n){
    return a<0 ? (a%n + n)%n : a%n;
  }
}