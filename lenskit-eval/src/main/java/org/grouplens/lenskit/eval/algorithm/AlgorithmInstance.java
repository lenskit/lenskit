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

import com.google.common.base.Supplier;
import groovy.sql.DataSet;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.eval.SharedPreferenceSnapshot;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * An algorithm instance. On its own, this doesn't do much; it exists to share some
 * metadata between {@link LenskitAlgorithmInstance} and {@link ExternalAlgorithmInstance}.
 */
public interface AlgorithmInstance {
    /**
     * Get the name of this algorithm instance.
     * @return The instance's name.
     */
    String getName();

    /**
     * Get the attributes associated with this algorithm instance.
     * @return The algorithm instance's attributes.
     */
    @Nonnull
    Map<String, Object> getAttributes();

    /**
     * Create a testable recommender instance from this algorithm.
     * @param data The data set. The test data should only be used if the recommender needs to
     *                 capture the test data (e.g. an external program that will produce
     *                 predictions en mass).
     * @param snapshot The (cached) shared preference snapshot.
     * @return A recommender instance for testing this algorithm.
     */
    RecommenderInstance makeTestableRecommender(TTDataSet data,
                                                Supplier<SharedPreferenceSnapshot> snapshot) throws RecommenderBuildException;
}
