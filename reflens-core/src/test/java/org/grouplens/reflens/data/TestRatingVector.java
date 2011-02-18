package org.grouplens.reflens.data;

import static org.grouplens.common.test.MoreAsserts.assertIsEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestRatingVector {
	private static final double EPSILON = 1.0e-6;
	
	protected RatingVector emptyVector() {
		return new RatingVector(Long2DoubleMaps.EMPTY_MAP);
	}
	
	/**
	 * Construct a simple rating vector with three ratings.
	 * @return A rating vector mapping {3, 7, 8} to {1.5, 3.5, 2}.
	 */
	protected RatingVector simpleVector() {
		long[] keys = {3, 7, 8};
		double[] values = {1.5, 3.5, 2};
		return RatingVector.wrap(keys, values);
	}
	
	/**
	 * Construct a simple rating vector with three ratings.
	 * @return A rating vector mapping {3, 5, 8} to {2, 2.3, 1.7}.
	 */
	protected RatingVector simpleVector2() {
		long[] keys = {3, 5, 8};
		double[] values = {2, 2.3, 1.7};
		return RatingVector.wrap(keys, values);
	}

	
	/**
	 * Construct a singleton rating vector mapping 5 to PI.
	 * @return
	 */
	protected RatingVector singleton() {
		return RatingVector.wrap(new long[]{5}, new double[]{Math.PI});
	}
	
	protected static void assertIsNaN(double v) {
		if (!Double.isNaN(v))
			fail("Expected NaN, got " + v);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#get(long)}.
	 */
	@Test
	public void testGet() {
		assertIsNaN(emptyVector().get(5));
		RatingVector v = singleton();
		assertEquals(Math.PI, v.get(5), EPSILON);
		assertIsNaN(v.get(2));
		
		v = simpleVector();
		assertEquals(3.5, v.get(7), EPSILON);
		assertEquals(1.5, v.get(3), EPSILON);
		assertEquals(2, v.get(8), EPSILON);
		assertIsNaN(v.get(1));
		assertIsNaN(v.get(4));
		assertIsNaN(v.get(9));
		assertIsNaN(v.get(42));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#get(long, double)}.
	 */
	@Test
	public void testGetWithDft() {
		assertIsNaN(emptyVector().get(5, Double.NaN));
		assertEquals(-1, emptyVector().get(5, -1), EPSILON);
		RatingVector v = singleton();
		assertEquals(Math.PI, v.get(5, -1), EPSILON);
		assertEquals(-1, v.get(2, -1), EPSILON);
		
		v = simpleVector();
		assertEquals(3.5, v.get(7, -1), EPSILON);
		assertEquals(1.5, v.get(3, -1), EPSILON);
		assertEquals(2, v.get(8, -1), EPSILON);
		assertEquals(-1, v.get(1, -1), EPSILON);
		assertEquals(42, v.get(4, 42), EPSILON);
		assertEquals(-7, v.get(9, -7), EPSILON);
		assertEquals(Math.E, v.get(42, Math.E), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#containsId(long)}.
	 */
	@Test
	public void testContainsId() {
		assertFalse(emptyVector().containsId(5));
		assertFalse(emptyVector().containsId(42));
		assertFalse(emptyVector().containsId(-1));
		
		assertTrue(singleton().containsId(5));
		assertFalse(singleton().containsId(3));
		assertFalse(singleton().containsId(7));
		
		assertFalse(simpleVector().containsId(1));
		assertFalse(simpleVector().containsId(5));
		assertFalse(simpleVector().containsId(42));
		assertTrue(simpleVector().containsId(3));
		assertTrue(simpleVector().containsId(7));
		assertTrue(simpleVector().containsId(8));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#iterator()}.
	 */
	@Test
	public void testIterator() {
		assertFalse(emptyVector().iterator().hasNext());
		try {
			emptyVector().iterator().next();
			fail("iterator.next() should throw exception");
		} catch (NoSuchElementException x) {
			/* no-op */
		}
		
		Iterator<Long2DoubleMap.Entry> iter = singleton().iterator();
		assertTrue(iter.hasNext());
		Long2DoubleMap.Entry e = iter.next();
		assertFalse(iter.hasNext());
		assertEquals(5, e.getLongKey());
		assertEquals(Long.valueOf(5), e.getKey());
		assertEquals(Math.PI, e.getDoubleValue(), EPSILON);
		assertEquals(Double.valueOf(Math.PI), e.getValue(), EPSILON);
		try {
			iter.next();
			fail("iter.next() should throw exception");
		} catch (NoSuchElementException x) {
			/* no-op */
		}
		
		Long2DoubleMap.Entry[] entries = Iterators.toArray(
				simpleVector().iterator(), Long2DoubleMap.Entry.class);
		assertEquals(3, entries.length);
		assertEquals(3, entries[0].getLongKey());
		assertEquals(7, entries[1].getLongKey());
		assertEquals(8, entries[2].getLongKey());
		assertEquals(1.5, entries[0].getDoubleValue(), EPSILON);
		assertEquals(3.5, entries[1].getDoubleValue(), EPSILON);
		assertEquals(2, entries[2].getDoubleValue(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#fastIterator()}.
	 */
	@Test
	public void testFastIterator() {
		assertFalse(emptyVector().fastIterator().hasNext());
		try {
			emptyVector().fastIterator().next();
			fail("iterator.next() should throw exception");
		} catch (NoSuchElementException x) {
			/* no-op */
		}
		
		Iterator<Long2DoubleMap.Entry> iter = singleton().fastIterator();
		assertTrue(iter.hasNext());
		Long2DoubleMap.Entry e = iter.next();
		assertFalse(iter.hasNext());
		assertEquals(5, e.getLongKey());
		assertEquals(Long.valueOf(5), e.getKey());
		assertEquals(Math.PI, e.getDoubleValue(), EPSILON);
		assertEquals(Double.valueOf(Math.PI), e.getValue(), EPSILON);
		try {
			iter.next();
			fail("iter.next() should throw exception");
		} catch (NoSuchElementException x) {
			/* no-op */
		}
		
		Long[] keys = Iterators.toArray(
				Iterators.transform(simpleVector().fastIterator(),
						new Function<Long2DoubleMap.Entry,Long>() {
					public Long apply(Long2DoubleMap.Entry e) {
						return e.getKey();
					}
				}), Long.class);
		assertThat(keys, equalTo(new Long[]{3l, 7l, 8l}));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#fast()}.
	 */
	@Test
	public void testFast() {
		assertNotNull(emptyVector().fast());
		// TODO: do more testing of the fast() method
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#idSet()}.
	 */
	@Test
	public void testIdSet() {
		LongSortedSet set = emptyVector().idSet();
		assertIsEmpty(set);
		
		long[] keys = singleton().idSet().toLongArray();
		assertThat(keys, equalTo(new long[]{5}));
		
		keys = simpleVector().idSet().toLongArray();
		assertThat(keys, equalTo(new long[]{3, 7, 8}));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#values()}.
	 */
	@Test
	public void testValues() {
		assertIsEmpty(emptyVector().values());
		
		double[] vals = singleton().values().toDoubleArray();
		assertEquals(1, vals.length);
		assertEquals(Math.PI, vals[0], EPSILON);
		
		DoubleRBTreeSet s = new DoubleRBTreeSet(simpleVector().values());
		assertThat(s, equalTo(new DoubleRBTreeSet(new double[]{1.5, 3.5, 2})));
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#size()}.
	 */
	@Test
	public void testSize() {
		assertEquals(0, emptyVector().size());
		assertEquals(1, singleton().size());
		assertEquals(3, simpleVector().size());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#isEmpty()}.
	 */
	@Test
	public void testIsEmpty() {
		assertTrue(emptyVector().isEmpty());
		assertFalse(singleton().isEmpty());
		assertFalse(simpleVector().isEmpty());
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#norm()}.
	 */
	@Test
	public void testNorm() {
		assertEquals(0, emptyVector().norm(), EPSILON);
		assertEquals(Math.PI, singleton().norm(), EPSILON);
		assertEquals(4.301162634, simpleVector().norm(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#sum()}.
	 */
	@Test
	public void testSum() {
		assertEquals(0, emptyVector().sum(), EPSILON);
		assertEquals(Math.PI, singleton().sum(), EPSILON);
		assertEquals(7, simpleVector().sum(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#mean()}.
	 */
	@Test
	public void testMean() {
		assertEquals(0, emptyVector().mean(), EPSILON);
		assertEquals(Math.PI, singleton().mean(), EPSILON);
		assertEquals(7.0/3, simpleVector().mean(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#copy()}.
	 */
	@Test
	public void testCopy() {
		assertTrue(emptyVector().copy().isEmpty());
		RatingVector v1 = singleton();
		RatingVector v2 = v1.copy();
		assertNotSame(v1, v2);
		assertEquals(v1, v2);
		v2.subtract(simpleVector2());
		assertEquals(Math.PI - 2.3, v2.sum(), EPSILON);
		assertEquals(Math.PI, v1.sum(), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#subtract(org.grouplens.reflens.data.RatingVector)}.
	 */
	@Test
	public void testSubtract() {
		RatingVector v = emptyVector();
		v.subtract(singleton());
		assertTrue(v.isEmpty());
		
		v = simpleVector2();
		v.subtract(singleton());
		assertEquals(2, v.get(3), EPSILON);
		assertEquals(2.3 - Math.PI, v.get(5), EPSILON);
		assertEquals(1.7, v.get(8), EPSILON);
		
		v = singleton();
		assertEquals(Math.PI, v.sum(), EPSILON);
		v.subtract(simpleVector2());
		assertEquals(Math.PI - 2.3, v.get(5), EPSILON);
		assertEquals(Math.PI - 2.3, v.sum(), EPSILON);
		
		v = simpleVector();
		v.subtract(simpleVector2());
		assertEquals(-0.5, v.get(3), EPSILON);
		assertEquals(3.5, v.get(7), EPSILON);
		assertEquals(0.3, v.get(8), EPSILON);
		
		v = simpleVector();
		v.subtract(singleton());
		assertEquals(simpleVector(), v);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#add(org.grouplens.reflens.data.RatingVector)}.
	 */
	@Test
	public void testAdd() {
		RatingVector v = emptyVector();
		v.add(singleton());
		assertTrue(v.isEmpty());
		
		v = simpleVector2();
		v.add(singleton());
		assertEquals(2, v.get(3), EPSILON);
		assertEquals(2.3 + Math.PI, v.get(5), EPSILON);
		assertEquals(1.7, v.get(8), EPSILON);
		
		v = singleton();
		assertEquals(Math.PI, v.sum(), EPSILON);
		v.add(simpleVector2());
		assertEquals(Math.PI + 2.3, v.get(5), EPSILON);
		assertEquals(Math.PI + 2.3, v.sum(), EPSILON);
		
		v = simpleVector();
		v.add(simpleVector2());
		assertEquals(3.5, v.get(3), EPSILON);
		assertEquals(3.5, v.get(7), EPSILON);
		assertEquals(3.7, v.get(8), EPSILON);
		
		v = simpleVector();
		v.add(singleton());
		assertEquals(simpleVector(), v);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#dot(org.grouplens.reflens.data.RatingVector)}.
	 */
	@Test
	public void testDot() {
		assertEquals(0, emptyVector().dot(emptyVector()), EPSILON);
		assertEquals(0, emptyVector().dot(simpleVector()), EPSILON);
		assertEquals(0, singleton().dot(simpleVector()), EPSILON);
		assertEquals(0, simpleVector().dot(singleton()), EPSILON);
		assertEquals(6.4, simpleVector().dot(simpleVector2()), EPSILON);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#userRatingVector(java.util.Collection)}.
	 */
	@Test
	public void testUserRatingVector() {
		Collection<Rating> ratings = new ArrayList<Rating>();
		ratings.add(new Rating(5, 7, 3.5));
		ratings.add(new Rating(5, 3, 1.5));
		ratings.add(new Rating(5, 8, 2));
		RatingVector v = RatingVector.userRatingVector(ratings);
		assertEquals(3, v.size());
		assertEquals(7, v.sum(), EPSILON);
		assertEquals(simpleVector(), v);
	}

	/**
	 * Test method for {@link org.grouplens.reflens.data.RatingVector#itemRatingVector(java.util.Collection)}.
	 */
	@Test
	public void testItemRatingVector() {
		Collection<Rating> ratings = new ArrayList<Rating>();
		ratings.add(new Rating(7, 5, 3.5));
		ratings.add(new Rating(3, 5, 1.5));
		ratings.add(new Rating(8, 5, 2));
		RatingVector v = RatingVector.itemRatingVector(ratings);
		assertEquals(3, v.size());
		assertEquals(7, v.sum(), EPSILON);
		assertEquals(simpleVector(), v);
	}
}
