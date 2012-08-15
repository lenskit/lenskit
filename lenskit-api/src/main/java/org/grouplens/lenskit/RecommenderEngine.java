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
package org.grouplens.lenskit;


/**
 * Service providing access to a recommender.  In LensKit, you build a recommender
 * engine from a {@link RecommenderEngineFactory}, then use it to open recommenders.
 *
 * <p>Recommender engines can be shared across threads or sessions; they are
 * long-lived objects that should be built once.  Recommenders are opened "per-request"
 * or per thread and provide access to the actual recommendation.</p>
 *
 * <p>Recommender engines do not hold connections to data sources.  The data
 * source is accessed by the factory, and then re-connected to the recommender
 * machinery in {@link #open()}.</p>
 *
 * @see RecommenderEngineFactory
 * @see Recommender
 * @compat Public
 */
public interface RecommenderEngine {
    /**
     * Open a recommender.  The client code must close the recommender when it is
     * finished with it.  The recommender is connected to the DAO using whatever
     * provider was configured for its factory.
     *
     * @return A recommender ready for use.
     * @throws IllegalStateException if the recommender requires a DAO and no
     * DAO provider is available.
     */
    public Recommender open();
}
