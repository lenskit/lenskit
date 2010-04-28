/**
 * 
 */
package org.grouplens.reflens.data.integer;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import java.util.Map;

import org.grouplens.reflens.data.generic.MapFactory;

/** Factory for building optimized int-based maps.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IntMapFactory implements MapFactory<Integer, Float> {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.generic.MapFactory#copy(java.util.Map)
	 */
	@Override
	public Map<Integer, Float> copy(Map<Integer, Float> map) {
		return new Int2FloatOpenHashMap(map);
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.generic.MapFactory#create()
	 */
	@Override
	public Map<Integer, Float> create() {
		return new Int2FloatOpenHashMap();
	}

}
