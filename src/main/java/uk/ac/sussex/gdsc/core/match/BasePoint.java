/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2019 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.core.match;

/**
 * Stores a 2D/3D point.
 *
 * <p>Overrides equals and hashCode methods using x,y,z, coordinates for equivalence. Derived
 * classes can optionally override this.
 *
 * @see java.lang.Object#equals(java.lang.Object)
 * @see java.lang.Object#hashCode()
 */
public class BasePoint implements Coordinate {
  /** The x. */
  protected final float x;

  /** The y. */
  protected final float y;

  /** The z. */
  protected final float z;

  /**
   * Instantiates a new base point.
   *
   * @param x the x value
   * @param y the y value
   * @param z the z value
   */
  public BasePoint(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Instantiates a new base point.
   *
   * @param x the x
   * @param y the y
   */
  public BasePoint(float x, float y) {
    this(x, y, 0);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    // Must be the same class, allowing subtypes their own implementation
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    // cast to native object is now safe
    final BasePoint that = (BasePoint) object;

    return x == that.x && y == that.y && z == that.z;
  }

  @Override
  public int hashCode() {
    // Note: floatToRawIntBits does not unify all possible NaN values
    // However since the equals() will fail for NaN values we are not
    // breaking the java contract.
    return (41 * (41 * (41 + Float.floatToRawIntBits(x)) + Float.floatToRawIntBits(y))
        + Float.floatToRawIntBits(z));
  }

  @Override
  public float getX() {
    return x;
  }

  @Override
  public float getY() {
    return y;
  }

  @Override
  public float getZ() {
    return z;
  }

  @Override
  public int getXint() {
    return (int) x;
  }

  @Override
  public int getYint() {
    return (int) y;
  }

  @Override
  public int getZint() {
    return (int) z;
  }

  @Override
  public double distance(float x, float y, float z) {
    return Math.sqrt(distanceSquared(x, y, z));
  }

  @Override
  public double distance(float x, float y) {
    return Math.sqrt(distanceSquared(x, y));
  }

  @Override
  public double distanceSquared(float x, float y, float z) {
    return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y) + (this.z - z) * (this.z - z);
  }

  @Override
  public double distanceSquared(float x, float y) {
    return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y);
  }

  @Override
  public double distanceXy(Coordinate other) {
    return distance(other.getX(), other.getY());
  }

  @Override
  public double distanceXySquared(Coordinate other) {
    return distanceSquared(other.getX(), other.getY());
  }

  @Override
  public double distanceXyz(Coordinate other) {
    return distance(other.getX(), other.getY(), other.getZ());
  }

  @Override
  public double distanceXyzSquared(Coordinate other) {
    return distanceSquared(other.getX(), other.getY(), other.getZ());
  }

  /**
   * Shift the point by the given deltas.
   *
   * @param dx the dx
   * @param dy the dy
   * @param dz the dz
   * @return the new base point
   */
  public BasePoint shift(float dx, float dy, float dz) {
    return new BasePoint(x + dx, y + dy, z + dz);
  }
}
