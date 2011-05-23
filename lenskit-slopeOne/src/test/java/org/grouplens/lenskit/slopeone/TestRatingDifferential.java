package org.grouplens.lenskit.slopeone;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.slopeone.DeviationComputer;
import org.grouplens.lenskit.slopeone.SlopeOneModelBuilder;
import org.junit.Test;

public class TestRatingDifferential {

	
	@Test
	public void test1() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 5, 2));
		rs.add(new SimpleRating(2, 5, 4));
		rs.add(new SimpleRating(1, 3, 5));
		rs.add(new SimpleRating(2, 3, 4));
		rs.add(new SimpleRating(3, 5, 4));
		rs.add(new SimpleRating(3, 6, 4));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(manager.open());
		PackedRatingSnapshot snapshot = snapBuilder.build();		
		SlopeOneModelBuilder builder = new SlopeOneModelBuilder();
		builder.setRatingSnapshot(snapshot);
		builder.setDeviationComputer(new DeviationComputer(0));
		assertEquals(-3.0000, builder.getRatingDifferential(1, 5, 3), 0.0001);
		assertEquals(3.0000, builder.getRatingDifferential(1, 3, 5), 0.0001);
		assertEquals(0, builder.getRatingDifferential(2, 5, 3), 0.0001);
		assertEquals(0, builder.getRatingDifferential(2, 3, 5), 0.0001);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 3, 5), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 5, 6), 0);
	}
	
	@Test
	public void test2() {
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
		SlopeOneModelBuilder builder = new SlopeOneModelBuilder();
		builder.setRatingSnapshot(snapshot);
		builder.setDeviationComputer(new DeviationComputer(0));
		assertEquals(1.0000, builder.getRatingDifferential(1, 4, 5), 0.0001);
		assertEquals(-1.0000, builder.getRatingDifferential(1, 5, 4), 0.0001);
		assertEquals(0, builder.getRatingDifferential(2, 4, 5), 0.0001);
		assertEquals(0, builder.getRatingDifferential(2, 5, 4), 0.0001);
		assertEquals(3.0000, builder.getRatingDifferential(3, 4, 5), 0.0001);
		assertEquals(-3.0000, builder.getRatingDifferential(3, 5, 4), 0.0001);
		assertEquals(3.0000, builder.getRatingDifferential(1, 4, 6), 0.0001);
		assertEquals(-3.0000, builder.getRatingDifferential(1, 6, 4), 0.0001);
		assertEquals(0, builder.getRatingDifferential(2, 4, 6), 0.0001);
		assertEquals(0, builder.getRatingDifferential(2, 6, 4), 0.0001);
		assertEquals(1.0000, builder.getRatingDifferential(3, 4, 6), 0.0001);
		assertEquals(-1.0000, builder.getRatingDifferential(3, 6, 4), 0.0001);
		assertEquals(2.0000, builder.getRatingDifferential(1, 5, 6), 0.0001);
		assertEquals(-2.0000, builder.getRatingDifferential(1, 6, 5), 0.0001);
		assertEquals(0, builder.getRatingDifferential(2, 5, 6), 0.0001);
		assertEquals(0, builder.getRatingDifferential(2, 6, 5), 0.0001);
		assertEquals(-2.0000, builder.getRatingDifferential(3, 5, 6), 0.0001);
		assertEquals(2.0000, builder.getRatingDifferential(3, 6, 5), 0.0001);
	}
	
	@Test
	public void test3() {
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
		SlopeOneModelBuilder builder = new SlopeOneModelBuilder();
		builder.setRatingSnapshot(snap);
		builder.setDeviationComputer(new DeviationComputer(0));
		
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 4, 5), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 5, 4), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 4, 6), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 6, 4), 0);
		assertEquals(-0.5000, builder.getRatingDifferential(1, 4, 7), 0.0001);
		assertEquals(0.5000, builder.getRatingDifferential(1, 7, 4), 0.0001);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 5, 6), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 6, 5), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 5, 7), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 7, 5), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 6, 7), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(1, 7, 6), 0);
		
		assertEquals(Double.NaN, builder.getRatingDifferential(2, 4, 5), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(2, 5, 4), 0);
		assertEquals(2.0000, builder.getRatingDifferential(2, 4, 6), 0.0001);
		assertEquals(-2.0000, builder.getRatingDifferential(2, 6, 4), 0.0001);
		assertEquals(1.0000, builder.getRatingDifferential(2, 4, 7), 0.0001);
		assertEquals(-1.0000, builder.getRatingDifferential(2, 7, 4), 0.0001);
		assertEquals(Double.NaN, builder.getRatingDifferential(2, 5, 6), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(2, 6, 5), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(2, 5, 7), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(2, 7, 5), 0);
		assertEquals(-1.0000, builder.getRatingDifferential(2, 6, 7), 0.0001);
		assertEquals(1.0000, builder.getRatingDifferential(2, 7, 6), 0.0001);
		
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 4, 5), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 5, 4), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 4, 6), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 6, 4), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 4, 7), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 7, 4), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 5, 6), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 6, 5), 0);
		assertEquals(2.7500, builder.getRatingDifferential(3, 5, 7), 0.0001);
		assertEquals(-2.7500, builder.getRatingDifferential(3, 7, 5), 0.0001);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 6, 7), 0);
		assertEquals(Double.NaN, builder.getRatingDifferential(3, 7, 6), 0);
	}
}
