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
