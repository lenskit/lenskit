package org.lenskit.eval.traintest.metrics;

import org.junit.Test;

import java.util.Map;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someMaps;
import static net.java.quickcheck.generator.PrimitiveGenerators.strings;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class MetricResultTest {
    @Test
    public void testEmpty() {
        assertThat(MetricResult.empty().getValues().keySet(),
                   hasSize(0));
    }

    @Test
    public void testFromMap() {
        for (Map<String,String> map: someMaps(strings(), strings())) {
            MetricResult result = MetricResult.fromMap(map);
            assertThat(result.getValues(), equalTo((Map) map));
        }
    }
}