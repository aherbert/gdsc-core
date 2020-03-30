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

/**
 * Provides classes for constructing and searching tree-like data structures such as a <a
 * href="https://en.wikipedia.org/wiki/K-d_tree">KD-Tree</a>.
 *
 * <p>The code contained here has been adapted from <a
 * href="http://robowiki.net/wiki/User:Rednaxela/kD-Tree">Rednaxela's KD-tree</a> and is Copyright
 * 2009 Rednaxela.
 *
 * <p>Modifications to the original code have been made to create specialisations for double or
 * float coordinates with and without associated data and to use a common interface for all
 * implementations. The tree navigation has been altered to be thread-safe to allow concurrent
 * search.
 *
 * @since 2.0
 */
package uk.ac.sussex.gdsc.core.trees;
