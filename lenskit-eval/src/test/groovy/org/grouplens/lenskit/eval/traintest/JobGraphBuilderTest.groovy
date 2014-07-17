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
package org.grouplens.lenskit.eval.traintest

import org.grouplens.grapht.Component
import org.grouplens.grapht.Dependency
import org.grouplens.grapht.graph.MergePool
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.baseline.UserMeanBaseline
import org.grouplens.lenskit.baseline.UserMeanItemScorer
import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.core.LenskitConfiguration
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstanceBuilder
import org.grouplens.lenskit.eval.data.GenericDataSource
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet
import org.grouplens.lenskit.eval.data.traintest.TTDataSet
import org.grouplens.lenskit.inject.GraphtUtils
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class JobGraphBuilderTest {
    TrainTestEvalTask task
    JobGraphBuilder builder
    TTDataSet dataSet
    LenskitConfiguration dataConfig
    ComponentCache cache

    @Before
    void createBuilder() {
        task = new TrainTestEvalTask()
        cache = new ComponentCache(null, null)
        builder = new JobGraphBuilder(task, cache)
        dataSet = GenericTTDataSet.newBuilder('data')
                                  .setTrain(new GenericDataSource('train', EventCollectionDAO.create([])))
                                  .setTest(new GenericDataSource('test', EventCollectionDAO.create([])))
                                  .build()
        dataConfig = new LenskitConfiguration()
        dataSet.configure(dataConfig)
    }

    @Test
    void testEmptyGraph() {
        def graph = builder.getGraph()
        assertThat graph, notNullValue()
        assertThat graph.reachableNodes, contains(graph)
    }

    @Test
    void testAddExternalJob() {
        def algo = new ExternalAlgorithmBuilder()
                .setName('foo')
                .setCommand(['cat'])
                .build()
        builder.addExternalJob(algo, dataSet)
        def graph = builder.getGraph()
        assertThat graph, notNullValue()
        assertThat graph.reachableNodes, hasSize(2)
        def jobs = graph.reachableNodes*.label.grep({it instanceof JobGraph.JobNode})*.job
        def algos = jobs*.algorithm
        assertThat algos, contains(algo)
    }

    @Test
    void testAddTwoExternalJobs() {
        def algo = new ExternalAlgorithmBuilder()
                .setName('foo')
                .setCommand(['cat'])
                .build()
        builder.addExternalJob(algo, dataSet)
        builder.addExternalJob(algo, dataSet)
        def graph = builder.getGraph()
        assertThat graph, notNullValue()
        assertThat graph.reachableNodes, hasSize(3)
        def jobs = graph.reachableNodes*.label.grep({it instanceof JobGraph.JobNode})*.job
        def algos = jobs*.algorithm
        assertThat algos, contains(algo, algo)

        // the nodes should have no dependencies themselves
        assertThat graph.adjacentNodes, hasSize(2)
        assertThat graph.adjacentNodes*.adjacentNodes.flatten(), hasSize(0)
    }

    @Test
    void testFenceTwoExternalJobs() {
        def algo = new ExternalAlgorithmBuilder()
                .setName('foo')
                .setCommand(['cat'])
                .build()
        builder.addExternalJob(algo, dataSet)
        builder.fence("fence")
        builder.addExternalJob(algo, dataSet)
        def graph = builder.getGraph()
        assertThat graph, notNullValue()
        assertThat graph.reachableNodes, hasSize(4)

        // still has the algorithms
        def jobs = graph.reachableNodes*.label.grep({it instanceof JobGraph.JobNode})*.job
        def algos = jobs*.algorithm
        assertThat algos, contains(algo, algo)

        // and we should have a linear chain
        assertThat graph.adjacentNodes, hasSize(1)
        assertThat graph.adjacentNodes*.label, contains(instanceOf(JobGraph.JobNode))
        assertThat graph.adjacentNodes*.adjacentNodes.flatten(), hasSize(1)
        assertThat graph.adjacentNodes*.adjacentNodes*.label.flatten(),
                contains(instanceOf(JobGraph.NoopNode))
        assertThat graph.adjacentNodes*.adjacentNodes*.adjacentNodes*.label.flatten(),
                   contains(instanceOf(JobGraph.JobNode))
    }

    @Test
    void testAddLenskitJob() {
        def ab = new AlgorithmInstanceBuilder()
        ConfigHelpers.configure(ab.config) {
            bind ItemScorer to UserMeanItemScorer
        }
        ab.name = 'foo'
        def algo = ab.build()
        def g = algo.buildRecommenderGraph(dataConfig)
        builder.addLenskitJob(algo, dataSet, g)
        def graph = builder.getGraph()
        assertThat graph, notNullValue()
        assertThat graph.reachableNodes, hasSize(2)
        def jobs = graph.reachableNodes*.label.grep({it instanceof JobGraph.JobNode})*.job
        def algos = jobs*.recommenderGraph
        assertThat algos, contains(g)
    }

    @Test
    void testAddTwoLenskitJobs() {
        def ab1 = new AlgorithmInstanceBuilder()
        ConfigHelpers.configure(ab1.config) {
            bind ItemScorer to UserMeanItemScorer
        }
        ab1.name = 'foo'
        def algo1 = ab1.build()

        def ab2 = new AlgorithmInstanceBuilder()
        ConfigHelpers.configure(ab2.config) {
            bind ItemScorer to ItemMeanRatingItemScorer
        }
        ab2.name = 'bar'
        def algo2 = ab2.build()

        def g1 = algo1.buildRecommenderGraph(dataConfig)
        def g2 = algo2.buildRecommenderGraph(dataConfig)

        builder.addLenskitJob(algo1, dataSet, g1)
        builder.addLenskitJob(algo2, dataSet, g2)
        def graph = builder.getGraph()
        assertThat graph, notNullValue()
        assertThat graph.reachableNodes, hasSize(3)
        def jobs = graph.reachableNodes*.label.grep({it instanceof JobGraph.JobNode})*.job
        def algos = jobs*.recommenderGraph
        assertThat algos, containsInAnyOrder(g1, g2)

        // no edges on job nodes
        assertThat graph.adjacentNodes*.adjacentNodes.flatten(), hasSize(0)

        // no cached nodes
        assertThat cache.cache.keySet(), hasSize(0)
    }

    @Test
    void testAddMergedLenskitJobs() {
        def pool = MergePool.<Component,Dependency>create()
        def ab1 = new AlgorithmInstanceBuilder()
        ConfigHelpers.configure(ab1.config) {
            bind ItemScorer to ItemMeanRatingItemScorer
        }
        ab1.name = 'foo'
        def algo1 = ab1.build()

        def ab2 = new AlgorithmInstanceBuilder()
        ConfigHelpers.configure(ab2.config) {
            bind ItemScorer to UserMeanItemScorer
            bind (UserMeanBaseline, ItemScorer) to ItemMeanRatingItemScorer
        }
        ab2.name = 'bar'
        def algo2 = ab2.build()

        def g1 = pool.merge(algo1.buildRecommenderGraph(dataConfig))
        def g2 = pool.merge(algo2.buildRecommenderGraph(dataConfig))
        def commonNodes = g1.reachableNodes.intersect(g2.reachableNodes)
        assertThat commonNodes, hasSize(greaterThan(0))
        // this test is really on merge pool capabilities, but include here
        assertThat commonNodes*.label*.satisfaction*.erasedType, hasItem(ItemMeanRatingItemScorer)

        builder.addLenskitJob(algo1, dataSet, g1)
        builder.addLenskitJob(algo2, dataSet, g2)
        def graph = builder.getGraph()
        assertThat graph, notNullValue()
        assertThat graph.reachableNodes, hasSize(3)
        def jobs = graph.reachableNodes*.label.grep({it instanceof JobGraph.JobNode})*.job
        def algos = jobs*.recommenderGraph
        assertThat algos, containsInAnyOrder(g1, g2)

        // algo 2 depends on algo1
        assertThat graph.adjacentNodes, hasSize(2)

        def job1 = graph.adjacentNodes.find { it.label.job.recommenderGraph == g1 }
        def job2 = graph.adjacentNodes.find { it.label.job.recommenderGraph == g2 }
        assertThat job1, notNullValue()
        assertThat job1.adjacentNodes, hasSize(0)

        assertThat job2, notNullValue()
        assertThat job2.adjacentNodes, contains(job1)
        def cnMatches = commonNodes.toList().collect({equalTo(it)})
        // assertThat job2.outgoingEdges*.label.flatten(), containsInAnyOrder(cnMatches)
        for (node in commonNodes) {
            if (GraphtUtils.isShareable(node)) {
                assertThat cache.cache, hasKey(node)
            }
        }
        assertThat cache.cache.keySet()*.label*.satisfaction*.erasedType, hasItem(ItemMeanRatingItemScorer)
    }
}
