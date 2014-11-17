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

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SimpleLikeBatchTest {
    @Test
    public void testSimpleLikeBatch() {
        LikeBatch like = Events.likeBatch(42, 67, 39);
        assertThat(like.getUserId(), equalTo(42L));
        assertThat(like.getItemId(), equalTo(67L));
        assertThat(like.getTimestamp(), equalTo(-1L));
        assertThat(like.getCount(), equalTo(39));
    }

    @Test
    public void testEquals() {
        LikeBatch like = Events.likeBatch(42, 67, 1989);
        LikeBatch equalLike = Events.likeBatch(42, 67, 1989);
        LikeBatch differentUser = Events.likeBatch(1, 67, 1989);
        LikeBatch differentItem = Events.likeBatch(42, 42, 1989);
        LikeBatch differentCount = Events.likeBatch(42, 67, 2014);

        assertThat(like.equals(like), equalTo(true));
        assertThat(like.equals(equalLike), equalTo(true));
        assertThat(like.equals(differentUser), equalTo(false));
        assertThat(like.equals(differentItem), equalTo(false));
        assertThat(like.equals(differentCount), equalTo(false));
        assertThat(like.equals(null), equalTo(false));

    }
}
