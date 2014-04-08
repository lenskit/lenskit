package org.grouplens.lenskit.eval.metrics;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ResultConverterTest {
    @Test
    public void testEmptyType() {
        ResultConverter<String> foo = ResultConverter.create(String.class);
        assertThat(foo.getColumnLabels(), hasSize(0));
        assertThat(foo.getColumns("hello"), hasSize(0));
    }

    @Test
    public void testMethod() {
        ResultConverter<MethodResult> foo = ResultConverter.create(MethodResult.class);
        assertThat(foo.getColumnLabels(), contains("hello"));
        assertThat(foo.getColumns(new MethodResult()),
                   contains((Object) "world"));
    }

    @Test
    public void testField() {
        ResultConverter<FieldResult> foo = ResultConverter.create(FieldResult.class);
        assertThat(foo.getColumnLabels(), contains("hello"));
        assertThat(foo.getColumns(new FieldResult()),
                   contains((Object) "world"));
    }

    private static class MethodResult {
        @ResultColumn("hello")
        public String getHello() {
            return "world";
        }
    }

    private static class FieldResult {
        @ResultColumn("hello")
        public final String hello = "world";
    }
}
