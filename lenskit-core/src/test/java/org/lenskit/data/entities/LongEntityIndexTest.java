/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.data.entities;

import org.junit.Test;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.entities.EntityIndex;
import org.lenskit.data.entities.LongEntityIndexBuilder;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class LongEntityIndexTest {
    private EntityFactory factory = new EntityFactory();

    @Test
    public void testEmpty() {
        LongEntityIndexBuilder bld = new LongEntityIndexBuilder("user");
        EntityIndex index = bld.build();
        assertThat(index, notNullValue());
        assertThat(index.getEntities(20L), hasSize(0));
    }

    @Test
    public void testAddEntity() {
        LongEntityIndexBuilder bld = new LongEntityIndexBuilder("user");
        Entity rating = factory.rating(10, 100, 3.5);
        bld.add(rating);
        EntityIndex index = bld.build();
        assertThat(index, notNullValue());
        assertThat(index.getEntities(10L), contains(rating));
        assertThat(index.getEntities(11L), hasSize(0));
    }

    @Test
    public void testNonLongValueEntity() {
        LongEntityIndexBuilder bld = new LongEntityIndexBuilder("user");
        Entity rating = factory.rating(10, 100, 3.5);
        bld.add(rating);
        EntityIndex index = bld.build();
        assertThat(index, notNullValue());
        assertThat(index.getEntities("10"), hasSize(0));
    }

    @Test
    public void testAddEntities() {
        LongEntityIndexBuilder bld = new LongEntityIndexBuilder("user");
        Entity r1 = factory.rating(10, 100, 3.5);
        Entity r2 = factory.rating(10, 50, 4.5);
        Entity r3 = factory.rating(15, 100, 2.5);
        bld.add(r1);
        bld.add(r3);
        bld.add(r2);
        EntityIndex index = bld.build();
        assertThat(index, notNullValue());
        assertThat(index.getEntities(10L), containsInAnyOrder(r1, r2));
        assertThat(index.getEntities(11L), hasSize(0));
        assertThat(index.getEntities(15L), contains(r3));
    }
}
