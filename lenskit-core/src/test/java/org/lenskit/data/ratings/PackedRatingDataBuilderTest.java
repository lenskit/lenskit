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
package org.lenskit.data.ratings;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.grouplens.lenskit.util.test.ExtraMatchers.equivalentTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PackedRatingDataBuilderTest {
    PackedRatingDataBuilder bld;

    private static Matcher<Preference> samePreferenceAs(Preference p) {
        return equivalentTo(p, Ratings.preferenceEquivalence());
    }

    @Before
    public void createBuilder() {
        bld = new PackedRatingDataBuilder();
    }

    @Test
    public void testInitialState() {
        assertThat(bld.size(), equalTo(0));
        PackedRatingData data = bld.build();
        assertThat(data, notNullValue());
        assertThat(data.size(), equalTo(0));
    }

    @Test
    public void testAddPreference() {
        int idx = bld.add(10, 39, 3.5);
        assertThat(idx, equalTo(0));
        assertThat(bld.size(), equalTo(1));
        PackedRatingData data = bld.build();
        assertThat(data.size(), equalTo(1));
        RatingMatrixEntry p2 = data.getEntry(0);
        assertThat(p2.getIndex(), equalTo(0));
        assertThat(p2.getUserIndex(), equalTo(0));
        assertThat(p2.getItemIndex(), equalTo(0));
        assertThat(p2.getUserId(), equalTo(10L));
        assertThat(p2.getItemId(), equalTo(39L));
        assertThat(p2.getValue(), equalTo(3.5));
    }

    @Test
    public void testAddMany() {
        Random rnd = new Random();
        long[] users = new long[500];
        long[] items = new long[1000];
        for (int i = 0; i < 500; i++) {
            users[i] = rnd.nextInt(20000);
            items[i * 2] = rnd.nextInt(20000);
            items[i * 2 + 1] = rnd.nextInt(20000);
        }
        Preference[] prefs = new Preference[10000];
        for (int i = 0; i < 10000; i++) {
            Preference p = prefs[i] = Rating.create(
                    users[rnd.nextInt(500)],
                    items[rnd.nextInt(1000)],
                    rnd.nextGaussian());
            bld.add(p.getUserId(), p.getItemId(), p.getValue());
        }
        assertThat(bld.size(), equalTo(10000));
        PackedRatingData data = bld.build();
        assertThat(data.size(), equalTo(10000));
        PackedRatingData.IndirectEntry ip = data.getEntry(-1);
        for (int i = 0; i < 10000; i++) {
            ip.setIndex(i);
            assertTrue(ip.isValid());
            assertThat(ip, samePreferenceAs(prefs[i]));
            assertThat(ip.getUserIndex(),
                       equalTo(data.getUserIndex().getIndex(prefs[i].getUserId())));
            assertThat(ip.getItemIndex(),
                       equalTo(data.getItemIndex().getIndex(prefs[i].getItemId())));
        }
    }
}
