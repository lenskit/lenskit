/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
