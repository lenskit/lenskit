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

    @Test
    public void testSubclassMethod() {
        assertThat(TypedMetricResult.getColumns(SubclassGetterResult.class),
                   containsInAnyOrder("foo", "bar"));
        assertThat(new SubclassGetterResult("hackem", "muche").getValues(),
                   allOf(hasEntry("foo", (Object) "hackem"),
                         hasEntry("bar", (Object) "muche")));
    }

    @Test
    public void testSubclassField() {
        assertThat(TypedMetricResult.getColumns(SubclassFieldResult.class),
                   containsInAnyOrder("foo", "bar"));
        assertThat(new SubclassFieldResult("hackem", "muche").getValues(),
                   allOf(hasEntry("foo", (Object) "hackem"),
                         hasEntry("bar", (Object) "muche")));
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

    static class SubclassGetterResult extends GetterResult {
        String bar;

        public SubclassGetterResult(String v1, String v2) {
            super(v1);
            bar = v2;
        }

        @MetricColumn("bar")
        public String getBar() {
            return bar;
        }
    }

    static class SubclassFieldResult extends FieldResult {
        @MetricColumn("bar")
        String bar;

        public SubclassFieldResult(String v1, String v2) {
            super(v1);
            bar = v2;
        }
    }
}
