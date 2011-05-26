package org.grouplens.lenskit.data.snapshot;

import static org.junit.Assert.*;

import it.unimi.dsi.fastutil.longs.LongCollection;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.util.FastCollection;
import org.junit.Before;
import org.junit.Test;

public class TestPackedRatingSnapshot {
	
	private PackedRatingSnapshot snap;
	
	@Before
	public void setup() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 7, 4, 1));
		rs.add(new SimpleRating(3, 7, 3, 1));
		rs.add(new SimpleRating(4, 7, 5, 1));
		rs.add(new SimpleRating(4, 7, 4, 2));
		rs.add(new SimpleRating(5, 7, 3, 1));
		rs.add(new SimpleRating(6, 7, 5, 1));
		rs.add(new SimpleRating(1, 8, 4, 1));
		rs.add(new SimpleRating(1, 8, 5, 2));
		rs.add(new SimpleRating(3, 8, 3, 1));
		rs.add(new SimpleRating(4, 8, 2, 1));
		rs.add(new SimpleRating(5, 8, 3, 1));
		rs.add(new SimpleRating(5, 8, 5, 2));
		rs.add(new SimpleRating(6, 8, 5, 1));
		rs.add(new SimpleRating(7, 8, 2, 1));
		rs.add(new SimpleRating(1, 9, 3, 1));
		rs.add(new SimpleRating(3, 9, 4, 1));
		rs.add(new SimpleRating(4, 9, 5, 1));
		rs.add(new SimpleRating(7, 9, 2, 1));
		rs.add(new SimpleRating(7, 9, 3, 2));
		rs.add(new SimpleRating(4, 10, 4, 1));
		rs.add(new SimpleRating(7, 10, 4, 1));
		rs.add(new SimpleRating(1, 11, 5, 1));
		rs.add(new SimpleRating(3, 11, 5, 2));
		rs.add(new SimpleRating(4, 11, 5, 1));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder builder = new PackedRatingSnapshot.Builder(manager.open());
		snap = builder.build();
	}

	
	@Test
	public void testGetUserIds() {
		LongCollection users = snap.getUserIds();
		assertTrue(users.contains(1));
		assertTrue(users.contains(3));
		assertTrue(users.contains(4));
		assertTrue(users.contains(5));
		assertTrue(users.contains(6));
		assertTrue(users.contains(7));
	}
	
	@Test
	public void testGetItemIds() {
		LongCollection items = snap.getItemIds();
		assertEquals(5, items.size());
		assertTrue(items.contains(7));
		assertTrue(items.contains(8));
		assertTrue(items.contains(9));
		assertTrue(items.contains(10));
		assertTrue(items.contains(11));
	}
	
	@Test
	public void testUserIndex() {
		Index ind = snap.userIndex();
		assertTrue(ind instanceof Index);
		assertEquals(6, ind.getObjectCount());
		assertTrue(ind.getIds().contains(1));
		assertTrue(ind.getIds().contains(3));
		assertTrue(ind.getIds().contains(4));
		assertTrue(ind.getIds().contains(5));
		assertTrue(ind.getIds().contains(6));
		assertTrue(ind.getIds().contains(7));
		assertEquals(0, ind.getIndex(1));
		assertEquals(1, ind.getIndex(3));
		assertEquals(2, ind.getIndex(4));
		assertEquals(3, ind.getIndex(5));
		assertEquals(4, ind.getIndex(6));
		assertEquals(5, ind.getIndex(7));
		assertEquals(1, ind.getId(0));
		assertEquals(3, ind.getId(1));
		assertEquals(4, ind.getId(2));
		assertEquals(5, ind.getId(3));
		assertEquals(6, ind.getId(4));
		assertEquals(7, ind.getId(5));
	}
	
	@Test
	public void testItemIndex() {
		Index ind = snap.itemIndex();
		assertTrue(ind instanceof Index);
		assertEquals(5, ind.getObjectCount());
		assertTrue(ind.getIds().contains(7));
		assertTrue(ind.getIds().contains(8));
		assertTrue(ind.getIds().contains(9));
		assertTrue(ind.getIds().contains(10));
		assertTrue(ind.getIds().contains(11));
		assertEquals(0, ind.getIndex(7));
		assertEquals(1, ind.getIndex(8));
		assertEquals(2, ind.getIndex(9));
		assertEquals(3, ind.getIndex(10));
		assertEquals(4, ind.getIndex(11));
		assertEquals(7, ind.getId(0));
		assertEquals(8, ind.getId(1));
		assertEquals(9, ind.getId(2));
		assertEquals(10, ind.getId(3));
		assertEquals(11, ind.getId(4));
	}
	
	@Test
	public void testGetRatings() {
		FastCollection<IndexedRating> ratings = snap.getRatings();
		assertEquals(20, ratings.size());
		assertTrue(ratings.contains(new SimpleRating(1, 7, 4, 1)));
		assertTrue(ratings.contains(new SimpleRating(3, 7, 3, 1)));
		assertTrue(ratings.contains(new SimpleRating(4, 7, 4, 2)));
		assertTrue(ratings.contains(new SimpleRating(5, 7, 3, 1)));
		assertTrue(ratings.contains(new SimpleRating(6, 7, 5, 1)));
		assertTrue(ratings.contains(new SimpleRating(1, 8, 5, 2)));
		assertTrue(ratings.contains(new SimpleRating(3, 8, 3, 1)));
		assertTrue(ratings.contains(new SimpleRating(4, 8, 2, 1)));
		assertTrue(ratings.contains(new SimpleRating(5, 8, 5, 2)));
		assertTrue(ratings.contains(new SimpleRating(6, 8, 5, 1)));
		assertTrue(ratings.contains(new SimpleRating(7, 8, 2, 1)));
		assertTrue(ratings.contains(new SimpleRating(1, 9, 3, 1)));
		assertTrue(ratings.contains(new SimpleRating(3, 9, 4, 1)));
		assertTrue(ratings.contains(new SimpleRating(4, 9, 5, 1)));
		assertTrue(ratings.contains(new SimpleRating(7, 9, 3, 2)));
		assertTrue(ratings.contains(new SimpleRating(4, 10, 4, 1)));
		assertTrue(ratings.contains(new SimpleRating(7, 10, 4, 1)));
		assertTrue(ratings.contains(new SimpleRating(1, 11, 5, 1)));
		assertTrue(ratings.contains(new SimpleRating(3, 11, 5, 2)));
		assertTrue(ratings.contains(new SimpleRating(4, 11, 5, 1)));
	}
	
	@Test
	public void testGetUserRatings() {
		FastCollection<IndexedRating> ratings = snap.getUserRatings(1);
		assertEquals(4, ratings.size());
		assertTrue(ratings.contains(new SimpleRating(1, 7, 4, 1)));
		assertTrue(ratings.contains(new SimpleRating(1, 8, 5, 2)));
		assertTrue(ratings.contains(new SimpleRating(1, 9, 3, 1)));
		assertTrue(ratings.contains(new SimpleRating(1, 11, 5, 1)));
		
		ratings = snap.getUserRatings(2);
		assertEquals(0, ratings.size());
		
		ratings = snap.getUserRatings(3);
		assertEquals(4, ratings.size());
		assertTrue(ratings.contains(new SimpleRating(3, 7, 3, 1)));
		assertTrue(ratings.contains(new SimpleRating(3, 8, 3, 1)));
		assertTrue(ratings.contains(new SimpleRating(3, 9, 4, 1)));
		assertTrue(ratings.contains(new SimpleRating(3, 11, 5, 2)));
		
		ratings = snap.getUserRatings(4);
		assertTrue(ratings.contains(new SimpleRating(4, 7, 4, 2)));
		assertTrue(ratings.contains(new SimpleRating(4, 8, 2, 1)));
		assertTrue(ratings.contains(new SimpleRating(4, 9, 5, 1)));
		assertTrue(ratings.contains(new SimpleRating(4, 10, 4, 1)));
		assertTrue(ratings.contains(new SimpleRating(4, 11, 5, 1)));
		
		ratings = snap.getUserRatings(5);
		assertEquals(2, ratings.size());
		assertTrue(ratings.contains(new SimpleRating(5, 7, 3, 1)));
		assertTrue(ratings.contains(new SimpleRating(5, 8, 5, 2)));
		
		ratings = snap.getUserRatings(6);
		assertEquals(2, ratings.size());
		assertTrue(ratings.contains(new SimpleRating(6, 7, 5, 1)));
		assertTrue(ratings.contains(new SimpleRating(6, 8, 5, 1)));
		
		ratings = snap.getUserRatings(7);
		assertEquals(3, ratings.size());
		assertTrue(ratings.contains(new SimpleRating(7, 8, 2, 1)));
		assertTrue(ratings.contains(new SimpleRating(7, 9, 3, 2)));
		assertTrue(ratings.contains(new SimpleRating(7, 10, 4, 1)));
	}
}
