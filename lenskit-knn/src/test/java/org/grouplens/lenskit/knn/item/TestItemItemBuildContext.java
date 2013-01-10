package org.grouplens.lenskit.knn.item;

import static org.junit.Assert.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

import org.grouplens.lenskit.knn.item.model.ItemItemBuildContext;
import org.grouplens.lenskit.knn.item.model.ItemItemBuildContext.ItemVecPair;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;

public class TestItemItemBuildContext {

	/**
	 * Test ItemItemBuildContext when all items have rating data.
	 */
	@Test
	public void testAllItemsData() {
		LongLinkedOpenHashSet items = new LongLinkedOpenHashSet();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);

		long[] userIds = {101, 102, 103, 104};
		double[] ratings1 = {4.0, 3.0, 2.5, 2.0};
		double[] ratings2 = {3.0, 2.5, 4.0, 1.0};
		double[] ratings3 = {5.0, 3.5, 0.5, 1.0};
		double[] ratings4 = {4.5, 3.0, 3.5, 1.5};		
		SparseVector v1 = MutableSparseVector.wrap(userIds, ratings1);
		SparseVector v2 = MutableSparseVector.wrap(userIds, ratings2);
		SparseVector v3 = MutableSparseVector.wrap(userIds, ratings3);
		SparseVector v4 = MutableSparseVector.wrap(userIds, ratings4);
		
		Long2ObjectOpenHashMap<SparseVector> ratingMap = new Long2ObjectOpenHashMap<SparseVector>();
		ratingMap.put(1, v1);
		ratingMap.put(2, v2);
		ratingMap.put(3, v3);
		ratingMap.put(4, v4);
		ItemItemBuildContext context = new ItemItemBuildContext(items, ratingMap);
		
		testRatingIntegrity(ratingMap, context);
	}

	/**
	 * Test ItemItemBuildContext when some items have rating data.
	 */
	@Test
	public void testSomeItemsData() {
		LongLinkedOpenHashSet items = new LongLinkedOpenHashSet();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);

		long[] userIds = {101, 102, 103, 104};
		double[] ratings1 = {4.0, 3.0, 2.5, 2.0};
		double[] ratings4 = {4.5, 3.0, 3.5, 1.5};		
		SparseVector v1 = MutableSparseVector.wrap(userIds, ratings1);
		SparseVector v4 = MutableSparseVector.wrap(userIds, ratings4);
		
		Long2ObjectOpenHashMap<SparseVector> ratingMap = new Long2ObjectOpenHashMap<SparseVector>();
		ratingMap.put(1, v1);
		ratingMap.put(2, new MutableSparseVector());
		ratingMap.put(3, new MutableSparseVector());
		ratingMap.put(4, v4);
		ItemItemBuildContext context = new ItemItemBuildContext(items, ratingMap);
		
		testRatingIntegrity(ratingMap, context);
	}
	
	/**
	 * Test ItemItemBuildContext when no items have rating data.
	 */
	@Test
	public void testNoItemsData() {
		LongLinkedOpenHashSet items = new LongLinkedOpenHashSet();
		items.add(1);
		items.add(2);
		items.add(3);
		items.add(4);
		
		Long2ObjectOpenHashMap<SparseVector> ratingMap = new Long2ObjectOpenHashMap<SparseVector>();
		for (long item : items) {
			ratingMap.put(item, new MutableSparseVector());
		}
		ItemItemBuildContext context = new ItemItemBuildContext(items, ratingMap);
		
		testRatingIntegrity(ratingMap, context);
	}
	
	/**
	 * Test ItemItemBuildContext when there is no rating data.
	 */
	@Test
	public void testEmpty() {
		LongLinkedOpenHashSet items = new LongLinkedOpenHashSet();
		Long2ObjectOpenHashMap<SparseVector> ratingMap = new Long2ObjectOpenHashMap<SparseVector>();
		ItemItemBuildContext context = new ItemItemBuildContext(items, ratingMap);
		
		testRatingIntegrity(ratingMap, context);
	}
		
	private void testRatingIntegrity(Long2ObjectMap<SparseVector> trueRatings, ItemItemBuildContext context) {		
		for (long itemId : context.getItems()) {
			assertEquals(trueRatings.get(itemId), context.itemVector(itemId));
		}
		
		for (ItemVecPair pair : context.getItemPairs()) {
			assertEquals(trueRatings.get(pair.itemId1), pair.vec1);
			assertEquals(trueRatings.get(pair.itemId2), pair.vec2);
		}
	}
}
