package org.grouplens.lenskit.norm;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.SimpleIndexedRating;
import org.grouplens.lenskit.data.context.PackedRatingBuildContext;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.FastCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Rating build context wrapper that provides normalized ratings.
 * 
 * <p>This class wraps the rating build context to provide pre-normalized ratings.
 * It should share the same scope as the rating build context, so if you re-scope
 * {@link PackedRatingBuildContext} (or some other rating build context implementation)
 * in your Guice configuration, you must re-scope this class as well.
 * 
 * <p>This class
 * also breaks the rule that rating build contexts shouldn't be retained, but
 * since its scope is intended to be identical to the rating build context itself,
 * that is OK.
 * 
 * <p>This class also computes the normed data lazily, so the computation cost
 * isn't incurred unless necessary even when injected as a singleton in the Guice
 * PRODUCTION scope.
 * 
 * <p><strong>Warning:</strong> Do not bind this class as the implementation of
 * {@link RatingBuildContext} in your Guice configuration, as that will implement
 * circular loops and general brokenness. Classes which want a normalized rating
 * build context should depend on it directly.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Singleton
public class NormalizedRatingBuildContext implements RatingBuildContext {
    private static final Logger logger = LoggerFactory.getLogger(NormalizedRatingBuildContext.class);
    private final RatingBuildContext buildContext;
    private final UserRatingVectorNormalizer normalizer;
    private SparseVector[] normedData;
    
    @Inject
    public NormalizedRatingBuildContext(RatingBuildContext context, UserRatingVectorNormalizer norm) {
        buildContext = context;
        normalizer = norm;
    }
    
    private synchronized void requireNormedData() {
        if (normedData == null) {
            logger.debug("Computing normalized build context");
            LongCollection users = buildContext.getUserIds();
            normedData = new SparseVector[users.size()];
            LongIterator uit = users.iterator();
            Index uidx = buildContext.userIndex();
            int ndone = 0; // for debugging
            while (uit.hasNext()) {
                final long uid = uit.nextLong();
                final int i = uidx.getIndex(uid);
                assert normedData[i] == null;
                MutableSparseVector rv = Ratings.userRatingVector(buildContext.getUserRatings(uid));
                normalizer.normalize(uid, rv);
                normedData[i] = rv;
                ndone++;
            }
            assert ndone == normedData.length;
        }
    }

    @Override
    public LongCollection getUserIds() {
        return buildContext.getUserIds();
    }

    @Override
    public LongCollection getItemIds() {
        return buildContext.getItemIds();
    }

    @Override
    public Index userIndex() {
        return buildContext.userIndex();
    }

    @Override
    public Index itemIndex() {
        return buildContext.itemIndex();
    }

    @Override
    public FastCollection<IndexedRating> getRatings() {
        requireNormedData();
        return new NormalizingCollection(normedData, buildContext.getRatings());
    }

    @Override
    public FastCollection<IndexedRating> getUserRatings(long userId) {
        requireNormedData();
        return new NormalizingCollection(normedData, buildContext.getUserRatings(userId));
    }

    /**
     * "Close" the build context by clearing its internal data. Does not close
     * the root build context.
     */
    @Override
    public synchronized void close() {
        normedData = null;
    }
    
    private static class NormalizingCollection extends AbstractCollection<IndexedRating> 
            implements FastCollection<IndexedRating> {
        private FastCollection<IndexedRating> base;
        private SparseVector[] normedData;
        public NormalizingCollection(SparseVector[] nd, FastCollection<IndexedRating> base) {
            this.base = base;
            normedData = nd;
        }
        @Override
        public Iterator<IndexedRating> fastIterator() {
            return new Iterator<IndexedRating>() {
                private Iterator<IndexedRating> biter = base.fastIterator();
                NormedRating rating = new NormedRating();
                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
                @Override public boolean hasNext() {
                    return biter.hasNext();
                }
                @Override public IndexedRating next() {
                    rating.brating = biter.next();
                    return rating;
                }
            };
        }
        @Override
        public Iterable<IndexedRating> fast() {
            return new Iterable<IndexedRating>() {
                @Override public Iterator<IndexedRating> iterator() {
                    return fastIterator();
                }
            };
        }
        @Override
        public Iterator<IndexedRating> iterator() {
            return new Iterator<IndexedRating>() {
                Iterator<IndexedRating> biter = base.fastIterator();
                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
                @Override public boolean hasNext() {
                    return biter.hasNext();
                }
                @Override public IndexedRating next() {
                    IndexedRating r = biter.next();
                    long iid = r.getItemId();
                    int uidx = r.getUserIndex();
                    return new SimpleIndexedRating(r.getUserId(), iid, 
                            normedData[uidx].get(iid), 
                            r.getTimestamp(), uidx, r.getItemIndex());
                }
            };
        }
        @Override
        public int size() {
            return base.size();
        }
        
        private class NormedRating implements IndexedRating {
            IndexedRating brating = null;

            public long getUserId() {
                return brating.getUserId();
            }

            public int getUserIndex() {
                return brating.getUserIndex();
            }

            public long getItemId() {
                return brating.getItemId();
            }

            public double getRating() {
                return normedData[getUserIndex()].get(getItemId());
            }

            public int getItemIndex() {
                return brating.getItemIndex();
            }

            public long getTimestamp() {
                return brating.getTimestamp();
            }
            
        }
    }

}
