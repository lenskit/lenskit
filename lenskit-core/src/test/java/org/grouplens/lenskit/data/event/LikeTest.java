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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class LikeTest {
    @Test
     public void testSimpleLike() {
        Like like = Like.create(42, 67);
        assertThat(like.getUserId(), equalTo(42L));
        assertThat(like.getItemId(), equalTo(67L));
        assertThat(like.getTimestamp(), equalTo(-1L));
    }

    @Test
    public void testTimestampedLike() {
        long ts = System.currentTimeMillis() / 1000;
        Like like = Like.create(42, 67, ts);
        assertThat(like.getUserId(), equalTo(42L));
        assertThat(like.getItemId(), equalTo(67L));
        assertThat(like.getTimestamp(), equalTo(ts));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedLike() {
        Like like = Events.like(42, 67);
        assertThat(like.getUserId(), equalTo(42L));
        assertThat(like.getItemId(), equalTo(67L));
        assertThat(like.getTimestamp(), equalTo(-1L));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedTimestampedLike() {
        long ts = System.currentTimeMillis() / 1000;
        Like like = Events.like(42, 67, ts);
        assertThat(like.getUserId(), equalTo(42L));
        assertThat(like.getItemId(), equalTo(67L));
        assertThat(like.getTimestamp(), equalTo(ts));
    }

    @Test
    public void testEquals() {
        Like like = Like.create(42, 67, 1989);
        Like equalLike = Like.create(42, 67, 1989);
        Like differentUser = Like.create(1, 67, 1989);
        Like differentItem = Like.create(42, 42, 1989);
        Like differentTime = Like.create(42, 67, 2014);

        assertThat(like.equals(like), equalTo(true));
        assertThat(like.equals(equalLike), equalTo(true));
        assertThat(like.equals(differentUser), equalTo(false));
        assertThat(like.equals(differentItem), equalTo(false));
        assertThat(like.equals(differentTime), equalTo(false));
        assertThat(like.equals(null), equalTo(false));
        assertThat(like.equals("false"), equalTo(false));
    }
}
