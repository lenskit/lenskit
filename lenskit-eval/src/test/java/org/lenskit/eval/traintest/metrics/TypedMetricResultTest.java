/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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