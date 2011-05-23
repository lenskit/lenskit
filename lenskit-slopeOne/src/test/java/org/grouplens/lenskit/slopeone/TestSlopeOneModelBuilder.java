package org.grouplens.lenskit.slopeone;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.slopeone.DeviationComputer;
import org.grouplens.lenskit.slopeone.SlopeOneModel;
import org.grouplens.lenskit.slopeone.SlopeOneModelBuilder;
import org.junit.Test;

public class TestSlopeOneModelBuilder {

	@Test
	public void testBuild1() {

		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 5, 2));
		rs.add(new SimpleRating(2, 5, 4));
		rs.add(new SimpleRating(1, 3, 5));
		rs.add(new SimpleRating(2, 3, 4));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(manager.open());
		PackedRatingSnapshot snapshot = snapBuilder.build();		
		SlopeOneModelBuilder builder1 = new SlopeOneModelBuilder();
		builder1.setRatingSnapshot(snapshot);
		builder1.setDeviationComputer(new DeviationComputer(0));
		SlopeOneModel model1 = builder1.build();
		assertEquals(2, model1.getCoratingMatrix().get(5, 3));
		assertEquals(2, model1.getCoratingMatrix().get(3, 5));
		assertEquals(-1.5000, model1.getDeviationMatrix().get(5, 3),0.0001);
		assertEquals(1.5000, model1.getDeviationMatrix().get(3,5), 0.0001);
	}

	@Test
	public void testBuild2() {

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
		SlopeOneModelBuilder builder2 = new SlopeOneModelBuilder();
		builder2.setRatingSnapshot(snapshot);
		builder2.setDeviationComputer(new DeviationComputer(0));
		SlopeOneModel model2 = builder2.build();
		assertEquals(3, model2.getCoratingMatrix().get(4, 5));
		assertEquals(3, model2.getCoratingMatrix().get(5, 4));
		assertEquals(3, model2.getCoratingMatrix().get(4, 6));
		assertEquals(3, model2.getCoratingMatrix().get(6, 4));
		assertEquals(3, model2.getCoratingMatrix().get(5, 6));
		assertEquals(3, model2.getCoratingMatrix().get(6, 5));
		assertEquals(1.3333, model2.getDeviationMatrix().get(4, 6), 0.0001);
		assertEquals(-1.3333, model2.getDeviationMatrix().get(6, 4), 0.0001);
		assertEquals(1.3333, model2.getDeviationMatrix().get(4, 5), 0.0001);
		assertEquals(-1.3333, model2.getDeviationMatrix().get(5, 4), 0.0001);
		assertEquals(0, model2.getDeviationMatrix().get(5, 6), 0.0001);
		assertEquals(0, model2.getDeviationMatrix().get(6, 5), 0.0001);
	}

	@Test
	public void testBuild3() {

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
		rs.add(new SimpleRating(3, 9, 4));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(manager.open());
		PackedRatingSnapshot snapshot = snapBuilder.build();		
		SlopeOneModelBuilder builder3 = new SlopeOneModelBuilder();
		builder3.setRatingSnapshot(snapshot);
		builder3.setDeviationComputer(new DeviationComputer(0));
		SlopeOneModel model3 = builder3.build();
		assertEquals(2, model3.getCoratingMatrix().get(6, 7));
		assertEquals(2, model3.getCoratingMatrix().get(7, 6));
		assertEquals(2, model3.getCoratingMatrix().get(6, 8));
		assertEquals(2, model3.getCoratingMatrix().get(8, 6));
		assertEquals(1, model3.getCoratingMatrix().get(6, 9));
		assertEquals(1, model3.getCoratingMatrix().get(9, 6));
		assertEquals(4, model3.getCoratingMatrix().get(7, 8));
		assertEquals(4, model3.getCoratingMatrix().get(8, 7));
		assertEquals(2, model3.getCoratingMatrix().get(7, 9));
		assertEquals(2, model3.getCoratingMatrix().get(9, 7));
		assertEquals(2, model3.getCoratingMatrix().get(8, 9));
		assertEquals(2, model3.getCoratingMatrix().get(9, 8));
		assertEquals(0.5000, model3.getDeviationMatrix().get(6, 7), 0.0001);
		assertEquals(-0.5000, model3.getDeviationMatrix().get(7, 6), 0.0001);
		assertEquals(-0.5000, model3.getDeviationMatrix().get(6, 8), 0.0001);
		assertEquals(0.5000, model3.getDeviationMatrix().get(8, 6), 0.0001);
		assertEquals(1.0000, model3.getDeviationMatrix().get(6, 9), 0.0001);
		assertEquals(-1.0000, model3.getDeviationMatrix().get(9, 6), 0.0001);
		assertEquals(0, model3.getDeviationMatrix().get(7, 8), 0.0001);
		assertEquals(0, model3.getDeviationMatrix().get(8, 7), 0.0001);
		assertEquals(0.5000, model3.getDeviationMatrix().get(7, 9), 0.0001);
		assertEquals(-0.5000, model3.getDeviationMatrix().get(9, 7), 0.0001);
		assertEquals(-0.5000, model3.getDeviationMatrix().get(8, 9), 0.0001);
		assertEquals(0.5000, model3.getDeviationMatrix().get(9, 8), 0.0001);
	}
	
	@Test
	public void testBuild4() {
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
		SlopeOneModel model = builder.build();
		assertEquals(0, model.getCoratingMatrix().get(4, 5));
		assertEquals(0, model.getCoratingMatrix().get(5, 4));
		assertEquals(1, model.getCoratingMatrix().get(4, 6));
		assertEquals(1, model.getCoratingMatrix().get(6, 4));
		assertEquals(2, model.getCoratingMatrix().get(4, 7));
		assertEquals(2, model.getCoratingMatrix().get(7, 4));
		assertEquals(0, model.getCoratingMatrix().get(5, 6));
		assertEquals(0, model.getCoratingMatrix().get(6, 5));
		assertEquals(1, model.getCoratingMatrix().get(5, 7));
		assertEquals(1, model.getCoratingMatrix().get(7, 5));
		assertEquals(1, model.getCoratingMatrix().get(6, 7));
		assertEquals(1, model.getCoratingMatrix().get(7, 6));
		assertEquals(Double.NaN, model.getDeviationMatrix().get(4, 5), 0);
		assertEquals(Double.NaN, model.getDeviationMatrix().get(5, 4), 0);
		assertEquals(2.0000, model.getDeviationMatrix().get(4, 6), 0.0001);
		assertEquals(-2.0000, model.getDeviationMatrix().get(6, 4), 0.0001);
		assertEquals(0.2500, model.getDeviationMatrix().get(4, 7), 0.0001);
		assertEquals(-0.2500, model.getDeviationMatrix().get(7, 4), 0.0001);
		assertEquals(Double.NaN, model.getDeviationMatrix().get(5, 6), 0);
		assertEquals(Double.NaN, model.getDeviationMatrix().get(6, 5), 0);
		assertEquals(2.7500, model.getDeviationMatrix().get(5, 7), 0.0001);
		assertEquals(-2.7500, model.getDeviationMatrix().get(7, 5), 0.0001);
		assertEquals(-1.000, model.getDeviationMatrix().get(6, 7), 0.0001);
		assertEquals(1.000, model.getDeviationMatrix().get(7, 6), 0.0001);
	}

}
