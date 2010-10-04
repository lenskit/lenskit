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

package org.grouplens.reflens.data.generic;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import java.util.Map;

/**
 * Create generic maps from keys to floats.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GenericMapFactory<K> implements MapFactory<K, Float> {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.generic.MapFactory#copy(java.util.Map)
	 */
	@Override
	public Map<K, Float> copy(Map<K, Float> map) {
		return new Object2FloatOpenHashMap<K>(map);
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.generic.MapFactory#create()
	 */
	@Override
	public Map<K, Float> create() {
		return new Object2FloatOpenHashMap<K>();
	}

}
