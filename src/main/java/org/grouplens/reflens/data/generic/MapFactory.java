package org.grouplens.reflens.data.generic;

import java.util.Map;

/**
 * Interface for factories that build maps.  We use this to allow more efficient
 * map implementations to be introduced in certain places.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <K>
 * @param <V>
 */
public interface MapFactory<K, V> {
	public Map<K,V> create();
	public Map<K,V> copy(Map<K,V> map);
}
