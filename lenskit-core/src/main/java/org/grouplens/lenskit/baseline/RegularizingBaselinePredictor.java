package org.grouplens.lenskit.baseline;

import javax.inject.Provider;

import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;


public class RegularizingBaselinePredictor extends AbstractBaselinePredictor {
	private class Builder implements Provider<PackedPreferenceSnapshot> {
		private PreferenceSnapshot snapshot;
		public Builder(PreferenceSnapshot data) {
			this.snapshot = data;
		}

		@Override
		public PackedPreferenceSnapshot get() {
			// TODO Auto-generated method stub
			return snapshot;
		}
		
	}
	private double learningRate;
	private double lambda;
	private double avg;
	private double rmse;
	private double oldRmse;
	private double uoff[];
	private double ioff[];
	private Builder builder;
	
	public RegularizingBaselinePredictor(PreferenceSnapshot data) {
		learningRate = 0.005;
		lambda = 0.002;
		rmse = Double.MAX_VALUE;
		oldRmse = 0.0;
		builder = new Builder(data);
		uoff = new double[builder.snapshot.getUserIds().size()];
		ioff = new double[builder.snapshot.getUserIds().size()];
	}
	
	public double predict(long user, long item) {
		int uid = builder.snapshot.userIndex().getIndex(user);
		int iid = builder.snapshot.itemIndex().getIndex(item);
		return avg + uoff[uid] + ioff[iid];
	}
	
	@Override
	public void predict(long user, SparseVector ratings,
			MutableSparseVector output, boolean predictSet) {
		
		avg = 0.0;
		for (VectorEntry e: ratings.fast()) {
			avg += e.getValue();
		}
		avg /= ratings.size();
		
		while (!stopper.isFinished(epoch, oldRmse - rmse)) {
			double sse = 0;
			for (VectorEntry e : ratings.fast()) {
				final long item = e.getKey();
				double p = predict(user, item);
				double err = e.getValue() - p;
				int uid = builder.snapshot.userIndex().getIndex(user);
				int iid = builder.snapshot.itemIndex().getIndex(item); 
				uoff[uid] += learningRate * (err - lambda*uoff[uid]);
				ioff[iid] += learningRate * (err - lambda*ioff[iid]);
				sse += err*err;
			}
			rmse = Math.sqrt(sse/ratings.size());
		}
	}
}
