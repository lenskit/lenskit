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
package org.grouplens.lenskit.core

import org.grouplens.grapht.graph.DAGNode
import org.grouplens.grapht.reflect.CachedSatisfaction
import org.grouplens.grapht.reflect.Satisfaction
import org.grouplens.grapht.reflect.internal.InstanceSatisfaction
import org.grouplens.grapht.solver.DesireChain
import org.grouplens.lenskit.ItemRecommender
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.RecommenderBuildException
import org.grouplens.lenskit.baseline.*
import org.grouplens.lenskit.basic.SimpleRatingPredictor
import org.grouplens.lenskit.basic.TopNItemRecommender
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.data.event.Event
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot
import org.grouplens.lenskit.iterative.StoppingThreshold
import org.grouplens.lenskit.iterative.ThresholdStoppingCondition
import org.grouplens.lenskit.transform.normalize.MeanVarianceNormalizer
import org.grouplens.lenskit.transform.normalize.VectorNormalizer
import org.grouplens.lenskit.util.test.MockItemScorer
import org.junit.Before
import org.junit.Test

import javax.inject.Inject
import javax.inject.Provider
import java.nio.ByteBuffer

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitRecommenderEngineTest {
    private EventDAO dao

    @Before
    public void setup() {
        dao = new EventCollectionDAO(Collections.<Event>emptyList())
    }

    @Test
    public void testBasicRec() throws RecommenderBuildException {
        LenskitConfiguration config = configureBasicRecommender(true)

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)
        verifyBasicRecommender(engine.createRecommender())
    }

    @Test
    public void testBasicNoEngine() throws RecommenderBuildException {
        LenskitConfiguration config = configureBasicRecommender(true)

        LenskitRecommender rec = LenskitRecommender.build(config)
        verifyBasicRecommender(rec)
    }

    private LenskitConfiguration configureBasicRecommender(boolean includeData) {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(ItemScorer.class)
              .to(ConstantItemScorer.class)
        config.bind(ItemRecommender.class)
              .to(TopNItemRecommender.class)
        if (includeData) {
            makeDAOConfig(config)
        }
        return config
    }

    private LenskitConfiguration makeDAOConfig(LenskitConfiguration config) {
        if (config == null) {
            config = new LenskitConfiguration()
        }
        config.bind(EventDAO.class)
              .to(dao)
        return config
    }

    private void verifyBasicRecommender(LenskitRecommender rec) {
        assertThat(rec.getItemRecommender(),
                   instanceOf(TopNItemRecommender.class))
        assertThat(rec.getItemScorer(),
                   instanceOf(ConstantItemScorer.class))
        assertThat(rec.getRatingPredictor(),
                   instanceOf(SimpleRatingPredictor.class))
        // Since we have an item scorer, we should have a recommender too
        assertThat(rec.getItemRecommender(),
                   instanceOf(TopNItemRecommender.class))
    }

    @Test
    public void testAddComponentInstance() throws RecommenderBuildException {
        LenskitConfiguration config = configureBasicRecommender(false)
        config.addComponent(dao)

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)
        verifyBasicRecommender(engine.createRecommender())
    }

    @Test
    public void testAddComponentClass() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.addComponent(ConstantItemScorer.class)
        makeDAOConfig(config)

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)
        verifyBasicRecommender(engine.createRecommender())
    }

    @Test
    public void testArbitraryRoot() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(EventDAO.class).to(dao)
        config.bind(VectorNormalizer.class)
              .to(MeanVarianceNormalizer.class)
        config.addRoot(VectorNormalizer.class)

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)
        LenskitRecommender rec = engine.createRecommender()
        assertThat(rec.get(VectorNormalizer.class),
                   instanceOf(MeanVarianceNormalizer.class))
    }

    @Test
    public void testSeparatePredictor() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(EventDAO.class).to(dao)
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(GlobalMeanRatingItemScorer.class)
        config.bind(ItemScorer.class)
              .to(UserMeanItemScorer.class)

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)

        LenskitRecommender rec1 = engine.createRecommender()
        LenskitRecommender rec2 = engine.createRecommender()
        assertThat(rec1.getItemScorer(),
                   instanceOf(UserMeanItemScorer.class))
        assertThat(rec2.getItemScorer(),
                   instanceOf(UserMeanItemScorer.class))

        // verify that recommenders have different scorers
        assertThat(rec1.getItemScorer(),
                   not(sameInstance(rec2.getItemScorer())))

        // verify that recommenders have different rating predictors
        assertThat(rec1.getRatingPredictor(),
                   not(sameInstance(rec2.getRatingPredictor())))

        // verify that recommenders have same baseline
        assertThat(rec1.get(UserMeanBaseline.class, ItemScorer.class),
                   sameInstance(rec2.get(UserMeanBaseline.class, ItemScorer.class)))
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testParameter() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        // FIXME This DAO binding should not be required
        config.bind(EventDAO.class).to(dao)
        config.set(StoppingThreshold.class).to(0.042)
        config.addRoot(ThresholdStoppingCondition.class)
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)
        LenskitRecommender rec = engine.createRecommender()
        ThresholdStoppingCondition stop = rec.get(ThresholdStoppingCondition.class)
        assertThat(stop, notNullValue())
        assertThat(stop.getThreshold(),
                   closeTo(0.042d, 1.0e-6d))
    }

    private void assertNodeNotEVDao(DAGNode<CachedSatisfaction,DesireChain> node) {
        CachedSatisfaction lbl = node.getLabel()
        if (lbl == null) {
            return
        }
        Satisfaction sat = lbl.getSatisfaction()
        if (sat instanceof InstanceSatisfaction) {
            assertThat((Class) sat.getErasedType(),
                       not(equalTo((Class) EventCollectionDAO.class)))
        }
    }

    /**
     * Test that we can configure data separately.
     */
    @Test
    public void testSeparateBuild() throws RecommenderBuildException {
        LenskitRecommenderEngineBuilder reb = LenskitRecommenderEngine.newBuilder()
        reb.addConfiguration(configureBasicRecommender(false))
        LenskitConfiguration daoConfig = new LenskitConfiguration()
        daoConfig.bind(EventDAO.class).to(dao)
        reb.addConfiguration(daoConfig)
        LenskitRecommenderEngine engine = reb.build()
        LenskitRecommender rec = engine.createRecommender()
        verifyBasicRecommender(rec)
    }

    /**
     * Test that no instance satisfaction contains an event collection DAO reference.
     */
    @Test
    public void testBasicNoInstance() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender(false)
        LenskitConfiguration daoConfig = makeDAOConfig(null)

        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                             .build()

        DAGNode<CachedSatisfaction,DesireChain> g = engine.getGraph()
        // make sure we have no record of an instance dao
        for (DAGNode<CachedSatisfaction,DesireChain> n: g.getReachableNodes()) {
            assertNodeNotEVDao(n)
        }
    }

    @Test
    public void testSerialize() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender(false)
        LenskitConfiguration daoConfig = makeDAOConfig(null)

        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                             .build()

        // engine.setSymbolMapping(null)
        File tfile = File.createTempFile("lenskit", "engine")
        try {
            engine.write(tfile)
            def e2 = LenskitRecommenderEngine.newLoader()
                                             .addConfiguration(daoConfig)
                                             .load(tfile)
            // e2.setSymbolMapping(mapping)
            verifyBasicRecommender(e2.createRecommender())
        } finally {
            tfile.delete()
        }
    }

    @Test
    public void testDeserializeValidate() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender(false)
        LenskitConfiguration daoConfig = makeDAOConfig(null)

        LenskitRecommenderEngine engine =
                LenskitRecommenderEngine.newBuilder()
                                        .addConfiguration(config)
                                        .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                        .build()

        // engine.setSymbolMapping(null)
        File tfile = File.createTempFile("lenskit", "engine")
        try {
            engine.write(tfile)
            shouldFail(RecommenderConfigurationException) {
                def e2 = LenskitRecommenderEngine.newLoader().load(tfile)
            }
        } finally {
            tfile.delete()
        }
    }

    @Test
    public void testDeserializeDeferredValidate() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender(false)
        LenskitConfiguration daoConfig = makeDAOConfig(null)

        LenskitRecommenderEngine engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                    .build()

        // engine.setSymbolMapping(null)
        File tfile = File.createTempFile("lenskit", "engine")
        try {
            engine.write(tfile)
            // loading should succeed
            def e2 = LenskitRecommenderEngine.newLoader()
                                             .setValidationMode(EngineValidationMode.DEFERRED)
                                             .load(tfile)
            shouldFail(IllegalStateException) {
                // creating the recommender should fail
                e2.createRecommender()
            }
        } finally {
            tfile.delete()
        }
    }

    @Test
    public void testContextDep() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(EventDAO.class)
              .to(dao)
        config.bind(ItemScorer.class)
              .to(FallbackItemScorer.class)
        config.bind(PrimaryScorer.class, ItemScorer.class)
                .to(MockItemScorer.newBuilder()
                                  .addScore(42, 15, 3.5)
                                  .build())
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(FallbackItemScorer.class)
        config.within(BaselineScorer.class, FallbackItemScorer.class)
              .bind(PrimaryScorer.class, ItemScorer.class)
              .to(MockItemScorer.newBuilder()
                                .addScore(38, 10, 4.0)
                                .build())
        config.within(BaselineScorer.class, FallbackItemScorer.class)
              .bind(BaselineScorer.class, ItemScorer.class)
              .to(new ConstantItemScorer(3.0))

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)
        LenskitRecommender rec = engine.createRecommender()
        ItemScorer scorer = rec.getItemScorer()
        assertThat(scorer, notNullValue())
        assert scorer != null
        // first scorer
        assertThat(scorer.score(42, 15), equalTo(3.5d))
        // first fallback
        assertThat(scorer.score(38, 10), equalTo(4.0d))
        // second fallback
        assertThat(scorer.score(42, 10), equalTo(3.0d))
    }

    /**
     * Verify that we can inject subclassed DAOs.
     */
    @Test
    public void testSubclassedDAO() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(EventDAO.class).to(dao)
        config.addRoot(SubclassedDAODepComponent.class)
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)
        LenskitRecommender rec = engine.createRecommender()
        SubclassedDAODepComponent dep = rec.get(SubclassedDAODepComponent.class)
        assertThat(dep, notNullValue())
        assertThat(dep.dao, notNullValue())
    }

    public static class SubclassedDAODepComponent {
        private final EventCollectionDAO dao

        @Inject
        public SubclassedDAODepComponent(EventCollectionDAO dao) {
            this.dao = dao
        }
    }

    /**
     * Test anchoring to the root (#344).
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAnchoredRoot() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(EventDAO.class).to(dao)
        config.bind(ItemScorer.class)
              .to(ConstantItemScorer.class)
        config.set(ConstantItemScorer.Value.class)
              .to(3.5)
        config.at(null)
              .bind(ItemScorer.class)
              .to(FallbackItemScorer.class)
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(GlobalMeanRatingItemScorer.class)
        LenskitRecommender rec = LenskitRecommender.build(config)
        assertThat(rec.getItemScorer(), instanceOf(FallbackItemScorer.class))
        SimpleRatingPredictor rp = (SimpleRatingPredictor) rec.getRatingPredictor()
        assertThat(rp, notNullValue())
        assert rp != null
        assertThat(rp.getScorer(), instanceOf(ConstantItemScorer.class))
        assertThat(((FallbackItemScorer) rec.getItemScorer()).getPrimaryScorer(),
                   sameInstance(rp.getScorer()))
    }

    /**
     * Test that recommender engines verify that they are instantiable.
     */
    @Test
    public void testEngineChecksInstantiable() {
        def config = configureBasicRecommender(false)
        def daoConfig = new LenskitConfiguration()
        makeDAOConfig(daoConfig)
        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                             .build()
        shouldFail(IllegalStateException) {
            engine.createRecommender()
        }
    }

    @Test
    public void testEngineRewriting() {
        def config = configureBasicRecommender(false)
        def daoConfig = new LenskitConfiguration()
        makeDAOConfig(daoConfig)
        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                             .build()
        def rec = engine.createRecommender(daoConfig)
        verifyBasicRecommender(rec)
    }

    //region Test shareable providers
    @Test
    public void testShareableProvider() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(EventDAO.class)
              .to(dao)
        config.addRoot(RootComp.class)
        config.bind(ByteBuffer.class)
              .toProvider(BufferProvider.class)
        config.bind(InputStream.class)
              .toProvider(StreamProvider.class)
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config)

        LenskitRecommender rec1 = engine.createRecommender()
        LenskitRecommender rec2 = engine.createRecommender()
        assertThat(rec2, not(sameInstance(rec1)))

        RootComp r1 = rec1.get(RootComp.class)
        RootComp r2 = rec2.get(RootComp.class)
        // byte buffers are shared
        assertThat(r2.getBuffer(), sameInstance(r1.getBuffer()))
        // streams are not
        assertThat(r2.getStream(), not(sameInstance(r1.getStream())))
    }

    public static class RootComp {
        private final ByteBuffer buf
        private final InputStream stream

        @Inject
        public RootComp(ByteBuffer b, InputStream s) {
            buf = b
            stream = s
        }

        public ByteBuffer getBuffer() {
            return buf
        }

        public InputStream getStream() {
            return stream
        }
    }

    public static class BufferProvider implements Provider<ByteBuffer> {
        @Override
        @Shareable
        public ByteBuffer get() {
            return ByteBuffer.allocate(32)
        }
    }

    public static class StreamProvider implements Provider<InputStream> {
        @Override
        public InputStream get() {
            return new ByteArrayInputStream([0, 3, 2] as byte[])
        }
    }
    //endregion

    @Test
    public void testRemoveShareableSnapshot() {
        def config = new LenskitConfiguration();
        config.bind(ItemScorer).to(LeastSquaresItemScorer)
        config.bind(EventDAO).to(dao)
        LenskitRecommender rec = LenskitRecommender.build(config)
        assertThat rec.getItemScorer(), instanceOf(LeastSquaresItemScorer)
        assertThat rec.get(PreferenceSnapshot), nullValue()
    }
}
