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
package org.lenskit.data.entities;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;

public class EntityTypeTest {
    @Test
    public void testCreate() {
        EntityType wombat = EntityType.forName("wombat");
        EntityType wombat2 = EntityType.forName("wombat");
        EntityType woozle = EntityType.forName("woozle");

        assertThat(wombat.getName(), equalTo("wombat"));
        assertThat(woozle.getName(), equalTo("woozle"));

        assertThat(wombat, equalTo(wombat2));
        assertThat(wombat, sameInstance(wombat2));
        assertThat(wombat, not(equalTo(woozle)));
    }

    @Test
    public void testSerialize() {
        EntityType wombat = EntityType.forName("wombat");

        EntityType cloned = SerializationUtils.clone(wombat);
        assertThat(cloned, sameInstance(wombat));
    }

    @Test
    public void testCaseNorm() {
        EntityType wombat = EntityType.forName("Wombat");
        assertThat(wombat.getName(), equalTo("wombat"));
        assertThat(EntityType.forName("wombaT"), sameInstance(wombat));
    }
}
