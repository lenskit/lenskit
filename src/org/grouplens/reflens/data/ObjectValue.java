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
