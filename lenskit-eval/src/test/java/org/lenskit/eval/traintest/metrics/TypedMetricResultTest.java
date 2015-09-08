package org.lenskit.eval.traintest.metrics;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TypedMetricResultTest {
    @Test
    public void testEmpty() {
        assertThat(TypedMetricResult.getColumns(NoResult.class),
                   hasSize(0));
        assertThat(new NoResult().getValues().size(),
                   equalTo(0));
    }

    @Test
    public void testField() {
        assertThat(TypedMetricResult.getColumns(FieldResult.class),
                   contains("foo"));
        assertThat(new FieldResult("bar").getValues(),
                   hasEntry("foo", (Object) "bar"));
    }

    @Test
    public void testMethod() {
        assertThat(TypedMetricResult.getColumns(GetterResult.class),
                   contains("foo"));
        assertThat(new GetterResult("bar").getValues(),
                   hasEntry("foo", (Object) "bar"));
    }

    static class NoResult extends TypedMetricResult { }

    static class FieldResult extends TypedMetricResult {
        @MetricColumn("foo")
        public final String foo;

        public FieldResult(String v) {
            foo = v;
        }
    }

    static class GetterResult extends TypedMetricResult {
        String foo;

        public GetterResult(String v) {
            foo = v;
        }

        @MetricColumn("foo")
        public String getFoo() {
            return foo;
        }
    }
}