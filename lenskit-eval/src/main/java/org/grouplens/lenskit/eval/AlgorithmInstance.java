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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.common.base.Supplier;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.RecommenderEngineFactory;
import org.grouplens.lenskit.core.Builder;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.eval.config.DefaultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of a recommender algorithm to be benchmarked.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@DefaultBuilder(AlgorithmBuilder.class)
public class AlgorithmInstance {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmInstance.class);
    private final @Nullable String algoName;
    private final @Nonnull LenskitRecommenderEngineFactory factory;
    private final @Nonnull Map<String,Object> attributes;
    private final boolean preload;

    public AlgorithmInstance(String name, LenskitRecommenderEngineFactory factory) {
        this(name, factory, Collections.<String,Object>emptyMap(), false);
    }

    public AlgorithmInstance(String name, LenskitRecommenderEngineFactory factory, Map<String,Object> attributes, boolean preload) {
        algoName = name;
        this.factory = factory;
        this.attributes = attributes;
        this.preload = preload;
    }

    /**
     * Get the name of this algorithm.  This returns a short name which is
     * used to identify the algorithm or instance.
     * @return The algorithm's name
     */
    public String getName() {
        return algoName;
    }

    /**
     * Query whether this algorithm is to operate on in-memory data.
     * @return <tt>true</tt> if the ratings database should be loaded in-memory
     * prior to running.
     */
    public boolean getPreload() {
        return preload;
    }

    @Nonnull
    public Map<String,Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    public RecommenderEngineFactory getFactory() {
        return factory;
    }

    public Recommender buildRecommender(DataAccessObject dao,
                                        final @Nullable Supplier<? extends RatingSnapshot> sharedSnapshot) {
        // Copy the factory & set up a shared rating snapshot
        LenskitRecommenderEngineFactory fac2 = factory;

        if (sharedSnapshot != null) {
            fac2 = factory.clone();
            Builder<RatingSnapshot> bld = new Builder<RatingSnapshot>() {
                @Override
                public RatingSnapshot build() {
                    return sharedSnapshot.get();
                }
            };
            fac2.setBuilder(RatingSnapshot.class, bld);
        }

        LenskitRecommenderEngine engine = fac2.create(dao);

        return engine.open(dao, false);
    }

    public Recommender buildRecommender(DataAccessObject dao, @Nullable RatingSnapshot sharedSnapshot) {
        // Copy the factory & set up a shared rating snapshot
        LenskitRecommenderEngineFactory fac2 = factory;

        if (sharedSnapshot != null) {
            fac2 = factory.clone();
            fac2.setComponent(RatingSnapshot.class, sharedSnapshot);
        }

        LenskitRecommenderEngine engine = fac2.create(dao);

        return engine.open(dao, false);
    }
}
