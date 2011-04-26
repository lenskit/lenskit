package org.grouplens.lenskit.plsi;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.grouplens.lenskit.DynamicRatingPredictor;
import org.grouplens.lenskit.data.context.PackedRatingBuildContext;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.dao.SimpleFileDAO;
import org.grouplens.lenskit.data.vector.SparseVector;

public class PLSIRecommenderBuilderTest extends TestCase {

	public void testBuild() throws IOException {
		File testDataFile = new File("src/test/java/PLSITestData.csv");
		SimpleFileDAO dao = new SimpleFileDAO(testDataFile, ",");
		RatingBuildContext ratings = PackedRatingBuildContext.make(dao.getSession());
		PLSIRecommenderBuilder builder = new PLSIRecommenderBuilder(10, 80, 4, 0.95);
		DynamicRatingPredictor predictor = builder.build(ratings, null);
		long[] items = {0, 3};
		double[] userRatings = {-2.0, 1.5};
		SparseVector ratingVector = SparseVector.wrap(items, userRatings);
		ArrayList<Long> itemsToRate = new ArrayList<Long>();
		itemsToRate.add(Long.valueOf(2));
		itemsToRate.add(Long.valueOf(4));
		SparseVector predictions = predictor.predict(-1, ratingVector, itemsToRate);
		for (Entry entry : predictions.fast()) {
			System.err.println("Rating for item " + entry.getLongKey() +
					": " + entry.getDoubleValue());
		}
		fail("Not yet implemented");
	}

}
