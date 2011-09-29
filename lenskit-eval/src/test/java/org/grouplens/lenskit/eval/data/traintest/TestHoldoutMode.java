/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval.data.traintest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestHoldoutMode {
    Rating[] ratings = {
            new SimpleRating(0, 5, 1, 3, 5),
            new SimpleRating(1, 5, 4, 2.5, 7),
            new SimpleRating(2, 5, 42, 1.0, 1),
            new SimpleRating(3, 5, 37, 3, 9)
    };

    @Test
    public void testRandFromString() {
        assertThat(HoldoutMode.fromString("random"), equalTo(HoldoutMode.RANDOM));
        assertThat(HoldoutMode.fromString("Random"), equalTo(HoldoutMode.RANDOM));
    }
    
    @Test
    public void testTimestampFromString() {
        assertThat(HoldoutMode.fromString("time"), equalTo(HoldoutMode.TIMESTAMP));
        assertThat(HoldoutMode.fromString("Time"), equalTo(HoldoutMode.TIMESTAMP));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testTimestampInvalid() {
        HoldoutMode.fromString("foo");
    }
    
    @Test
    public void testRandomList() {
        List<Rating> rlst = Lists.newArrayList(ratings);
        int p = HoldoutMode.RANDOM.partition(rlst, 2);
        assertThat(p, equalTo(2));
    }
    
    @Test
    public void testRandomListLong() {
        List<Rating> rlst = Lists.newArrayList(ratings);
        int p = HoldoutMode.RANDOM.partition(rlst, 5);
        assertThat(p, equalTo(0));
    }
    
    @Test
    public void testTSList() {
        List<Rating> rlst = Lists.newArrayList(ratings);
        int p = HoldoutMode.TIMESTAMP.partition(rlst, 1);
        assertThat(p, equalTo(3));
        assertThat(rlst.get(0).getId(), equalTo(2l));
        assertThat(rlst.get(1).getId(), equalTo(0l));
        assertThat(rlst.get(2).getId(), equalTo(1l));
        assertThat(rlst.get(3).getId(), equalTo(3l));
    }
    
    @Test
    public void testTSListLong() {
        List<Rating> rlst = Lists.newArrayList(ratings);
        int p = HoldoutMode.TIMESTAMP.partition(rlst, 5);
        assertThat(p, equalTo(0));        
    }

}
