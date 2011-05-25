package org.grouplens.lenskit.slopeone;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.slopeone.DeviationMatrix;
import org.junit.Test;


public class TestDeviationMatrix {
	
	@Test
	public void testPutGet1() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 5, 2));
		rs.add(new SimpleRating(2, 5, 4));
		rs.add(new SimpleRating(1, 3, 5));
		rs.add(new SimpleRating(2, 3, 4));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(manager.open());
		PackedRatingSnapshot snapshot = snapBuilder.build();
		DeviationMatrix data = new DeviationMatrix(snapshot);
		data.put(5, 3, -1.5);
		assertEquals(-1.5000, data.get(5, 3), 0.0001);
		assertEquals(1.5000, data.get(3, 5), 0.0001);		
	}
	
	@Test
	public void testPutGet2() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 4, 4));
		rs.add(new SimpleRating(2, 4, 5));
		rs.add(new SimpleRating(3, 4, 4));
		rs.add(new SimpleRating(1, 5, 3));
		rs.add(new SimpleRating(2, 5, 5));
		rs.add(new SimpleRating(3, 5, 1));
		rs.add(new SimpleRating(1, 6, 1));
		rs.add(new SimpleRating(2, 6, 5));
		rs.add(new SimpleRating(3, 6, 3));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(manager.open());
		PackedRatingSnapshot snapshot = snapBuilder.build();
		DeviationMatrix data = new DeviationMatrix(snapshot);
		data.put(4, 6, 1.3333);
		data.put(4, 5, 1.3333);
		data.put(5, 6, 0);
		assertEquals(1.3333, data.get(4, 6), 0.0001);
		assertEquals(-1.3333, data.get(6, 4), 0.0001);
		assertEquals(1.3333, data.get(4, 5), 0.0001);
		assertEquals(-1.3333, data.get(5, 4), 0.0001);
		assertEquals(0, data.get(5, 6), 0.0001);
		assertEquals(0, data.get(6, 5), 0.0001);		
	}
	
	@Test
	public void testPutGet3() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 6, 4));
		rs.add(new SimpleRating(2, 6, 2));
		rs.add(new SimpleRating(1, 7, 3));
		rs.add(new SimpleRating(2, 7, 2));
		rs.add(new SimpleRating(3, 7, 5));
		rs.add(new SimpleRating(4, 7, 2));
		rs.add(new SimpleRating(1, 8, 3));
		rs.add(new SimpleRating(2, 8, 4));
		rs.add(new SimpleRating(3, 8, 3));
		rs.add(new SimpleRating(4, 8, 2));
		rs.add(new SimpleRating(5, 8, 3));
		rs.add(new SimpleRating(6, 8, 2));
		rs.add(new SimpleRating(1, 9, 3));
		rs.add(new SimpleRating(3, 9, 3));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(manager.open());
		PackedRatingSnapshot snapshot = snapBuilder.build();
		DeviationMatrix data = new DeviationMatrix(snapshot);
		data.put(6, 7, 0.5);
		data.put(6, 8, -0.5);
		data.put(6, 9, 1);
		data.put(7, 8, 0);
		data.put(7, 9, 0.5);
		data.put(8, 9, -0.5);
		assertEquals(0.5000, data.get(6, 7), 0.0001);
		assertEquals(-0.5000, data.get(7, 6), 0.0001);
		assertEquals(-0.5000, data.get(6, 8), 0.0001);
		assertEquals(0.5000, data.get(8, 6), 0.0001);
		assertEquals(1.0000, data.get(6, 9), 0.0001);
		assertEquals(-1.0000, data.get(9, 6), 0.0001);
		assertEquals(0, data.get(7, 8), 0.0001);
		assertEquals(0, data.get(8, 7), 0.0001);
		assertEquals(0.5000, data.get(7, 9), 0.0001);
		assertEquals(-0.5000, data.get(9, 7), 0.0001);
		assertEquals(-0.5000, data.get(8, 9), 0.0001);
		assertEquals(0.5000, data.get(9, 8), 0.0001);
	}
	
	@Test
	public void testPutGet4() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 4, 3.5));
		rs.add(new SimpleRating(2, 4, 5));
		rs.add(new SimpleRating(3, 5, 4.25));
		rs.add(new SimpleRating(2, 6, 3));
		rs.add(new SimpleRating(1, 7, 4));
		rs.add(new SimpleRating(2, 7, 4));
		rs.add(new SimpleRating(3, 7, 1.5));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(manager.open());
		PackedRatingSnapshot snap = snapBuilder.build();
		DeviationMatrix data = new DeviationMatrix(snap);
		data.put(4, 5, Double.NaN);
		data.put(4, 6, 2);
		data.put(4, 7, 0.25);
		data.put(5, 6, Double.NaN);
		data.put(5, 7, 2.75);
		data.put(6, 7, -1);
		assertEquals(Double.NaN, data.get(4, 5), 0);
		assertEquals(Double.NaN, data.get(5, 4), 0);
		assertEquals(2.0000, data.get(4, 6), 0.0001);
		assertEquals(-2.0000, data.get(6, 4), 0.0001);
		assertEquals(0.2500, data.get(4, 7), 0.0001);
		assertEquals(-0.2500, data.get(7, 4), 0.0001);
		assertEquals(Double.NaN, data.get(5, 6), 0);
		assertEquals(Double.NaN, data.get(6, 5), 0);
		assertEquals(2.7500, data.get(5, 7), 0.0001);
		assertEquals(-2.7500, data.get(7, 5), 0.0001);
		assertEquals(-1.000, data.get(6, 7), 0.0001);
		assertEquals(1.000, data.get(7, 6), 0.0001);
	}
}