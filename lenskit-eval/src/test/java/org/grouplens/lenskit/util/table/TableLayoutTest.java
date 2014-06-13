/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
