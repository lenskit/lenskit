package org.grouplens.lenskit.norm;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

/**
 * @author Michael Ekstrand
 */
public class TestValueArrayQuantizer {
    @Test
    public void testFindSingle() {
        Quantizer q = new ValueArrayQuantizer(new double[]{5.0});
        assertThat(q.getCount(), equalTo(1));
        assertThat(q.getValue(0), equalTo(5.0));
        assertThat(q.apply(2.5), equalTo(0));
        assertThat(q.apply(5.0), equalTo(0));
    }

    @Test
    public void testSomeElements() {
        Quantizer q = new ValueArrayQuantizer(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        assertThat(q.getCount(), equalTo(5));
        assertThat(q.getValue(0), equalTo(1.0));
        assertThat(q.apply(2.5), equalTo(2));
        assertThat(q.apply(5.0), equalTo(4));
        assertThat(q.apply(1.73), equalTo(1));
    }
}
