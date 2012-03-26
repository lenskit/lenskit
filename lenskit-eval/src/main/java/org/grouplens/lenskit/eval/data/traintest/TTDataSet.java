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
package org.grouplens.lenskit.eval.data.traintest;

import java.util.Map;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.JobGroup;
import org.grouplens.lenskit.eval.config.DefaultBuilder;

import javax.annotation.Nullable;

/**
 * Interface for train-test data sets.  This is a single train-test pair.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@DefaultBuilder(GenericTTDataBuilder.class)
public interface TTDataSet {
    /**
     * Get the data set name.
     * @return A name for the data set.  Used as the job group name.
     * @see JobGroup#getName()
     */
    String getName();

    /**
     * Get the preference domain for this data set.
     * @return The data set preference domain.
     */
    @Nullable
    PreferenceDomain getPreferenceDomain();
    
    /**
     * Get the data set attributes (used for identification in output).
     * 
     * @return A key -> value map of the attributes used to identify this data
     *         set. For example, a crossfold data set may include the source
     *         name and fold number.
     */
    Map<String,Object> getAttributes();

    /**
     * Release the data set. Called when the train-test job group using this
     * data set is finished.
     */
    void release();
    
    /**
     * Get the training data.
     * @return A DAO factory returning the training data.
     */
    DAOFactory getTrainFactory();
    
    /**
     * Get the test data.
     * @return A DAO factory returning the test data.
     */
    DAOFactory getTestFactory();

    long lastUpdated();
}
