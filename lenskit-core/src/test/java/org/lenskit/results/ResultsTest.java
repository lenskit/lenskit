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