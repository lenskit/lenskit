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
package org.lenskit.util.table;

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
