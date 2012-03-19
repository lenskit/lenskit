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
package org.grouplens.lenskit.eval.data;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.config.DefaultBuilder;

import javax.annotation.Nullable;

/**
 * Data source for a single data set.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@DefaultBuilder(CSVDataSourceBuilder.class)
public interface DataSource {
    /**
     * Get the data source name.
     * @return The data sources's name.
     */
    String getName();

    /**
     * Get the preference domain of this data source.
     * @return The data source preference domain.
     */
    @Nullable
    PreferenceDomain getPreferenceDomain();
    
    /**
     * Get a DAO factory for this data source. The data source must be prepared
     * before this method is called or the resulting DAO factory used.
     * 
     * @return A DAO factory backed by this data source.
     */
    DAOFactory getDAOFactory();

    long lastUpdated();
}
