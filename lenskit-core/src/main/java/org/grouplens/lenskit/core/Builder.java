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

import org.grouplens.lenskit.params.meta.Built;

/**
 * Builder provides a flexible mechanism for RecommenderEngineFactories to
 * create instances of components that can be built once for the
 * RecommenderEngine and shared by all the Recommender instances.
 * <p>
 * In the LenskitRecommenderEngineFactory, Builders can be configured directly,
 * or types can be annotated with the {@link Built} annotation.
 *
 * @author Michael Ludwig
 * @param <M>
 */
public interface Builder<M> {
    /**
     * Build the model object of type M using the Builder's current
     * configuration.
     *
     * @return An instance of type M built with the builder's configuration
     */
    public abstract M build();
}
