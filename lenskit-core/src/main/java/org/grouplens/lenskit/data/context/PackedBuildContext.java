package org.grouplens.lenskit.data.context;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;

import java.util.Arrays;
import java.util.List;

import javax.annotation.WillNotClose;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.Indexer;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.util.CollectionUtils;
import org.grouplens.lenskit.util.FastCollection;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A build context that an in-memory snapshot in packed arrays.
 * 
 * <p>By default, this class is injected as a singleton.  Deployments may want
 * to override that in the Guice module to allow multiple build contexts to be
 * built (e.g. every day).  Applications using LensKit will often want to use
 * explicit providers for build contexts.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Singleton
public class PackedBuildContext implements BuildContext {
	private PackedRatingData data;
	private List<IntList> userIndices;
	
	@Inject
	public PackedBuildContext(RatingDataAccessObject dao) {
		Cursor<Rating> ratings = dao.getRatings();
		try {
			packRatings(ratings);
		} finally {
			ratings.close();
		}
	}
	
	private void requireValid() {
		if (data == null)
			throw new IllegalStateException("build context closed");
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.context.BuildContext#getUserIds()
	 */
	@Override
	public LongCollection getUserIds() {
		requireValid();
		return data.userIndex.getIds();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.context.BuildContext#getItemIds()
	 */
	@Override
	public LongCollection getItemIds() {
		requireValid();
		return data.itemIndex.getIds();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.context.BuildContext#userIndex()
	 */
	@Override
	public Index userIndex() {
		requireValid();
		return data.userIndex;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.context.BuildContext#itemIndex()
	 */
	@Override
	public Index itemIndex() {
		requireValid();
		return data.itemIndex;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.context.BuildContext#getRatings()
	 */
	@Override
	public FastCollection<IndexedRating> getRatings() {
		return new PackedRatingCollection(data);
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.context.BuildContext#getUserRatings(long)
	 */
	@Override
	public FastCollection<IndexedRating> getUserRatings(long userId) {
	    requireValid();
		int uidx = data.userIndex.getIndex(userId);
		if (uidx < 0 || uidx >= userIndices.size())
		    return CollectionUtils.emptyFastCollection();
		else
		    return new PackedRatingCollection(data, userIndices.get(uidx));
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.context.BuildContext#close()
	 */
	@Override
	public void close() {
		data = null;
	}
	
	void packRatings(@WillNotClose Cursor<Rating> ratings) {
		int size = ratings.getRowCount();
		// default to something nice and large
		if (size < 0) size = 10000;
		
		// Track the indices where everything appears.  ArrayList user indices
		// to map of item indices to global indices.
		IndexManager imgr = new IndexManager(2000);
		
		// initialize arrays. we only track timestamps when we find them.
		IntArrayList users = new IntArrayList(size);
		IntArrayList items = new IntArrayList(size);
		DoubleArrayList values = new DoubleArrayList(size);
		LongArrayList timestamps = null;
		Indexer itemIndex = new Indexer();
		Indexer userIndex = new Indexer();
		int nratings = 0;
		
		for (Rating r: ratings) {
			final int uidx = userIndex.internId(r.getUserId());
			final int iidx = itemIndex.internId(r.getItemId());
			final double v = r.getRating();
			final long ts = r.getTimestamp();
			int idx = imgr.getIndex(uidx, iidx);
			if (idx < 0) {
				// new rating
				nratings++;
				users.add(uidx);
				items.add(iidx);
				values.add(v);
				assert users.size() == nratings;
				assert items.size() == nratings;
				assert values.size() == nratings;
				if (ts >= 0) {
					if (timestamps == null) {
						long[] tss = new long[values.elements().length];
						Arrays.fill(tss, 0, nratings - 1, -1);
						timestamps = LongArrayList.wrap(tss, nratings - 1);
					}
					timestamps.add(ts);
					assert timestamps.size() == nratings;
				}
			} else {
				// be careful with timestamps
				if (timestamps == null) {
					if (ts < 0) {
						values.set(idx, v);
					} else {
						timestamps = new LongArrayList(values.elements().length);
						Arrays.fill(timestamps.elements(), 0, nratings, -1);
						timestamps.set(idx, ts);
						values.set(idx, v);
					}
				} else if (ts >= timestamps.get(idx)) {
					values.set(idx, v);
					timestamps.set(idx, ts);
				} // fall through - not null && not newer
			}
		}
		
		users.trim();
		items.trim();
		values.trim();
		if (timestamps != null)
			timestamps.trim();
		
		data = new PackedRatingData(users.elements(), items.elements(), values.elements(),
				timestamps == null ? null : timestamps.elements(), itemIndex, userIndex);
		assert data.users.length == nratings;
		assert data.items.length == nratings;
		assert data.values.length == nratings;
		assert timestamps == null || data.timestamps.length == nratings;
		userIndices = imgr.getUserIndexMatrix();
	}
}
