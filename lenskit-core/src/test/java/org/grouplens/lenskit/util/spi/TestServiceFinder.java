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
package org.grouplens.lenskit.util.spi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link ServiceFinder} class.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestServiceFinder {
    ServiceFinder<DummyInterface> dummyFinder;
    
    /**
     * Clear the service finder instance cache, so we always have a fresh
     * service finder.
     */
    @Before
    public void clearCache() {
        ServiceFinder.instanceMap.clear();
        dummyFinder = ServiceFinder.get(DummyInterface.class);
        dummyFinder.getLoader().reload();
    }
    
    @Test
    public void testGet() {
        assertThat(dummyFinder, notNullValue());
        assertThat(dummyFinder.serviceInterface, equalTo(DummyInterface.class));
        assertThat(dummyFinder.loader, notNullValue());
    }
    
    @Test
    public void testHasProviders() {
        assertThat(dummyFinder.getProviderCount(), greaterThanOrEqualTo(1));
    }

    @Test
    public void testFindByConfigName() {
        assertThat(dummyFinder.findProvider("dummy"), 
                   instanceOf(DummyImpl.class));
    }
    
    @Test
    public void testFindByAbbrvName() {
        assertThat(dummyFinder.findProvider("DummyImpl"), 
                   instanceOf(DummyImpl.class));
    }
    
    @Test
    public void testFindByFullName() {
        assertThat(dummyFinder.findProvider("org.grouplens.lenskit.util.spi.DummyImpl"),
                   instanceOf(DummyImpl.class));
    }
    
    @Test
    public void testUnregistered() {
        assertThat(dummyFinder.findProvider("UnregImpl"), nullValue());
        assertThat(dummyFinder.findProvider(UnregImpl.class.getName()),
                   instanceOf(UnregImpl.class));
    }
}
