package org.grouplens.lenskit.core;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.spi.Satisfaction;
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
