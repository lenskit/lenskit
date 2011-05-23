package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.IndexedRating;

public class SlopeOneModelBuilder extends RecommenderComponentBuilder<SlopeOneModel> {

	private CoratingMatrix commonUsers;
	private DeviationMatrix deviations;
	private BaselinePredictor baseline;
	private DeviationComputer devComp;

	@Override
	public SlopeOneModel build() {
		if (commonUsers == null || deviations == null) buildDeviations(); //??
		return new SlopeOneModel(commonUsers, deviations, baseline);

	}

	final double getRatingDifferential(long user, long item1, long item2) {

		if (item1 == item2) return Double.NaN;
		double rating1 = Double.NaN;
		double rating2 = Double.NaN;
		for (IndexedRating rating : snapshot.getUserRatings(user)) {
			if (rating.getItemId() == item1) rating1 = rating.getRating();
			else if (rating.getItemId() == item2) rating2 = rating.getRating();
		}
		if (rating1 != Double.NaN && rating2 != Double.NaN) return rating1 - rating2;
		else return Double.NaN;
	}

	private void buildDeviations() {
		
		commonUsers = new CoratingMatrix(snapshot);
		deviations = new DeviationMatrix(snapshot);
		long[] items = snapshot.getItemIds().toLongArray();
		for (int i = 0; i < items.length-1; i++) {
			for (int j = i+1; j < items.length; j++) {
				int nusers = 0;
				double totalDiff = 0;
				for (long currentUser : snapshot.getUserIds()) {
					double differential = getRatingDifferential(currentUser, items[i], items[j]);
					if (!Double.isNaN(differential)) {
						nusers++;
						totalDiff += differential;
					}
				}
				commonUsers.put(items[i], items[j], nusers);
				if (nusers != 0) deviations.put(items[i], items[j], devComp.findDeviation(totalDiff, nusers));
				else deviations.put(items[i], items[j], Double.NaN);
			}
		}
	}

	public void setBaselinePredictor(BaselinePredictor predictor) {
		baseline = predictor;
	}

	public void setCoratingMatrix(CoratingMatrix coData) {
		commonUsers = coData;
	}

	public void setDeviationMatrix(DeviationMatrix devData) {
		deviations = devData;
	}

	public void setDeviationComputer(DeviationComputer comp) {
		devComp = comp;
	}
}
