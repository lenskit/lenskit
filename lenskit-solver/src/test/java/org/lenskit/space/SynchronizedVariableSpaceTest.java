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

package org.lenskit.space;

import org.apache.commons.math3.stat.StatUtils;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SynchronizedVariableSpaceTest {

    @Test
    public void testSynchronizedVariableSpace() {
        VariableSpace variableSpace = new SynchronizedVariableSpace();
        variableSpace.requestScalarVar("biases", 10, 0.0, false, false);
        assertThat(variableSpace.getScalarVarByName("biases").getDimension(), equalTo(10));
        assertThat(variableSpace.getScalarVarByNameIndex("biases", 0), equalTo(0.0));
        variableSpace.requestVectorVar("factors", 10, 10, 0.0, true, true);
        assertThat(variableSpace.getVectorVarByName("factors").size(), equalTo(10));
        assertThat(StatUtils.sum(variableSpace.getVectorVarByNameIndex("factors", 0).toArray()), closeTo(1.0, 0.01));
    }
}
