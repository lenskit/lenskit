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
package org.grouplens.lenskit.util.table;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class TableTest {

    String[] header = {"a", "b", "c", "d", "end"};
    Object[] row1 = {String.valueOf("r1"), Integer.valueOf(0), Double.valueOf(1.23), 10, 22.2};
    Object[] row2 = {String.valueOf("r2"), Integer.valueOf(1), Double.valueOf(2.23), 10, 2.2122};
    Object[] row3 = {String.valueOf("r3"), Integer.valueOf(1), Double.valueOf(3.23), 100, 2.23};
    Object[] row4 = {String.valueOf("r4"), Integer.valueOf(3), Double.valueOf(4.23), 1000, 2.24};
    Table table;

    @Before
    public void Initialize() {
        TableBuilder builder = new TableBuilder(Arrays.asList(header));
        builder.addRow(row1);
        builder.addRow(row2);
        builder.addRow(row3);
        builder.addRow(row4);
        table = builder.build();
    }

    @Test
    public void TestFilter() {
        assertThat(table.filter("a", "r1").size(), equalTo(1));
        assertThat(table.filter("a", "r123").size(), equalTo(0));
        assertThat(table.filter("b", 1).size(), equalTo(2));
        assertThat(table.filter("end", 22.2).size(), equalTo(1));
        assertThat(table.filter("d", 10).filter("b", 1).size(), equalTo(1));
    }

    @Test
    public void TestValues() {
        assertThat(table.column("b").size(),
                   equalTo(4));
        assertThat(table.column("b").get(1),
                   equalTo((Object) 1));
        assertThat(table.filter("d", 10).column("a").get(0),
                   equalTo((Object) "r1"));
        try {
            table.column("None");
            fail("missing column should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            /* expected, good */
        }
    }

    @Test
    public void TestSum() {
        assertThat(table.column("b").sum(),
                   closeTo(5, 1.0e-5));
        try {
            table.column("a").sum();
            fail("summing non-numeric column should throw exception");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
        assertThat(table.column("c").sum(),
                   closeTo(10.92, 1.0e-5));
    }

    @Test
    public void TestAverage() {
        assertThat(table.column("b").average(),
                   closeTo(5 / 4.0, 1.0e-5));
        try {
            table.column("a").average();
            fail("summing non-numeric column should throw exception");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }

}
