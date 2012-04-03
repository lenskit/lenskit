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
package org.grouplens.lenskit.core;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Qualifier;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.lenskit.data.dao.DataAccessObject;

import com.google.common.base.Function;

/**
 * DAOSatisfaction is a place-holder satisfaction to mark the node that holds
 * the DataAccessObject for each recommender.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class DAOSatisfaction implements Satisfaction {
    @Override
    public List<? extends Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return DataAccessObject.class;
    }

    @Override
    public Class<?> getErasedType() {
        return DataAccessObject.class;
    }

    @Override
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comparator<ContextMatcher> contextComparator(Qualifier qualifier) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int hashCode() {
        return DAOSatisfaction.class.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof DAOSatisfaction;
    }
}
