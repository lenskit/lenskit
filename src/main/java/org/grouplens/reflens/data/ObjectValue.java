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

package org.grouplens.reflens.data;

import java.util.Iterator;
import java.util.Map;

public class ObjectValue<I> implements Comparable<ObjectValue<I>> {
	private I item;
	private float value;
	
	public ObjectValue(I item, float value) {
		this.item = item;
		this.value = value;
	}
	
	public I getItem() {
		return item;
	}
	
	public float getRating() {
		return value;
	}
	
	public static <I> Iterable<ObjectValue<I>> wrap(final Iterable<Map.Entry<I,Float>> iter) {
		return new Iterable<ObjectValue<I>>() {
			public Iterator<ObjectValue<I>> iterator() {
				return wrap(iter.iterator());
			}
		};
	}
	
	private static <I> Iterator<ObjectValue<I>> wrap(final Iterator<Map.Entry<I, Float>> iter) {
		return new Iterator<ObjectValue<I>>() {
			public boolean hasNext() {
				return iter.hasNext();
			}
			public ObjectValue<I> next() {
				Map.Entry<I, Float> next = iter.next();
				return new ObjectValue<I>(next.getKey(), next.getValue());
			}
			public void remove() {
				iter.remove();
			}
		};
	}
	
	@Override
	public int compareTo(ObjectValue<I> other) {
		return Float.compare(value, other.getRating());
	}
}
