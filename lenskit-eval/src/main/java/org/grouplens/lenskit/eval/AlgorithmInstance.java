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
package org.grouplens.lenskit.eval;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.eval.config.BuilderCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

import java.util.Collections;
import java.util.Map;

/**
 * An instance of a recommender algorithm to be benchmarked.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@BuilderCommand(AlgorithmInstanceCommand.class)
public class AlgorithmInstance {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmInstance.class);
    @Nullable
    private final String algoName;
    @Nonnull
    private final LenskitRecommenderEngineFactory factory;
    @Nonnull
    private final Map<String, Object> attributes;
    private final boolean preload;

    public AlgorithmInstance(String name, LenskitRecommenderEngineFactory factory) {
        this(name, factory, Collections.<String, Object>emptyMap(), false);
    }

    public AlgorithmInstance(String name, LenskitRecommenderEngineFactory factory, Map<String, Object> attributes, boolean preload) {
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

    @Nonnull
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    public LenskitRecommenderEngineFactory getFactory() {
        return factory;
    }

    public Recommender buildRecommender(DataAccessObject dao,
                                        @Nullable final Supplier<? extends PreferenceSnapshot> sharedSnapshot,
                                        PreferenceDomain dom) {
        // Copy the factory & set up a shared rating snapshot
        LenskitRecommenderEngineFactory fac2 = factory.clone();

        if (dom != null) {
            fac2.bind(PreferenceDomain.class).to(dom);
        }

        if (sharedSnapshot != null) {
            // FIXME Bind this to a provider
            Provider<PreferenceSnapshot> prv = new Provider<PreferenceSnapshot>() {
                @Override
                public PreferenceSnapshot get() {
                    return sharedSnapshot.get();
                }
            };
            fac2.bind(PreferenceSnapshot.class).toProvider(prv);
        }

        LenskitRecommenderEngine engine = fac2.create(dao);

        return engine.open(dao, false);
    }
}
