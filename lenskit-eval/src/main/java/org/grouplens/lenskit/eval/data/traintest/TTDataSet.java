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
package org.grouplens.lenskit.eval.data.traintest;

import java.util.Map;

import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.eval.script.BuiltBy;

import javax.annotation.Nullable;

/**
 * Interface for train-test data sets.  This is a single train-test pair.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
@BuiltBy(GenericTTDataBuilder.class)
public interface TTDataSet {
    /**
     * Get the data set name.
     *
     * @return A name for the data set. Used in the output file.
     */
    String getName();

    /**
     * Get the data set attributes (used for identification in output).
     *
     * @return A key &rarr; value map of the attributes used to identify this data
     *         set. For example, a crossfold data set may include the source
     *         name and fold number.
     */
    Map<String, Object> getAttributes();

    /**
     * Release the data set. Called when the train-test job group using this
     * data set is finished.
     */
    void release();

    /**
     * Get the training data.
     *
     * @return A data source containing the training data.
     */
    DataSource getTrainingData();

    /**
     * Get the training data.
     *
     * @return An event DAO encompassing the training data.
     */
    EventDAO getTrainingDAO();

    /**
     * Get the query data.
     *
     * @return A data source containing the query data.
     */
    DataSource getQueryData();

    /**
     * Get the query data.
     *
     * @return An event DAO encompassing the query data.
     */
    EventDAO getQueryDAO();

    /**
     * Get the training data.
     *
     * @return A data source containing the test data.
     */
    DataSource getTestData();

    /**
     * Get the test data.
     *
     * @return A DAO factory returning the test data.
     */
    EventDAO getTestDAO();

    /**
     * Get the last modification time of this data set.
     * @return The last modification time, in milliseconds since the epoch.
     */
    long lastModified();

    /**
     * Configure LensKit to have the training data from this data source.
     *
     * @param config A configuration in which the training data for this data set should be
     *               configured.
     */
    void configure(LenskitConfiguration config);
}
