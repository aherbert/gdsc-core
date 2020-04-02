/*
 * Copyright 2009 Rednaxela
 *
 * This software is provided 'as-is', without any express or implied warranty. In no event will the
 * authors be held liable for any damages arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose, including commercial
 * applications, and to alter it and redistribute it freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the
 * original software. If you use this software in a product, an acknowledgment in the product
 * documentation would be appreciated but is not required.
 *
 * 2. This notice may not be removed or altered from any source distribution.
 */

package uk.ac.sussex.gdsc.core.trees;

/**
 * Constants representing the status of a node during the running of a search.
 *
 * <p>This is deliberately not an enum to minimise memory overhead when storing an array
 * of the status. An enum would be a class instance and may be larger than 32-bits per
 * value depending on the JVM platform.
 */
final class Status {
  /** Status indicating that neither child has been visited. */
  static final int NONE = 0x0;
  /** Status indicating that the left has been visited. */
  static final int LEFTVISITED = 0x1;
  /** Status indicating that the right has been visited. */
  static final int RIGHTVISITED = 0x2;
  /** Status indicating that both the left and the right have been visited. */
  static final int ALLVISITED = 0x3;

  /** No instances. */
  private Status() {}
}
