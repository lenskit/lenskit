/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.grouplens.reflens;

/**
 * Compute the similarity between two objects (typically rating vectors).
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface Similarity<V> {
	/**
	 * Compute the similarity between two vectors.
	 * @param vec1
	 * @param vec2
	 * @return The similarity, in the range [-1,1].
	 */
	double similarity(V vec1, V vec2);
}