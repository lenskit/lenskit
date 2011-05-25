package org.grouplens.lenskit.slopeone;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.slopeone.DeviationComputer;
import org.grouplens.lenskit.slopeone.SlopeOneRatingPredictor;
import org.junit.Test;

public class TestSlopeOneRatingPredictor {

	@Test
	public void testPredict1() {

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
		RatingDataAccessObject dao = manager.open();
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(dao);
		PackedRatingSnapshot snap = snapBuilder.build();
		SlopeOneModelBuilder builder = new SlopeOneModelBuilder();
		builder.setRatingSnapshot(snap);
		builder.setDeviationComputer(new DeviationComputer(0));
		SlopeOneModel model = builder.build();
		SlopeOneRatingPredictor predictor = new SlopeOneRatingPredictor(dao,model);
		assertEquals(2.3333, predictor.predict(2, 9).getScore(), 0.0001);
		assertEquals(4.3333, predictor.predict(3, 6).getScore(), 0.0001);
		assertEquals(2.0000, predictor.predict(4, 6).getScore(), 0.0001);
		assertEquals(2.0000, predictor.predict(4, 9).getScore(), 0.0001);
		assertEquals(2.5000, predictor.predict(5, 6).getScore(), 0.0001);
		assertEquals(3.0000, predictor.predict(5, 7).getScore(), 0.0001);
		assertEquals(3.5000, predictor.predict(5, 9).getScore(), 0.0001);
		assertEquals(1.5000, predictor.predict(6, 6).getScore(), 0.0001);
		assertEquals(2.0000, predictor.predict(6, 7).getScore(), 0.0001);
		assertEquals(2.5000, predictor.predict(6, 9).getScore(), 0.0001);
	}
	
	@Test
	public void testPredict2() {
		List<Rating> rs = new ArrayList<Rating>();
		rs.add(new SimpleRating(1, 4, 3.5));
		rs.add(new SimpleRating(2, 4, 5));
		rs.add(new SimpleRating(3, 5, 4.25));
		rs.add(new SimpleRating(2, 6, 3));
		rs.add(new SimpleRating(1, 7, 4));
		rs.add(new SimpleRating(2, 7, 4));
		rs.add(new SimpleRating(3, 7, 1.5));
		RatingCollectionDAO.Manager manager = new RatingCollectionDAO.Manager(rs);
		RatingDataAccessObject dao = manager.open();
		PackedRatingSnapshot.Builder snapBuilder = new PackedRatingSnapshot.Builder(dao);
		PackedRatingSnapshot snap = snapBuilder.build();
		SlopeOneModelBuilder builder = new SlopeOneModelBuilder();
		builder.setRatingSnapshot(snap);
		builder.setDeviationComputer(new DeviationComputer(0));
		SlopeOneModel model = builder.build();
		SlopeOneRatingPredictor predictor = new SlopeOneRatingPredictor(dao,model);
		assertEquals(6.7500, predictor.predict(1, 5).getScore(), 0.0001);
		assertEquals(2.2500, predictor.predict(1, 6).getScore(), 0.0001);
		assertEquals(6.7500, predictor.predict(2, 5).getScore(), 0.0001);
		assertEquals(1.7500, predictor.predict(3, 4).getScore(), 0.0001);
		assertEquals(0.5000, predictor.predict(3, 6).getScore(), 0.0001);
	}
}