/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.util.Collection;

import org.grouplens.lenskit.dtree.DataNode;
import org.grouplens.lenskit.eval.EvaluatorConfigurationException;

/**
 * Interface for configuring data sources for evaluation.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface DataSourceProvider {
    /**
     * Configure a set of data sources.
     * 
     * @param config The configuration
     * @return The data sources specified by this configuration
     * @throws EvaluatorConfigurationException if there is a configuration
     *         error.
     */
    Collection<DataSource> configure(DataNode config) throws EvaluatorConfigurationException;
}
