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

package com.google.android.stardroid.util;

import com.google.android.stardroid.units.Vector3;

public class Matrix4x4 {

  public Matrix4x4() {}

  public Matrix4x4(float[] contents) {
    assert(contents.length == 16);
    for (int i = 0; i < 16; ++i) {
      mValues[i] = contents[i];
    }
  }

  public Matrix4x4(double[] contents) {
    assert(contents.length == 16);
    for (int i = 0; i < 16; ++i) {
      mValues[i] = (float) contents[i];
    }
  }

  public static Matrix4x4 createIdentity() {
    return createScaling(1, 1, 1);
  }

  public static Matrix4x4 createScaling(double x, double y, double z) {
    return new Matrix4x4(new double[] {
        x, 0, 0, 0,
        0, y, 0, 0,
        0, 0, z, 0,
        0, 0, 0, 1});
  }

  public static Matrix4x4 createTranslation(double x, double y, double z) {
    return new Matrix4x4(new double[] {
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        x, y, z, 1});
  }

  // axis MUST be normalized.
  public static Matrix4x4 createRotation(double angle, Vector3 axis) {
    double[] m = new double[16];

    double xSqr = axis.x * axis.x;
    double ySqr = axis.y * axis.y;
    double zSqr = axis.z * axis.z;

    double sinAngle = Math.sin(angle);

    double cosAngle = Math.cos(angle);
    double oneMinusCosAngle = 1 - cosAngle;

    double xSinAngle = axis.x * sinAngle;
    double ySinAngle = axis.y * sinAngle;
    double zSinAngle = axis.z * sinAngle;

    double zOneMinusCosAngle = axis.z * oneMinusCosAngle;

    double xyOneMinusCosAngle = axis.x * axis.y * oneMinusCosAngle;
    double xzOneMinusCosAngle = axis.x * zOneMinusCosAngle;
    double yzOneMinusCosAngle = axis.y * zOneMinusCosAngle;

    m[0] = (float) (xSqr + (ySqr + zSqr) * cosAngle);
    m[1] = (float) (xyOneMinusCosAngle + zSinAngle);
    m[2] = (float) (xzOneMinusCosAngle - ySinAngle);
    m[3] = 0;

    m[4] = (float) (xyOneMinusCosAngle - zSinAngle);
    m[5] = (float) (ySqr + (xSqr + zSqr) * cosAngle);
    m[6] = (float) (yzOneMinusCosAngle + xSinAngle);
    m[7] = 0;

    m[8] = (float) (xzOneMinusCosAngle + ySinAngle);
    m[9] = (float) (yzOneMinusCosAngle - xSinAngle);
    m[10] = (float) (zSqr + (xSqr + ySqr) * cosAngle);
    m[11] = 0;

    m[12] = 0;
    m[13] = 0;
    m[14] = 0;
    m[15] = 1;

    return new Matrix4x4(m);
  }

  public static Matrix4x4 createPerspectiveProjection(double width, double height, double fovyInRadians) {
    double near = 0.01;
    double far = 10000.0;

    double inverseAspectRatio = (height / width);

    double oneOverTanHalfRadiusOfView = (1.0 / Math.tan(fovyInRadians));

    return new Matrix4x4(new double[]{
        inverseAspectRatio * oneOverTanHalfRadiusOfView,
        0,
        0,
        0,

        0,
        oneOverTanHalfRadiusOfView,
        0,
        0,

        0,
        0,
        -(far + near) / (far - near),
        -1,

        0,
        0,
        -2*far*near / (far - near),
        0});
  }

  public static Matrix4x4 createView(Vector3 lookDir, Vector3 up, Vector3 right) {
    return new Matrix4x4(new double[] {
        right.x,
        up.x,
        -lookDir.x,
        0,

        right.y,
        up.y,
        -lookDir.y,
        0,

        right.z,
        up.z,
        -lookDir.z,
        0,

        0,
        0,
        0,
        1,});
  }

  public static Matrix4x4 multiplyMM(Matrix4x4 mat1, Matrix4x4 mat2) {
    float[] m = mat1.mValues;
    float[] n = mat2.mValues;

    return new Matrix4x4(new double[] {
        m[0]*n[0] + m[4]*n[1] + m[8]*n[2] + m[12]*n[3],
        m[1]*n[0] + m[5]*n[1] + m[9]*n[2] + m[13]*n[3],
        m[2]*n[0] + m[6]*n[1] + m[10]*n[2] + m[14]*n[3],
        m[3]*n[0] + m[7]*n[1] + m[11]*n[2] + m[15]*n[3],

        m[0]*n[4] + m[4]*n[5] + m[8]*n[6] + m[12]*n[7],
        m[1]*n[4] + m[5]*n[5] + m[9]*n[6] + m[13]*n[7],
        m[2]*n[4] + m[6]*n[5] + m[10]*n[6] + m[14]*n[7],
        m[3]*n[4] + m[7]*n[5] + m[11]*n[6] + m[15]*n[7],

        m[0]*n[8] + m[4]*n[9] + m[8]*n[10] + m[12]*n[11],
        m[1]*n[8] + m[5]*n[9] + m[9]*n[10] + m[13]*n[11],
        m[2]*n[8] + m[6]*n[9] + m[10]*n[10] + m[14]*n[11],
        m[3]*n[8] + m[7]*n[9] + m[11]*n[10] + m[15]*n[11],

        m[0]*n[12] + m[4]*n[13] + m[8]*n[14] + m[12]*n[15],
        m[1]*n[12] + m[5]*n[13] + m[9]*n[14] + m[13]*n[15],
        m[2]*n[12] + m[6]*n[13] + m[10]*n[14] + m[14]*n[15],
        m[3]*n[12] + m[7]*n[13] + m[11]*n[14] + m[15]*n[15]});
  }

  public static Vector3 multiplyMV(Matrix4x4 mat, Vector3 v) {
    float[] m = mat.mValues;
    return new Vector3(
        m[0]*v.x + m[4]*v.y + m[8]*v.z + m[12],
        m[1]*v.x + m[5]*v.y + m[9]*v.z + m[13],
        m[2]*v.x + m[6]*v.y + m[10]*v.z + m[14]);
  }

  /**
   * Used to perform a perspective transformation.  This multiplies the given
   * vector by the matrix, but also divides the x and y components by the w
   * component of the result, as needed when doing perspective projections.
   */
  public static Vector3 transformVector(Matrix4x4 mat, Vector3 v) {
    Vector3 trans = multiplyMV(mat, v);
    float[] m = mat.mValues;
    double w = m[3]*v.x + m[7]*v.y + m[11]*v.z + m[15];
    double oneOverW = 1.0f / w;
    trans.x *= oneOverW;
    trans.y *= oneOverW;
    // Don't transform z, we just leave it as a "pseudo-depth".
    return trans;
  }

  public float[] getFloatArray() {
    return mValues;
  }

  private float[] mValues = new float[16];
}
