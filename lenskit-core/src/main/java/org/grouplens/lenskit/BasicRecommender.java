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
package org.grouplens.lenskit;

import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.picocontainer.PicoContainer;

public class BasicRecommender implements Recommender {
    private final PicoContainer container;
    private final RatingDataAccessObject dao;
    private final boolean shouldCloseDao;
    
    // An alternate to this BasicRecommender where it asks for the components as needed
    // is to see if there is an actual Recommender that can be built from the container
    // and then delegate to that.  The wrapper recommender would still handle the closing
    // logic, this would give us a single configuration point if people chose to use it.
    public BasicRecommender(PicoContainer container, RatingDataAccessObject dao, boolean shouldCloseDao) {
        this.container = container;
        this.dao = dao;
        this.shouldCloseDao = shouldCloseDao;
    }
    
    @Override
    public <T> T getComponent(Class<T> cls) {
        return container.getComponent(cls);
    }

    @Override
    public RatingPredictor getRatingPredictor() {
        return container.getComponent(RatingPredictor.class);
    }

    @Override
    public DynamicRatingPredictor getDynamicRatingPredictor() {
        return container.getComponent(DynamicRatingPredictor.class);
    }

    @Override
    public DynamicItemRecommender getDynamicItemRecommender() {
        return container.getComponent(DynamicItemRecommender.class);
    }

    @Override
    public BasketRecommender getBasketRecommender() {
        return container.getComponent(BasketRecommender.class);
    }

    @Override
    public void close() {
        if (shouldCloseDao)
            dao.close();
    }

    @Override
    public RatingDataAccessObject getRatingDataAccessObject() {
        return dao;
    }

	@Override
	public ItemRecommender getItemRecommender() {
		return container.getComponent(ItemRecommender.class);
	}
}