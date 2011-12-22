/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.lenskit.core;

import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.picocontainer.PicoContainer;

/**
 * Recommender implementation built on LensKit containers.  Recommenders built
 * with {@link LenskitRecommenderEngineFactory} will produce this type of
 * recommender.
 *
 * <p>The {@link Recommender} interface will meet most needs, so most users can
 * ignore this class.  However, if you need to inspect internal components of a
 * recommender (e.g. extract the item-item similarity matrix), this class and its
 * {@link #getComponent(Class)} method can be useful.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class LenskitRecommender implements Recommender {
    private final PicoContainer container;
    private final DataAccessObject dao;
    private final boolean shouldCloseDao;

    // An alternate to this LenskitRecommender where it asks for the components as needed
    // is to see if there is an actual Recommender that can be built from the container
    // and then delegate to that.  The wrapper recommender would still handle the closing
    // logic, this would give us a single configuration point if people chose to use it.
    public LenskitRecommender(PicoContainer container, DataAccessObject dao, boolean shouldCloseDao) {
        this.container = container;
        this.dao = dao;
        this.shouldCloseDao = shouldCloseDao;
    }

    /**
     * Get a particular component from the recommender session.  Generally
     * you want to use one of the type-specific getters; this method only exists
     * for specialized applications which need deep access to the recommender
     * components.
     * @param <T>
     * @param cls The component class to get.
     * @return The instance of the specified component.
     */
    public <T> T getComponent(Class<T> cls) {
        return container.getComponent(cls);
    }

    @Override
    public ItemScorer getItemScorer() {
        return container.getComponent(ItemScorer.class);
    }

    @Override
    public RatingPredictor getRatingPredictor() {
        return container.getComponent(RatingPredictor.class);
    }

    @Override @Deprecated
    public RatingPredictor getDynamicRatingPredictor() {
        return getRatingPredictor();
    }

    @Override @Deprecated
    public ItemRecommender getDynamicItemRecommender() {
        return getItemRecommender();
    }

    @Override
    public void close() {
        if (shouldCloseDao)
            dao.close();
    }

    /**
     * Get the rating DAO for this recommender session.
     * @return The DAO, or <var>null</var> if this recommender is not connected
     * to a DAO.  All LensKit recommenders are connected to DAOs; recommenders
     * from other frameworks that are adapted to the LensKit API may not be.
     */
    public DataAccessObject getRatingDataAccessObject() {
        return dao;
    }

    @Override
    public ItemRecommender getItemRecommender() {
        return container.getComponent(ItemRecommender.class);
    }
}