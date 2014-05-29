/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.traintest

import org.grouplens.grapht.CachePolicy
import org.grouplens.grapht.Component
import org.grouplens.grapht.graph.DAGNode
import org.grouplens.grapht.reflect.Satisfactions
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.data.event.Ratings
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.grouplens.lenskit.util.test.ExtraMatchers.existingFile
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.nullValue
import static org.hamcrest.Matchers.sameInstance
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ComponentCacheTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder()
    def dao = EventCollectionDAO.create([
            Ratings.make(1, 10, 3.5),
            Ratings.make(1, 11, 2.5),
            Ratings.make(2, 10, 3.0)
    ])

    ComponentCache cache
    def config = ConfigHelpers.load {
        bind ItemScorer to ItemMeanRatingItemScorer
        bind EventDAO to dao
    }

    @Before
    void createCache() {
        cache = new ComponentCache(folder.root, null) 
    }

    @Test
    public void testCacheObject() {
        def graph = config.buildGraph()
        cache.registerSharedNodes(graph.reachableNodes)
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
        cache.registerSharedNodes(graph.reachableNodes)
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
            bind EventDAO to dao
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
