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

package uk.ac.sussex.gdsc.core.ij;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;
import ij.ImageStack;
import ij.process.ImageProcessor;
import uk.ac.sussex.gdsc.core.utils.DigestUtils;

/**
 * Contains helper functions for using the {@link Hasher} functionality of Guava.
 */
public final class ImageJHashUtils {

  /** The hash size in bits. */
  private static final int HASH_SIZE = 128;

  /**
   * A funnel for {@code byte[]} pixels.
   */
  public enum BytePixelsFunnel implements Funnel<byte[]> {
    /** The instance. */
    INSTANCE;

    @Override
    public void funnel(byte[] from, PrimitiveSink into) {
      into.putBytes(from);
    }
  }

  /**
   * A funnel for {@code short[]} pixels.
   */
  public enum ShortPixelsFunnel implements Funnel<short[]> {
    /** The instance. */
    INSTANCE;

    @Override
    public void funnel(short[] from, PrimitiveSink into) {
      for (final short value : from) {
        into.putShort(value);
      }
    }
  }

  /**
   * A funnel for {@code float[]} pixels.
   */
  public enum FloatPixelsFunnel implements Funnel<float[]> {
    /** The instance. */
    INSTANCE;

    @Override
    public void funnel(float[] from, PrimitiveSink into) {
      for (final float value : from) {
        into.putFloat(value);
      }
    }
  }

  /**
   * A funnel for {@code int[]} pixels.
   */
  public enum IntPixelsFunnel implements Funnel<int[]> {
    /** The instance. */
    INSTANCE;

    @Override
    public void funnel(int[] from, PrimitiveSink into) {
      for (final int value : from) {
        into.putInt(value);
      }
    }
  }

  /**
   * A funnel for {@code byte[]} pixels as an object.
   */
  public enum ByteObjectPixelsFunnel implements Funnel<Object> {
    /** The instance. */
    INSTANCE;

    @Override
    public void funnel(Object from, PrimitiveSink into) {
      BytePixelsFunnel.INSTANCE.funnel((byte[]) from, into);
    }
  }

  /**
   * A funnel for {@code short[]} pixels as an object.
   */
  public enum ShortObjectPixelsFunnel implements Funnel<Object> {
    /** The instance. */
    INSTANCE;

    @Override
    public void funnel(Object from, PrimitiveSink into) {
      ShortPixelsFunnel.INSTANCE.funnel((short[]) from, into);
    }
  }

  /**
   * A funnel for {@code float[]} pixels as an object.
   */
  public enum FloatObjectPixelsFunnel implements Funnel<Object> {
    /** The instance. */
    INSTANCE;

    @Override
    public void funnel(Object from, PrimitiveSink into) {
      FloatPixelsFunnel.INSTANCE.funnel((float[]) from, into);
    }
  }

  /**
   * A funnel for {@code int[]} pixels as an object.
   */
  public enum IntObjectPixelsFunnel implements Funnel<Object> {
    /** The instance. */
    INSTANCE;

    @Override
    public void funnel(Object from, PrimitiveSink into) {
      IntPixelsFunnel.INSTANCE.funnel((int[]) from, into);
    }
  }

  /**
   * No public construction.
   */
  private ImageJHashUtils() {}

  /**
   * Gets the pixels funnel. Supports {@code byte[]}, {@code  short[]}, {@code  float[]},
   * {@code  int[]} arrays.
   *
   * @param pixels the pixels
   * @return the pixel funnel
   * @throws IllegalArgumentException If the pixels array type is not recognised
   */
  public static Funnel<Object> getPixelsFunnel(Object pixels) {
    if (pixels instanceof byte[]) {
      return ByteObjectPixelsFunnel.INSTANCE;
    }
    if (pixels instanceof short[]) {
      return ShortObjectPixelsFunnel.INSTANCE;
    }
    if (pixels instanceof float[]) {
      return FloatObjectPixelsFunnel.INSTANCE;
    }
    if (pixels instanceof int[]) {
      return IntObjectPixelsFunnel.INSTANCE;
    }
    throw new IllegalArgumentException("Unrecognised pixels type");
  }

  /**
   * Digest the processor pixels. No other information from the processor is digested.
   *
   * <p>Uses the fast hashing algorithm provided by Guava's {@link Hashing#goodFastHash(int)} using
   * a 128 bit hash.
   *
   * @param ip the image
   * @return the string
   */
  public static String digest(ImageProcessor ip) {
    final Object pixels = ip.getPixels();
    final Funnel<Object> funnel = getPixelsFunnel(pixels);
    final Hasher hasher = Hashing.goodFastHash(HASH_SIZE).newHasher();
    hasher.putObject(pixels, funnel);
    return DigestUtils.toHex(hasher.hash().asBytes());
  }

  /**
   * Digest the stack pixels. No other information from the stack is digested.
   *
   * <p>Uses the fast hashing algorithm provided by Guava's {@link Hashing#goodFastHash(int)} using
   * a 128 bit hash.
   *
   * @param stack the stack
   * @return the string
   */
  public static String digest(ImageStack stack) {
    final Funnel<Object> funnel = getPixelsFunnel(stack.getPixels(1));
    final Hasher hasher = Hashing.goodFastHash(HASH_SIZE).newHasher();
    for (int i = 1; i <= stack.getSize(); i++) {
      hasher.putObject(stack.getPixels(i), funnel);
    }
    return DigestUtils.toHex(hasher.hash().asBytes());
  }
}
