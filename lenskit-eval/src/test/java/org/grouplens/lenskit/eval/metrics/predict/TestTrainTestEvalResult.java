package org.grouplens.lenskit.eval.metrics.predict;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import org.grouplens.lenskit.eval.results.TrainTestEvalResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestTrainTestEvalResult {

    String[] header = {"a", "b", "c", "d", "end"};
    Object[] row1 = {String.valueOf("r1"), Integer.valueOf(0), Double.valueOf(1.23), 10, 22.2};
    Object[] row2 = {String.valueOf("r2"), Integer.valueOf(1), Double.valueOf(2.23), 10, 2.2122};
    Object[] row3 = {String.valueOf("r3"), Integer.valueOf(1), Double.valueOf(3.23), 100, 2.23};
    Object[] row4 = {String.valueOf("r4"), Integer.valueOf(3), Double.valueOf(4.23), 1000, 2.24};

    @Test
    public void TestValues() {
        TrainTestEvalResult result = new TrainTestEvalResult(Arrays.asList(header));
        result.addResultRow(row1);
        result.addResultRow(row2);
        result.addResultRow(row3);
        result.addResultRow(row4);
        TrainTestEvalResult rows = result.getRows("a", "r1");
        assertEquals(rows.getField()[4],"end");
        assertEquals(rows.getValues("b")[0],0);
        assertEquals(rows.getValues("end")[0],22.2);
        assertEquals(rows.getValues("a")[0],"r1");
        assertEquals(result.getRows("d",10).getSize(), 2);
        assertEquals(result.getRows("d",10).getRows("b", 1).getSize(), 1);
        assertEquals(result.getRows("d",10).getRows("b", 1).getValues("end").length, 1);
        assertEquals(result.getRows("d",10).getRows("b", 1).getValues("end")[0], 2.2122);
    }

}
