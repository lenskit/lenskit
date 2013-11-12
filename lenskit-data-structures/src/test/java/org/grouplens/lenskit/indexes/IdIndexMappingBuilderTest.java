/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.indexes;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class IdIndexMappingBuilderTest {
    IdIndexMappingBuilder builder;

    @Before
    public void createBuilder() {
        builder = new IdIndexMappingBuilder();
    }

    @Test
    public void testEmpty() {
        IdIndexMapping idx = builder.build();
        assertThat(idx.size(),
                   equalTo(0));
        assertThat(idx.getIdList(), hasSize(0));
        assertThat(idx.tryGetIndex(42), lessThan(0));
        try {
            idx.getIndex(42);
            fail("getIndex should fail with bad key");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }

    @Test
    public void testAddOne() {
        builder.add(42);
        IdIndexMapping idx = builder.build();
        assertThat(idx.size(), equalTo(1));
        assertThat(idx.getIdList(), contains(42L));
        assertThat(idx.getIndex(42), equalTo(0));
        assertThat(idx.getId(0), equalTo(42L));
        assertThat(idx.tryGetIndex(39), lessThan(0));
    }

    @Test
    public void testAddSeveral() {
        List<Long> keys = Arrays.asList(42L, 39L, 16L, 68L, 16L);
        builder.addAll(keys);
        IdIndexMapping idx = builder.build();
        assertThat(idx.size(), equalTo(4));
        assertThat(idx.getIndex(42), greaterThanOrEqualTo(0));
        Set<Long> keySet = Sets.newHashSet(keys);
        for (int i = 0; i < idx.size(); i++) {
            long key = idx.getId(i);
            assertThat(keySet, hasItem(key));
            assertThat(idx.getIndex(key), equalTo(i));
        }
        assertThat(idx.getIdList(), containsInAnyOrder(keySet.toArray(new Long[keySet.size()])));
    }
}
