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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class EntityDefaultsTest {
    @Test
    public void testMissingDefaults() {
        EntityDefaults defaults = EntityDefaults.lookup(EntityType.forName("wombat"));
        // we don't know anything about wombats
        assertThat(defaults, nullValue());
    }

    @Test
    public void testRatingDefaults() {
        EntityDefaults defaults = EntityDefaults.lookup(EntityType.forName("rating"));
        assertThat(defaults, notNullValue());
        assertThat(defaults.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(defaults.getCommonAttributes(),
                   containsInAnyOrder((TypedName) CommonAttributes.USER_ID,
                                      CommonAttributes.ITEM_ID,
                                      CommonAttributes.RATING,
                                      CommonAttributes.TIMESTAMP));
        assertThat(defaults.getDefaultColumns(),
                   contains((TypedName) CommonAttributes.USER_ID,
                            CommonAttributes.ITEM_ID,
                            CommonAttributes.RATING,
                            CommonAttributes.TIMESTAMP));
        // FIXME Re-enable this assert when rating builders work
//        assertThat(defaults.getDefaultBuilder(),
//                   equalTo((Class) RatingBuilder.class));
    }
}
