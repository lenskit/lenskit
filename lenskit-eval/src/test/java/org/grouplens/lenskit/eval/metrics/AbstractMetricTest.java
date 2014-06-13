/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.metrics;

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AbstractMetricTest {
    
    // a test metric we can use to test the suffix/prefix logic on.
    private static class AggregateResult {
        @ResultColumn("foo")
        public final int tmp = 0;
    }
    
    private static class UserResult {
        @ResultColumn("bar")
        public final int tmp = 0;
    }
    
    private static class TestMetric extends AbstractMetric<Void, AggregateResult, UserResult> {
        private final String prefix;
        private final String suffix;

        private TestMetric(String prefix, String suffix) {
            super(AggregateResult.class, UserResult.class);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        protected String getPrefix() {
            return prefix;
        }

        @Override
        protected String getSuffix() {
            return suffix;
        }

        @Override
        protected UserResult doMeasureUser(TestUser user, Void context) {
            return null;
        }

        @Override
        protected AggregateResult getTypedResults(Void context) {
            return null;
        }

        @Nullable
        @Override
        public Void createContext(Attributed algorithm, TTDataSet dataSet, Recommender recommender) {
            return null;
        }
    }

    @Test
    public void testNameGenerationNoPrefixNoSuffix() {
        TestMetric tm = new TestMetric(null, null);
        assertThat(tm.getColumnLabels(), notNullValue());
        assertThat(tm.getColumnLabels().size(), is(1));
        assertThat(tm.getColumnLabels(), contains("foo"));
        assertThat(tm.getUserColumnLabels(), notNullValue());
        assertThat(tm.getUserColumnLabels().size(), is(1));
        assertThat(tm.getUserColumnLabels(), contains("bar"));
    }

    @Test
    public void testNameGenerationNoPrefix() {
        TestMetric tm = new TestMetric(null, "suffix");
        assertThat(tm.getColumnLabels(), notNullValue());
        assertThat(tm.getColumnLabels().size(), is(1));
        assertThat(tm.getColumnLabels(), contains("foo.suffix"));
        assertThat(tm.getUserColumnLabels(), notNullValue());
        assertThat(tm.getUserColumnLabels().size(), is(1));
        assertThat(tm.getUserColumnLabels(), contains("bar.suffix"));
    }

    @Test
    public void testNameGenerationNoSuffix() {
        TestMetric tm = new TestMetric("prefix", null);
        assertThat(tm.getColumnLabels(), notNullValue());
        assertThat(tm.getColumnLabels().size(), is(1));
        assertThat(tm.getColumnLabels(), contains("prefix.foo"));
        assertThat(tm.getUserColumnLabels(), notNullValue());
        assertThat(tm.getUserColumnLabels().size(), is(1));
        assertThat(tm.getUserColumnLabels(), contains("prefix.bar"));
    }



    @Test
    public void testNameGeneration() {
        TestMetric tm = new TestMetric("prefix", "suffix");
        assertThat(tm.getColumnLabels(), notNullValue());
        assertThat(tm.getColumnLabels().size(), is(1));
        assertThat(tm.getColumnLabels(), contains("prefix.foo.suffix"));
        assertThat(tm.getUserColumnLabels(), notNullValue());
        assertThat(tm.getUserColumnLabels().size(), is(1));
        assertThat(tm.getUserColumnLabels(), contains("prefix.bar.suffix"));
    }
}
