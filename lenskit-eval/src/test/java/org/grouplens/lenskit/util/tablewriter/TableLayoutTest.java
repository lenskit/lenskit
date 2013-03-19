package org.grouplens.lenskit.util.tablewriter;

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
}
