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

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.dao.DataAccessObjectManager;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.pico.JustInTimePicoContainer;
import org.grouplens.lenskit.pico.ParameterAnnotationInjector;
import org.picocontainer.ComponentFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;

/**
 * LensKit implementation of a recommender engine.  It uses containers set up by
 * the {@link LenskitRecommenderEngineFactory} to set up recommender sessions.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @see LenskitRecommenderEngineFactory
 * @see LenskitRecommender
 */
public class LenskitRecommenderEngine implements RecommenderEngine {
    private final PicoContainer recommenderContainer;
    private final Map<Object, Object> sessionBindings;
    private final DataAccessObjectManager<? extends RatingDataAccessObject> manager;
    
    public LenskitRecommenderEngine(DataAccessObjectManager<? extends RatingDataAccessObject> manager,
                                 PicoContainer recommenderContainer, Map<Object, Object> sessionBindings) {
        this.manager = manager;
        this.recommenderContainer = recommenderContainer;
        this.sessionBindings = sessionBindings;
    }

    @Override
    public LenskitRecommender open() {
        if (manager == null)
            throw new IllegalStateException("No DAO manager supplied");
        // FIXME Unsafe if open() throws without closing the DAO
        return open(manager.open(), true);
    }

    @Override
    public LenskitRecommender open(@Nonnull RatingDataAccessObject dao, boolean shouldClose) {
        if (dao == null)
            throw new NullPointerException("Dao cannot be null when this method is used");
        return new LenskitRecommender(createSessionContainer(dao), dao, shouldClose);
    }
    
    private PicoContainer createSessionContainer(RatingDataAccessObject dao) {
        ComponentFactory factory = new Caching().wrap(new ParameterAnnotationInjector.Factory());
        MutablePicoContainer sessionContainer = new JustInTimePicoContainer(factory, 
                                                                            recommenderContainer);
        // Configure session container
        for (Entry<Object, Object> binding: sessionBindings.entrySet()) {
            sessionContainer.addComponent(binding.getKey(), binding.getValue());
        }
        
        // Add in the dao
        sessionContainer.addComponent(dao);
        return sessionContainer;
    }
}