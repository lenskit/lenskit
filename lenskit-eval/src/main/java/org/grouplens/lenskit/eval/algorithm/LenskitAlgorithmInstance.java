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
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.eval.ExecutionInfo;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.script.BuiltBy;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private final LenskitConfiguration config;
    @Nonnull
    private final Map<String, Object> attributes;
    private final boolean preload;
    private Random random;

    public LenskitAlgorithmInstance(String name, LenskitConfiguration config) {
        this(name, config, Collections.<String, Object>emptyMap(), false);
    }

    public LenskitAlgorithmInstance(String name, LenskitConfiguration cfg, Map<String, Object> attrs, boolean preload) {
        algoName = name;
        config = cfg;
        attributes = attrs;
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
    public LenskitConfiguration getConfig() {
        return config;
    }

    /**
     * Let LenskitAlgorithmInstanceBuilder to pass random number generator to algorithm instance
     * 
     * @param builder The LenskitAlgorithmInstanceBuilder
     * @return The new algorithm instance
     */
    public LenskitAlgorithmInstance setRandom(Random rd) {
        random = rd;
        return this;
    }
    
    public LenskitRecommender buildRecommender(DataSource data,
                                               @Nullable final Provider<? extends PreferenceSnapshot> sharedSnapshot,
                                               @Nullable ExecutionInfo info) throws RecommenderBuildException {
        // Copy the config & set up a shared rating snapshot
        LenskitConfiguration cfg = new LenskitConfiguration(config);

        PreferenceDomain dom = data.getPreferenceDomain();
        if (dom != null) {
            cfg.bind(PreferenceDomain.class).to(dom);
        }

        if (sharedSnapshot != null) {
            cfg.bind(PreferenceSnapshot.class).toProvider(sharedSnapshot);
        }

        if (info != null) {
            cfg.bind(ExecutionInfo.class).to(info);
        }

        if (random != null) {
            cfg.bind(Random.class).to(random);
        }
        
        cfg.bind(EventDAO.class).toProvider(data.getEventDAOProvider());

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(cfg);

        return engine.createRecommender();
    }

    @Override
    public RecommenderInstance makeTestableRecommender(TTDataSet data,
                                                       Provider<? extends PreferenceSnapshot> snapshot,
                                                       ExecutionInfo info) throws RecommenderBuildException {
        return new RecInstance(buildRecommender(data.getTrainingData(),
                                                snapshot, info));
    }

    private static class RecInstance implements RecommenderInstance {
        private final LenskitRecommender recommender;

        public RecInstance(LenskitRecommender rec) {
            recommender = rec;
        }

        @Override
        public UserEventDAO getUserEventDAO() {
            return recommender.get(UserEventDAO.class);
        }

        @Override
        public SparseVector getPredictions(long uid, LongSet testItems) {
            RatingPredictor rp = recommender.getRatingPredictor();
            if (rp == null) return null;

            return rp.predict(uid, testItems);
        }

        @Override
        public List<ScoredId> getRecommendations(long uid, LongSet testItems, int n) {
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
