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
package org.grouplens.lenskit.eval.data.traintest;

import java.util.Collections;
import java.util.Map;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.PreparationException;

/**
 * A train-test data set backed by a pair of factories.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public class GenericTTDataSet implements TTDataSet {
    private final String name;
    private final DAOFactory trainFactory;
    private final DAOFactory testFactory;
    private long lastUpdated = 0L;
    
    /**
     * Create a new generic data set.
     * @param name The data set name.
     * @param train The training DAO factory.
     * @param test The test DAO factory.
     */
    public GenericTTDataSet(String name, DAOFactory train, DAOFactory test) {
        this.name = name;
        trainFactory = train;
        testFactory = test;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return Collections.<String,Object>singletonMap("DataSet", getName());
    }
    
    /**
     * Set the last update time of the data set.
     * @param upd The last update time.  Defaults to 0L.
     */
    public void setLastUpdated(long upd) {
        lastUpdated = upd;
    }
    
    @Override
    public long lastUpdated(PreparationContext context) {
        return lastUpdated ;
    }

    @Override
    public void prepare(PreparationContext context) throws PreparationException {
        /* do nothing */
    }

    @Override
    public void release() {
        /* Do nothing */
    }

    @Override
    public DAOFactory getTrainFactory() {
        return trainFactory;
    }

    @Override
    public DAOFactory getTestFactory() {
        return testFactory;
    }
    
    @Override
    public String toString() {
        return String.format("{TTDataSet %s}", name);
    }
}
