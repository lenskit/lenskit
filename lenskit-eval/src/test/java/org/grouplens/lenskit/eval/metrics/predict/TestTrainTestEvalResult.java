package org.grouplens.lenskit.eval.metrics.predict;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import org.grouplens.lenskit.eval.results.TrainTestEvalResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestTrainTestEvalResult {

    String[] header = {"a", "b", "c", "d", "end"};
    Object[] row1 = {String.valueOf("r1"), Integer.valueOf(0), Double.valueOf(1.23), 10, 22.2};
    Object[] row2 = {String.valueOf("r2"), Integer.valueOf(1), Double.valueOf(2.23), 10, 2.2122};
    Object[] row3 = {String.valueOf("r3"), Integer.valueOf(1), Double.valueOf(3.23), 100, 2.23};
    Object[] row4 = {String.valueOf("r4"), Integer.valueOf(3), Double.valueOf(4.23), 1000, 2.24};
    TrainTestEvalResult result;

    @Before
    public void Initialize() {
        result = new TrainTestEvalResult(Arrays.asList(header));
        result.addResultRow(row1);
        result.addResultRow(row2);
        result.addResultRow(row3);
        result.addResultRow(row4);
    }

    @Test
    public void TestFilter() {
        assertEquals(result.filter("a", "r1").getSize(), 1);
        assertEquals(result.filter("a", "r123").getSize(), 0);
        assertEquals(result.filter("b", 1).getSize(), 2);
        assertEquals(result.filter("end", 22.2).getSize(), 1);
        assertEquals(result.filter("d",10).filter("b", 1).getSize(), 1);


    }

    @Test
    public void TestValues() {
        assertEquals(result.getValues("b").length, 4);
        assertEquals(result.getValues("b")[1], 1);
        assertEquals(result.filter("d",10).getValues("a")[0], "r1");
        assertEquals(result.getValues("None").length, 0);
    }

    @Test
    public void TestSum() {
        assertEquals(result.getSum("b"), Double.valueOf(5));
        assertTrue(result.getSum("a").isNaN());
        assertTrue(result.getSum("NONE").isNaN());
        assertEquals(result.getSum("c"), Double.valueOf(10.92));
    }

    @Test
    public void TestAverage() {
        assertEquals(result.getAverage("b"), Double.valueOf(5/4.0));
        assertTrue(result.getAverage("a").isNaN());
        assertTrue(result.getAverage("none").isNaN());
    }

}