/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.data.context;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;

import java.util.Arrays;
import java.util.List;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.Indexer;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.util.CollectionUtils;
import org.grouplens.lenskit.util.FastCollection;

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
public class PackedRatingBuildContext extends AbstractRatingBuildContext {
    private final RatingDataAccessObject dao;
    
	private PackedRatingData data;
	private List<IntList> userIndices;
	
	protected PackedRatingBuildContext(RatingDataAccessObject dao, 
	        PackedRatingData data, List<IntList> userIndices) {
	    this.dao = dao;
	    this.data = data;
	    this.userIndices = userIndices;
	}
	
	private void requireValid() {
		if (data == null)
			throw new IllegalStateException("build context closed");
	}
	
	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.context.RatingBuildContext#getDAO()
	 */
	@Override
	public RatingDataAccessObject getDAO() {
	    return dao;
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
	    super.close();
		data = null;
	}
	
	public static PackedRatingBuildContext make(RatingDataAccessObject dao) {
	    Cursor<Rating> ratings = dao.getRatings();
	    
	    IntArrayList users;
	    IntArrayList items;
	    DoubleArrayList values;
	    LongArrayList timestamps;
	    Indexer itemIndex = new Indexer();
	    Indexer userIndex = new Indexer();
        // Track the indices where everything appears.  ArrayList user indices
        // to map of item indices to global indices.
	    IndexManager imgr = new IndexManager(2000);
	    
	    int nratings = 0;
	    
	    try {
	        int size = ratings.getRowCount();
	        // default to something nice and large
	        if (size < 0) size = 10000;

	        // initialize arrays. we only track timestamps when we find them.
	        users = new IntArrayList(size);
	        items = new IntArrayList(size);
	        values = new DoubleArrayList(size);
	        timestamps = null;
	        itemIndex = new Indexer();
	        userIndex = new Indexer();

	        for (Rating r: ratings) {
	            final int iidx = itemIndex.internId(r.getItemId());
	            final int uidx = userIndex.internId(r.getUserId());
	            final double v = r.getRating();
	            final long ts = r.getTimestamp();
	            int idx = imgr.getIndex(uidx, iidx);
	            if (idx < 0) {
	                // new rating
	                imgr.putIndex(uidx, iidx, nratings);
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
	    } finally {
	        ratings.close();
	    }
		
		users.trim();
		items.trim();
		values.trim();
		if (timestamps != null)
			timestamps.trim();
		
		PackedRatingData data =
		    new PackedRatingData(users.elements(), items.elements(), values.elements(),
				timestamps == null ? null : timestamps.elements(), itemIndex, userIndex);
		assert data.users.length == nratings;
		assert data.items.length == nratings;
		assert data.values.length == nratings;
		assert timestamps == null || data.timestamps.length == nratings;
		List<IntList> userIndices = imgr.getUserIndexMatrix();
		return new PackedRatingBuildContext(dao, data, userIndices);
	}
}
