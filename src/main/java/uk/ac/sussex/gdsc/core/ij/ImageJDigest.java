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

import uk.ac.sussex.gdsc.core.utils.DigestUtils;

import ij.ImageStack;
import ij.process.ImageProcessor;

import java.security.MessageDigest;

/**
 * Provide digest functionality for ImageJ images to digest the pixels array.
 *
 * @since 1.2.0
 */
public class ImageJDigest {

  /** The message digest. */
  private final MessageDigest messageDigest;

  /**
   * The base class for digesting pixels.
   */
  private abstract class PixelsDigester {
    MessageDigest pixelsDigest;

    PixelsDigester(MessageDigest digest) {
      this.pixelsDigest = digest;
      digest.reset();
    }

    abstract void update(Object pixels);
  }

  /**
   * A digester for {@code byte} pixels.
   */
  private class BytePixelsDigester extends PixelsDigester {
    BytePixelsDigester(MessageDigest digest) {
      super(digest);
    }

    @Override
    void update(Object pixels) {
      pixelsDigest.update((byte[]) pixels);
    }
  }

  /**
   * A digester for {@code short} pixels.
   */
  private class ShortPixelsDigester extends PixelsDigester {
    byte[] buffer = new byte[2];

    ShortPixelsDigester(MessageDigest digest) {
      super(digest);
    }

    @Override
    void update(Object pixels) {
      final short[] data = (short[]) pixels;
      for (final int v : data) {
        buffer[0] = (byte) (v >>> 8);
        buffer[1] = (byte) (v >>> 0);
        pixelsDigest.update(buffer);
      }
    }
  }

  /**
   * A digester for {@code int} pixels.
   */
  private class IntegerPixelsDigester extends PixelsDigester {
    byte[] buffer = new byte[4];

    IntegerPixelsDigester(MessageDigest digest) {
      super(digest);
    }

    @Override
    void update(Object pixels) {
      final int[] data = (int[]) pixels;
      for (final int v : data) {
        buffer[0] = (byte) (v >>> 24);
        buffer[1] = (byte) (v >>> 16);
        buffer[2] = (byte) (v >>> 8);
        buffer[3] = (byte) (v >>> 0);
        pixelsDigest.update(buffer);
      }
    }
  }

  /**
   * A digester for {@code float} pixels.
   */
  private class FloatPixelsDigester extends PixelsDigester {
    byte[] buffer = new byte[4];

    FloatPixelsDigester(MessageDigest digest) {
      super(digest);
    }

    @Override
    void update(Object pixels) {
      final float[] data = (float[]) pixels;
      for (final float value : data) {
        final int v = Float.floatToRawIntBits(value);
        buffer[0] = (byte) (v >>> 24);
        buffer[1] = (byte) (v >>> 16);
        buffer[2] = (byte) (v >>> 8);
        buffer[3] = (byte) (v >>> 0);
        pixelsDigest.update(buffer);
      }
    }
  }

  /**
   * Instantiates a new IJ digest.
   */
  public ImageJDigest() {
    this(DigestUtils.MD5);
  }

  /**
   * Instantiates a new IJ digest.
   *
   * @param algorithm the algorithm
   */
  public ImageJDigest(String algorithm) {
    messageDigest = DigestUtils.getDigest(algorithm);
  }

  /**
   * Digest the processor.
   *
   * @param ip the image
   * @return the string
   */
  public String digest(ImageProcessor ip) {
    final Object pixels = ip.getPixels();
    final PixelsDigester digester = getPixelsDigester(pixels);
    digester.update(pixels);
    return DigestUtils.toHex(digester.pixelsDigest.digest());
  }

  /**
   * Digest the stack.
   *
   * @param stack the stack
   * @return the string
   */
  public String digest(ImageStack stack) {
    final PixelsDigester digester = getPixelsDigester(stack.getPixels(1));
    for (int i = 1; i <= stack.getSize(); i++) {
      digester.update(stack.getPixels(i));
    }
    return DigestUtils.toHex(digester.pixelsDigest.digest());
  }

  /**
   * Gets the pixels digester.
   *
   * @param pixels the pixels
   * @return the pixels digester
   */
  private PixelsDigester getPixelsDigester(Object pixels) {
    if (pixels instanceof byte[]) {
      return new BytePixelsDigester(messageDigest);
    }
    if (pixels instanceof short[]) {
      return new ShortPixelsDigester(messageDigest);
    }
    if (pixels instanceof float[]) {
      return new FloatPixelsDigester(messageDigest);
    }
    if (pixels instanceof int[]) {
      return new IntegerPixelsDigester(messageDigest);
    }
    throw new IllegalArgumentException("Unrecognised pixels type");
  }
}
