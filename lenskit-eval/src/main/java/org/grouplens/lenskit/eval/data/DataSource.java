/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.data;

import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.script.BuiltBy;

import javax.annotation.Nullable;
import javax.inject.Provider;

/**
 * Data source for a single data set.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@BuiltBy(CSVDataSourceBuilder.class)
public interface DataSource {
    /**
     * Get the data source name.
     *
     * @return The data sources's name.
     */
    String getName();

    /**
     * Get the preference domain of this data source.
     *
     * @return The data source preference domain.
     */
    @Nullable
    PreferenceDomain getPreferenceDomain();

    /**
     * Get an event DAO for this data source.
     *
     * @return A DAO factory backed by this data source.
     */
    EventDAO getEventDAO();

    /**
     * Get a user-event DAO for this data source.  This implementation will probably not be used
     * for model training at present.
     *
     * @return A user-event DAO.
     */
    UserEventDAO getUserEventDAO();

    /**
     * Get a item-event DAO for this data source.  This implementation will probably not be used
     * for model training at present.
     *
     * @return A item-event DAO.
     */
    ItemEventDAO getItemEventDAO();

    /**
     * Get an item DAO for this data source.  This implementation will probably not be used
     * for model training at present.
     *
     * @return An item DAO.
     */
    ItemDAO getItemDAO();

    /**
     * Get an user DAO for this data source.  This implementation will probably not be used
     * for model training at present.
     *
     * @return An user DAO.
     */
    UserDAO getUserDAO();

    /**
     * Configure LensKit to use this data set.
     * @param config A LensKit configuration.  Bindings for this data source's data will be added
     *               to this configuration.
     */
    void configure(LenskitConfiguration config);

    long lastModified();
}
