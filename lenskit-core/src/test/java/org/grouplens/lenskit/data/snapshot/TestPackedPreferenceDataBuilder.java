/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.snapshot;

import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.data.pref.SimplePreference;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestPackedPreferenceDataBuilder {
    PackedPreferenceDataBuilder bld;

    @Before
    public void createBuilder() {
        bld = new PackedPreferenceDataBuilder();
    }

    @Test
    public void testInitialState() {
        assertThat(bld.size(), equalTo(0));
        PackedPreferenceData data = bld.build();
        assertThat(data, notNullValue());
        assertThat(data.size(), equalTo(0));
    }

    @Test
    public void testAddPreference() {
        Preference pref = new SimplePreference(10, 39, 3.5);
        int idx = bld.add(pref);
        assertThat(idx, equalTo(0));
        assertThat(bld.size(), equalTo(1));
        PackedPreferenceData data = bld.build();
        assertThat(data.size(), equalTo(1));
        IndexedPreference p2 = data.preference(0);
        assertThat(p2, equalTo(pref));
        assertThat(p2.getIndex(), equalTo(0));
        assertThat(p2.getUserIndex(), equalTo(0));
        assertThat(p2.getItemIndex(), equalTo(0));
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
            prefs[i] = new SimplePreference(
                    users[rnd.nextInt(500)],
                    items[rnd.nextInt(1000)],
                    rnd.nextGaussian());
            bld.add(prefs[i]);
        }
        assertThat(bld.size(), equalTo(10000));
        PackedPreferenceData data = bld.build();
        assertThat(data.size(), equalTo(10000));
        PackedPreferenceData.IndirectPreference ip = data.preference(-1);
        for (int i = 0; i < 10000; i++) {
            ip.setIndex(i);
            assertTrue(ip.isValid());
            assertThat(ip, equalTo(prefs[i]));
            assertThat(ip.getUserIndex(),
                       equalTo(data.getUserIndex().getIndex(prefs[i].getUserId())));
            assertThat(ip.getItemIndex(),
                       equalTo(data.getItemIndex().getIndex(prefs[i].getItemId())));
        }
    }

    @Test
    public void testRemove() {
        bld.add(new SimplePreference(1, 3, 20));
        bld.add(new SimplePreference(4, 2, -3));
        bld.add(new SimplePreference(2, 3, Math.PI));
        assertThat(bld.size(), equalTo(3));
        bld.release(1);
        assertThat(bld.size(), equalTo(2));
        PackedPreferenceData data = bld.build();
        assertThat(data.size(), equalTo(2));
        assertThat(data.preference(0),
                   equalTo((Preference) new SimplePreference(1, 3, 20)));
        assertThat(data.preference(1),
                   equalTo((Preference) new SimplePreference(2, 3, Math.PI)));
    }

    @Test
    public void testRemoveLast() {
        bld.add(new SimplePreference(1, 3, 20));
        bld.add(new SimplePreference(4, 2, -3));
        bld.add(new SimplePreference(2, 3, Math.PI));
        assertThat(bld.size(), equalTo(3));
        bld.release(2);
        assertThat(bld.size(), equalTo(2));
        PackedPreferenceData data = bld.build();
        assertThat(data.size(), equalTo(2));
        assertThat(data.preference(0),
                   equalTo((Preference) new SimplePreference(1, 3, 20)));
        assertThat(data.preference(1),
                   equalTo((Preference) new SimplePreference(4, 2, -3)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveBad() {
        bld.add(new SimplePreference(1, 3, 20));
        bld.add(new SimplePreference(4, 2, -3));
        bld.add(new SimplePreference(2, 3, Math.PI));
        bld.release(7);
    }

    @Test
    public void testReuse() {
        bld.add(new SimplePreference(1, 3, 20));
        bld.add(new SimplePreference(4, 2, -3));
        bld.add(new SimplePreference(2, 3, Math.PI));
        assertThat(bld.size(), equalTo(3));
        bld.release(1);
        assertThat(bld.size(), equalTo(2));
        int idx = bld.add(new SimplePreference(7, 2, Math.E));
        assertThat(bld.size(), equalTo(3));
        assertThat(idx, equalTo(1));
        PackedPreferenceData data = bld.build();
        assertThat(data.size(), equalTo(3));
        assertThat(data.preference(0),
                   equalTo((Preference) new SimplePreference(1, 3, 20)));
        assertThat(data.preference(1),
                   equalTo((Preference) new SimplePreference(7, 2, Math.E)));
        assertThat(data.preference(2),
                   equalTo((Preference) new SimplePreference(2, 3, Math.PI)));
    }
}
