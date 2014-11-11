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
package org.grouplens.lenskit.data.event;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class LikeBuilderTest {
    LikeBuilder builder;

    @Before
    public void createBuilder() {
        builder = new LikeBuilder();
    }

    @Test
    public void testBasicBuild() {
        Like evt = builder.setUserId(42)
                          .setItemId(39)
                          .build();
        assertThat(evt, notNullValue());
        assertThat(evt.getUserId(), equalTo(42L));
        assertThat(evt.getItemId(), equalTo(39L));
        assertThat(evt.getTimestamp(), equalTo(-1L));
    }

    @Test
    public void testTimestampBuild() {
        Like evt = builder.setUserId(42)
                          .setItemId(39)
                          .setTimestamp(1000)
                          .build();
        assertThat(evt, notNullValue());
        assertThat(evt.getUserId(), equalTo(42L));
        assertThat(evt.getItemId(), equalTo(39L));
        assertThat(evt.getTimestamp(), equalTo(1000L));
    }
}
