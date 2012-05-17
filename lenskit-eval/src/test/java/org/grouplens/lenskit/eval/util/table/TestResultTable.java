package org.grouplens.lenskit.eval.util.table;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestResultTable {

    String[] header = {"a", "b", "c", "d", "end"};
    Object[] row1 = {String.valueOf("r1"), Integer.valueOf(0), Double.valueOf(1.23), 10, 22.2};
    Object[] row2 = {String.valueOf("r2"), Integer.valueOf(1), Double.valueOf(2.23), 10, 2.2122};
    Object[] row3 = {String.valueOf("r3"), Integer.valueOf(1), Double.valueOf(3.23), 100, 2.23};
    Object[] row4 = {String.valueOf("r4"), Integer.valueOf(3), Double.valueOf(4.23), 1000, 2.24};
    ResultTable result;

    @Before
    public void Initialize() {
        result = new ResultTable();
        result.setHeader(Arrays.asList(header));
        result.addResultRow(row1);
        result.addResultRow(row2);
        result.addResultRow(row3);
        result.addResultRow(row4);
    }

    @Test
    public void TestFilter() {
        assertEquals(result.filter("a", "r1").size(), 1);
        assertEquals(result.filter("a", "r123").size(), 0);
        assertEquals(result.filter("b", 1).size(), 2);
        assertEquals(result.filter("end", 22.2).size(), 1);
        assertEquals(result.filter("d",10).filter("b", 1).size(), 1);


    }

    @Test
    public void TestValues() {
        assertEquals(result.column("b").size(), 4);
        assertEquals(result.column("b").get(1), 1);
        assertEquals(result.filter("d",10).column("a").get(0), "r1");
        assertEquals(result.column("None").size(), 0);
    }

    @Test
    public void TestSum() {
        assertEquals(result.column("b").sum(), Double.valueOf(5));
        assertTrue(result.column("a").sum().isNaN());
        assertTrue(result.column("NONE").sum().isNaN());
        assertEquals(result.column("c").sum(), Double.valueOf(10.92));
    }

    @Test
    public void TestAverage() {
        assertEquals(result.column("b").average(), Double.valueOf(5/4.0));
        assertTrue(result.column("a").average().isNaN());
        assertTrue(result.column("NONE").average().isNaN());
    }

}