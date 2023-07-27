/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides a seed of bits. Methods are provided to convert between data representations.
 */
public final class Seed {
  /** The bytes. */
  private final byte[] bytes;
  /** The hash code. */
  private int hash;

  /**
   * Create an instance.
   *
   * @param bytes the bytes
   */
  private Seed(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Create a seed from the bytes. No reference to the input array is stored.
   *
   * @param bytes the bytes (must not be null)
   * @return the seed
   */
  public static Seed from(byte[] bytes) {
    return new Seed(Objects.requireNonNull(bytes, "The bytes must not be null").clone());
  }

  /**
   * Create a seed from the hex-encoded characters. No reference to the input is stored.
   *
   * <p>If the sequence is an odd length then the final hex character is assumed to be '0'.
   *
   * @param chars the hex characters
   * @return the seed
   * @throws IllegalArgumentException If the sequence does not contain valid hex characters
   */
  public static Seed from(CharSequence chars) {
    int length = Objects.requireNonNull(chars, "The characters must not be null").length();
    // Avoid IAE when length is zero
    if (length == 0) {
      return new Seed(new byte[0]);
    }
    // Here any error decoding will throw an exception
    return new Seed(Hex.decode(chars, () -> {
      throw new IllegalArgumentException("Invalid hex sequence");
    }));
  }

  /**
   * Create a seed from the value. All bits in the input are used including leading zeros.
   *
   * @param value the value
   * @return the seed
   */
  public static Seed from(long value) {
    return new Seed(ByteBuffer.allocate(8).putLong(value).array());
  }

  /**
   * Converts to a byte representation.
   *
   * @return the bytes
   */
  public byte[] toBytes() {
    return bytes.clone();
  }

  /**
   * Converts to a long representation. The value is created using all of the information in the
   * seed.
   *
   * @return the long
   */
  public long toLong() {
    long result = 0;
    // Process blocks of 8, then the remaining part
    final int length = bytes.length;
    final int limit = length & 0x7ffffff8;
    if (length >= 8) {
      final ByteBuffer bb = ByteBuffer.wrap(bytes);
      for (int i = 0; i < limit; i += 8) {
        result ^= bb.getLong();
      }
    }
    // Consume remaining bytes
    if (limit < length) {
      final ByteBuffer bb = ByteBuffer.allocate(8);
      bb.put(bytes, limit, length - limit);
      bb.rewind();
      result ^= bb.getLong();
    }
    return result;
  }

  @Override
  public int hashCode() {
    int result = hash;
    if (result == 0) {
      hash = result = Arrays.hashCode(bytes);
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    // self check
    if (this == obj) {
      return true;
    }
    // null check and type check (this class is final so no sub-classes are possible)
    if (!(obj instanceof Seed)) {
      return false;
    }
    // field comparison
    return Arrays.equals(bytes, ((Seed) obj).bytes);
  }

  /**
   * Converts to a hex-encoded String of the byte representation.
   */
  @Override
  public String toString() {
    return new String(Hex.encode(bytes));
  }
}
