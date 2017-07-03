/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.results;

import org.junit.Test;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.java.quickcheck.generator.CombinedGenerators.lists;
import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someLists;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static net.java.quickcheck.generator.PrimitiveGenerators.longs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Tests for result utility methods.
 */
public class ResultsTest {
    @Test
    public void testCollectEmpty() {
        ResultList list = Stream.<Result>empty()
                .collect(Results.listCollector());
        assertThat(list, hasSize(0));
    }

    @Test
    public void testCollectSingleton() {
        ResultList list = Stream.of(Results.create(42, 3.5))
                                .collect(Results.listCollector());
        assertThat(list, contains(Results.create(42, 3.5)));
    }

    @Test
    public void testCollectMany() {
        for (List<Long> ids: someLists(longs(), 10, 100)) {
            List<Double> values = lists(doubles(), ids.size()).next();
            ResultList results = IntStream.range(0, ids.size())
                                          .mapToObj(i -> Results.create(ids.get(i), values.get(i)))
                                          .collect(Results.listCollector());
            assertThat(results.stream().map(Result::getId).collect(Collectors.toList()),
                       equalTo(ids));
            assertThat(results.stream().map(Result::getScore).collect(Collectors.toList()),
                       equalTo(values));
        }
    }

    @Test
    public void testParallelCollect() {
        for (List<Long> ids: someLists(longs(), 100, 10000)) {
            List<Double> values = lists(doubles(), ids.size()).next();
            ResultList results = IntStream.range(0, ids.size())
                                          .parallel()
                                          .mapToObj(i -> Results.create(ids.get(i), values.get(i)))
                                          .collect(Results.listCollector());
            assertThat(results.stream().map(Result::getId).collect(Collectors.toList()),
                       equalTo(ids));
            assertThat(results.stream().map(Result::getScore).collect(Collectors.toList()),
                       equalTo(values));
        }
    }
}