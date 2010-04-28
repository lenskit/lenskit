/**
 * 
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
