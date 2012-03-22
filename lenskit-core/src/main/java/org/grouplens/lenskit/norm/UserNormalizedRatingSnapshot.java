/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.norm;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.MutableIndexedPreference;
import org.grouplens.lenskit.data.snapshot.AbstractRatingSnapshot;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.params.NormalizedSnapshot;
import org.grouplens.lenskit.params.UserVectorNormalizer;
import org.grouplens.lenskit.util.Index;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rating snapshot that provides normalized ratings. They are built
 * with a {@link UserNormalizedRatingSnapshot.Builder}.
 *
 * <p>
 * This class also computes the normed data lazily, so the computation cost
 * isn't incurred unless necessary.
 *
 * <p>
 * <strong>Warning:</strong> Do not configure this component in the
 * {@link LenskitRecommenderEngineFactory} as a plain RatingSnapshot. If this is
 * done, reference cycles will exist as UserNormalizedRatingSnapshot depends on
 * another RatingSnapshot for its data.It can be configured if combined with an
 * annotation, such as {@link NormalizedSnapshot}.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class UserNormalizedRatingSnapshot extends AbstractRatingSnapshot {
    private static final Logger logger = LoggerFactory.getLogger(UserNormalizedRatingSnapshot.class);
    private final RatingSnapshot snapshot;
    private final VectorNormalizer<? super UserVector> normalizer;
    private SparseVector[] normedData;

    public UserNormalizedRatingSnapshot(RatingSnapshot snapshot,
                                        @UserVectorNormalizer VectorNormalizer<? super UserVector> norm) {
        super();
        this.snapshot = snapshot;
        normalizer = norm;
    }

    private synchronized void requireNormedData() {
        if (normedData == null) {
            logger.debug("Computing normalized build context");
            logger.debug("Using normalizer {}", normalizer);
            LongCollection users = snapshot.getUserIds();
            normedData = new SparseVector[users.size()];
            Index uidx = snapshot.userIndex();
            LongIterator uit = users.iterator();
            int ndone = 0; // for debugging
            while (uit.hasNext()) {
                final long uid = uit.nextLong();
                final int i = uidx.getIndex(uid);
                assert normedData[i] == null;
                UserVector rv = UserVector.fromPreferences(uid, snapshot.getUserRatings(uid));
                normedData[i] = normalizer.normalize(rv, null);
                ndone++;
            }
            assert ndone == normedData.length;
        }
    }

    public VectorNormalizer<? super UserVector> getNormalizer() {
        return normalizer;
    }

    @Override
    public LongCollection getUserIds() {
        return snapshot.getUserIds();
    }

    @Override
    public LongCollection getItemIds() {
        return snapshot.getItemIds();
    }

    @Override
    public Index userIndex() {
        return snapshot.userIndex();
    }

    @Override
    public Index itemIndex() {
        return snapshot.itemIndex();
    }

    @Override
    public FastCollection<IndexedPreference> getRatings() {
        requireNormedData();
        return new NormalizingCollection(normedData, snapshot.getRatings());
    }

    @Override
    public FastCollection<IndexedPreference> getUserRatings(long userId) {
        requireNormedData();
        return new NormalizingCollection(normedData, snapshot.getUserRatings(userId));
    }

    /**
     * "Close" the build context by clearing its internal data. Does not close
     * the root build context.
     */
    @Override
    public synchronized void close() {
        super.close();
        normedData = null;
    }

    private static class NormalizingCollection extends AbstractCollection<IndexedPreference>
            implements FastCollection<IndexedPreference> {
        private FastCollection<IndexedPreference> base;
        private SparseVector[] normedData;

        public NormalizingCollection(SparseVector[] nd, FastCollection<IndexedPreference> base) {
            this.base = base;
            normedData = nd;
        }

        @Override
        public Iterator<IndexedPreference> fastIterator() {
            return new Iterator<IndexedPreference>() {
                private Iterator<IndexedPreference> biter = base.fastIterator();
                IndirectPreference preference = new IndirectPreference();
                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
                @Override public boolean hasNext() {
                    return biter.hasNext();
                }
                @Override public IndexedPreference next() {
                    preference.base = biter.next();
                    return preference;
                }
            };
        }

        final class IndirectPreference implements IndexedPreference {
            IndexedPreference base;

            @Override
            public long getUserId() {
                return base.getUserId();
            }

            @Override
            public long getItemId() {
                return base.getItemId();
            }

            @Override
            public double getValue() {
                return normedData[getUserIndex()].get(getItemId());
            }

            @Override
            public int getIndex() {
                return base.getIndex();
            }

            @Override
            public int getUserIndex() {
                return base.getUserIndex();
            }

            @Override
            public int getItemIndex() {
                return base.getItemIndex();
            }

            @Override
            public IndexedPreference clone() {
                return new MutableIndexedPreference(getUserId(), getItemId(),
                                                    getValue(), getIndex(),
                                                    getUserIndex(), getItemIndex());
            }
        }

        @Override @Deprecated
        public Iterable<IndexedPreference> fast() {
            return new Iterable<IndexedPreference>() {
                @Override public Iterator<IndexedPreference> iterator() {
                    return fastIterator();
                }
            };
        }

        @Override
        public Iterator<IndexedPreference> iterator() {
            return new Iterator<IndexedPreference>() {
                Iterator<IndexedPreference> biter = base.fastIterator();
                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
                @Override public boolean hasNext() {
                    return biter.hasNext();
                }
                @Override public IndexedPreference next() {
                    IndexedPreference r = biter.next();
                    long iid = r.getItemId();
                    int uidx = r.getUserIndex();
                    return new MutableIndexedPreference(r.getUserId(), iid,
                            normedData[uidx].get(iid), r.getIndex(),
                            uidx, r.getItemIndex());
                }
            };
        }

        @Override
        public int size() {
            return base.size();
        }
    }
}
