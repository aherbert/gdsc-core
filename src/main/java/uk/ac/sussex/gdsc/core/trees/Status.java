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
 * Enumeration representing the status of a node during the running of a search.
 */
enum Status {
  /** Status indicating that neither child has been visited. */
  NONE,
  /** Status indicating that the left has been visited. */
  LEFTVISITED,
  /** Status indicating that the right has been visited. */
  RIGHTVISITED,
  /** Status indicating that both the left and the right have been visited. */
  ALLVISITED
}