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
package org.lenskit.eval.traintest

import org.grouplens.grapht.CachePolicy
import org.grouplens.grapht.Component
import org.grouplens.grapht.graph.DAGNode
import org.grouplens.grapht.reflect.Satisfactions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.config.ConfigHelpers
import org.lenskit.data.dao.DataAccessObject
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.data.ratings.Rating

import static org.grouplens.lenskit.util.test.ExtraMatchers.existingFile
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class ComponentCacheTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()
    def source = StaticDataSource.fromList([
            Rating.create(1, 10, 3.5),
            Rating.create(1, 11, 2.5),
            Rating.create(2, 10, 3.0)
    ])
    def dao = source.get()

    ComponentCache cache
    def config = ConfigHelpers.load {
        bind ItemScorer to ItemMeanRatingItemScorer
        bind DataAccessObject to dao
    }

    @Before
    void createCache() {
        cache = new ComponentCache(folder.root.toPath(), null)
    }

    @Test
    public void testCacheObject() {
        def graph = config.buildGraph()
        def node = graph.reachableNodes.find {
            it.label.satisfaction.type == ItemMeanRatingItemScorer
        }
        def object = cache.instantiate(node)
        def other = cache.instantiate(node)
        assertThat object,
                   sameInstance(other)
        assertThat new File(folder.root, "${cache.makeNodeKey(node)}.dat.gz"),
                   existingFile()
    }

    @Test
    public void testReadDiskCache() {
        def graph = config.buildGraph()
        def node = graph.reachableNodes.find {
            it.label.satisfaction.type == ItemMeanRatingItemScorer
        }
        def object = cache.instantiate(node)
        cache.cache[node].cachedObject = null
        def other = cache.instantiate(node)
        // this doesn't really test that it was actually read from the file
        // that involves more custom classes
        assertThat other, notNullValue()
    }

    @Test
    public void testConfigureNull() {
        def config = ConfigHelpers.load {
            bind ItemScorer to ItemMeanRatingItemScorer
            bind ItemMeanRatingItemScorer to null
            bind DataAccessObject to dao
        }
        def graph = config.buildGraph()
        def node = graph.reachableNodes.find {
            it.label.satisfaction.type == ItemMeanRatingItemScorer
        }
        def object = cache.instantiate(node)
        assertThat object, nullValue()
    }

    @Test
    public void testUnequalKeys() {
        def node1 = DAGNode.newBuilder(Component.create(Satisfactions.instance("foo"),
                                                        CachePolicy.MEMOIZE))
                           .build()
        def node2 = DAGNode.newBuilder(Component.create(Satisfactions.instance("bar"),
                                                        CachePolicy.MEMOIZE))
                           .build()
        assertThat cache.makeNodeKey(node1), not(equalTo(cache.makeNodeKey(node2)))
    }

    @Test
    public void testEqualKeys() {
        def node1 = DAGNode.newBuilder(Component.create(Satisfactions.instance("foo"),
                                                        CachePolicy.MEMOIZE))
                           .build()
        def node2 = DAGNode.newBuilder(Component.create(Satisfactions.instance("foo"),
                                                        CachePolicy.MEMOIZE))
        .build()
        assertThat cache.makeNodeKey(node1), equalTo(cache.makeNodeKey(node2))
    }

    @Test
    public void testPolicyUnequalKeys() {
        def node1 = DAGNode.newBuilder(Component.create(Satisfactions.instance("foo"),
                                                        CachePolicy.MEMOIZE))
                           .build()
        def node2 = DAGNode.newBuilder(Component.create(Satisfactions.instance("foo"),
                                                        CachePolicy.NEW_INSTANCE))
                           .build()
        assertThat cache.makeNodeKey(node1), not(equalTo(cache.makeNodeKey(node2)))
    }
}
