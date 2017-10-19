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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test the TableLayout and TableLayoutBuilder.
 */
public class TableLayoutTest {
    private TableLayoutBuilder builder;

    @Before
    public void setUp() {
        builder = new TableLayoutBuilder();
    }

    @Test
    public void testEmpty() {
        TableLayout layout = builder.build();
        assertThat(layout.getColumnCount(), equalTo(0));
        assertThat(layout.getColumns(), hasSize(0));
    }

    @Test
    public void testAddColumns() {
        builder.addColumn("foo");
        builder.addColumn("bar");
        assertThat(builder.getColumnCount(), equalTo(2));
        TableLayout layout = builder.build();
        assertThat(layout.getColumnCount(), equalTo(2));
        assertThat(layout.getColumns(),
                   contains("foo", "bar"));
    }

    @Test
    public void testAddDupColummn() {
        builder.addColumn("foo");
        builder.addColumn("bar");
        try {
            builder.addColumn("foo");
            fail("inserting duplicate column should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            /* no-op, expected */
        }
    }

    @Test
    public void testIsolate() {
        builder.addColumn("foo");
        builder.addColumn("bar");
        TableLayout layout = builder.build();
        builder.addColumn("wombat");
        assertThat(layout.getColumnCount(),
                   equalTo(2));
        assertThat(layout.getColumns(),
                   contains("foo", "bar"));
        TableLayout l2 = builder.build();
        assertThat(l2.getColumns(),
                   contains("foo", "bar", "wombat"));
    }

    @Test
    public void testCopy() {
        builder.addColumn("foo");
        builder.addColumn("bar");
        TableLayout layout = builder.build();

        TableLayout l2 = TableLayoutBuilder.copy(layout).addColumn("wombat").build();
        assertThat(l2.getColumnCount(),
                   equalTo(3));
        assertThat(l2.getColumns(),
                   contains("foo", "bar", "wombat"));
    }
}
