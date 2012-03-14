/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
/**
 *
 */
package org.grouplens.lenskit.baseline;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestConstantPredictor {

    @Test
    public void testConstantPredict() {
        BaselinePredictor pred = new ConstantPredictor(5);
        UserVector map = new UserVector(10l, Long2DoubleMaps.EMPTY_MAP);
        SparseVector pv = pred.predict(map, new LongArrayList(new long[]{2l}));
        assertEquals(5, pv.get(2l), 0.00001);
    }
}
