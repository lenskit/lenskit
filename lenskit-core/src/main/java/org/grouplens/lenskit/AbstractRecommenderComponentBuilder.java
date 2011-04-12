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

import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.context.RatingBuildContext.Key;

/**
 * AbstractRecommenderComponentBuilder is a RecommenderComponentBuilder shell
 * that handles the memoization logic for the build() method. Subclasses should
 * not need to override build(), implementing buildNew() should be sufficient.
 * Memoization is handled by storing values within the cache of the
 * RatingBuildContext (i.e. using {@link RatingBuildContext#get(Key)} and
 * {@link RatingBuildContext#put(Key, Object)}).
 * 
 * @author Michael Ludwig
 * @param <M>
 */
public abstract class AbstractRecommenderComponentBuilder<M> implements RecommenderComponentBuilder<M> {
    protected final Key<M> key = new Key<M>();
    
    @Override
    public M build(RatingBuildContext context) {
        M built = context.get(key);
        if (built == null) {
            built = buildNew(context);
            context.put(key, built);
        }
        
        return built;
    }
    
    protected abstract M buildNew(RatingBuildContext context);
}
