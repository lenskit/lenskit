/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.eval.algorithm;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.eval.ExecutionInfo;
import org.grouplens.lenskit.eval.SharedPreferenceSnapshot;
import org.grouplens.lenskit.eval.script.BuiltBy;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.Collections;
import java.util.Map;

/**
 * An instance of a recommender algorithm to be benchmarked.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@BuiltBy(LenskitAlgorithmInstanceBuilder.class)
public class LenskitAlgorithmInstance implements AlgorithmInstance {
    private static final Logger logger = LoggerFactory.getLogger(LenskitAlgorithmInstance.class);
    @Nullable
    private final String algoName;
    @Nonnull
    private final LenskitRecommenderEngineFactory factory;
    @Nonnull
    private final Map<String, Object> attributes;
    private final boolean preload;

    public LenskitAlgorithmInstance(String name, LenskitRecommenderEngineFactory factory) {
        this(name, factory, Collections.<String, Object>emptyMap(), false);
    }

    public LenskitAlgorithmInstance(String name, LenskitRecommenderEngineFactory factory, Map<String, Object> attributes, boolean preload) {
        algoName = name;
        this.factory = factory;
        this.attributes = attributes;
        this.preload = preload;
    }

    /**
     * Get the name of this algorithm.  This returns a short name which is
     * used to identify the algorithm or instance.
     *
     * @return The algorithm's name
     */
    @Override
    public String getName() {
        return algoName;
    }

    /**
     * Query whether this algorithm is to operate on in-memory data.
     *
     * @return {@code true} if the ratings database should be loaded in-memory
     *         prior to running.
     */
    public boolean getPreload() {
        return preload;
    }

    @Override
    @Nonnull
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    public LenskitRecommenderEngineFactory getFactory() {
        return factory;
    }

    public LenskitConfiguration getConfig() {
        return factory.getConfig();
    }

    public LenskitRecommender buildRecommender(DataAccessObject dao,
                                               @Nullable final Supplier<? extends PreferenceSnapshot> sharedSnapshot,
                                               PreferenceDomain dom, ExecutionInfo info,
                                               boolean shouldClose) throws RecommenderBuildException {
        try {
            // Copy the factory & set up a shared rating snapshot
            LenskitRecommenderEngineFactory fac2 = factory.clone();

            if (dom != null) {
                fac2.bind(PreferenceDomain.class).to(dom);
            }

            if (sharedSnapshot != null) {
                Provider<PreferenceSnapshot> prv = new Provider<PreferenceSnapshot>() {
                    @Override
                    public PreferenceSnapshot get() {
                        return sharedSnapshot.get();
                    }
                };
                fac2.bind(PreferenceSnapshot.class).toProvider(prv);
            }

            if (info != null) {
                fac2.bind(ExecutionInfo.class).to(info);
            }

            LenskitRecommenderEngine engine = fac2.create(dao);

            return engine.open(dao, shouldClose);
        } catch (RuntimeException e) {
            if (shouldClose) {
                dao.close();
            }
            throw e;
        } catch (RecommenderBuildException e) {
            if (shouldClose) {
                dao.close();
            }
            throw e;
        }
    }

    @Override
    public RecommenderInstance makeTestableRecommender(TTDataSet data,
                                                       Supplier<SharedPreferenceSnapshot> snapshot,
                                                       ExecutionInfo info) throws RecommenderBuildException {
        return new RecInstance(buildRecommender(data.getTrainFactory().create(),
                                                snapshot,
                                                data.getPreferenceDomain(),
                                                info,
                                                true));
    }

    private static class RecInstance implements RecommenderInstance {
        private final LenskitRecommender recommender;

        public RecInstance(LenskitRecommender rec) {
            recommender = rec;
        }

        @Override
        public DataAccessObject getDAO() {
            return recommender.getDataAccessObject();
        }

        @Override
        public SparseVector getPredictions(long uid, LongSet testItems) {
            RatingPredictor rp = recommender.getRatingPredictor();
            if (rp == null) return null;

            return rp.score(uid, testItems);
        }

        @Override
        public ScoredLongList getRecommendations(long uid, LongSet testItems, int n) {
            ItemRecommender irec = recommender.getItemRecommender();
            if (irec == null) {
                return null;
            }

            return irec.recommend(uid, n, testItems, null);
        }

        @Override
        public LenskitRecommender getRecommender() {
            return recommender;
        }

        @Override
        public void close() {
            recommender.close();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LenskitAlgorithm(")
          .append(getName())
          .append(")");
        if (!attributes.isEmpty()) {
            sb.append("[");
            Joiner.on(", ")
                  .withKeyValueSeparator("=")
                  .appendTo(sb, attributes);
            sb.append("]");
        }
        return sb.toString();
    }
}
