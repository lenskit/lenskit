/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.data.store;

import org.junit.Test;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GenericEntityIndexTest {
    private EntityFactory factory = new EntityFactory();

    @Test
    public void testEmpty() {
        GenericEntityIndexBuilder bld = new GenericEntityIndexBuilder("user");
        EntityIndex index = bld.build();
        assertThat(index, notNullValue());
        assertThat(index.getEntities(20L), hasSize(0));
    }

    @Test
    public void testAddEntity() {
        GenericEntityIndexBuilder bld = new GenericEntityIndexBuilder("user");
        Entity rating = factory.rating(10, 100, 3.5);
        bld.add(rating);
        EntityIndex index = bld.build();
        assertThat(index, notNullValue());
        assertThat(index.getEntities(10L), hasSize(1));
        assertThat(index.getEntities(10L), contains(rating));
        assertThat(index.getEntities(11L), hasSize(0));
    }

    @Test
    public void testAddEntities() {
        GenericEntityIndexBuilder bld = new GenericEntityIndexBuilder("user");
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
