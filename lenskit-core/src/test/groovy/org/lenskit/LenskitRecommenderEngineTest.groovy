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
package org.lenskit

import org.grouplens.grapht.Component
import org.grouplens.grapht.Dependency
import org.grouplens.grapht.graph.DAGNode
import org.grouplens.grapht.reflect.Satisfaction
import org.grouplens.grapht.reflect.internal.InstanceSatisfaction
import org.grouplens.lenskit.transform.threshold.RealThreshold
import org.grouplens.lenskit.transform.threshold.ThresholdValue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.api.RecommenderBuildException
import org.lenskit.baseline.*
import org.lenskit.basic.*
import org.lenskit.data.dao.DataAccessObject
import org.lenskit.data.dao.EntityCollectionDAO
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.data.ratings.RatingMatrix
import org.lenskit.inject.Shareable
import org.lenskit.transform.normalize.MeanVarianceNormalizer
import org.lenskit.transform.normalize.VectorNormalizer
import org.lenskit.util.io.CompressionMode

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
    private StaticDataSource source
    private DataAccessObject dao

    @Before
    public void setup() {
        source = new StaticDataSource()
        source.addSource(Collections.emptyList())
        dao = source.get()
    }

    @Test
    public void testBasicRec() throws RecommenderBuildException {
        LenskitConfiguration config = configureBasicRecommender()

        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            verifyBasicRecommender(rec)
        } finally {
            rec.close()
        }
    }

    @Test
    public void testGetComponentFromEngine() throws RecommenderBuildException {
        LenskitConfiguration config = configureBasicRecommender()

        def engine = LenskitRecommenderEngine.build(config, dao)
        assertThat(engine.getComponent(ItemScorer.class),
                   notNullValue());
        assertThat(engine.getComponent(ItemScorer.class),
                   instanceOf(ConstantItemScorer.class));
    }

    @Test
    public void testBasicNoEngine() throws RecommenderBuildException {
        LenskitConfiguration config = configureBasicRecommender()

        def rec = LenskitRecommender.build(config, dao)
        try {
            verifyBasicRecommender(rec)
        } finally {
            rec.close()
        }
    }

    private static LenskitConfiguration configureBasicRecommender() {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(ItemScorer.class)
              .to(ConstantItemScorer.class)
        config.bind(ItemRecommender.class)
              .to(TopNItemRecommender.class)
        return config
    }

    private static void verifyBasicRecommender(LenskitRecommender rec) {
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
        LenskitConfiguration config = configureBasicRecommender()
        config.addComponent(dao)

        def engine = LenskitRecommenderEngine.build(config)
        def rec = engine.createRecommender()
        try {
            verifyBasicRecommender(rec)
        } finally {
            rec.close()
        }
    }

    @Test
    public void testAddComponentClass() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.addComponent(ConstantItemScorer.class)

        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            verifyBasicRecommender(rec)
        } finally {
            rec.close()
        }
    }

    @Test
    public void testArbitraryRoot() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(VectorNormalizer.class)
              .to(MeanVarianceNormalizer.class)
        config.addRoot(VectorNormalizer.class)

        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.get(VectorNormalizer.class),
                       instanceOf(MeanVarianceNormalizer.class))
        } finally {
            rec.close()
        }
    }

    @Test
    public void testSeparatePredictor() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(GlobalMeanRatingItemScorer.class)
        config.bind(ItemScorer.class)
              .to(UserMeanItemScorer.class)

        def engine = LenskitRecommenderEngine.build(config, dao)

        LenskitRecommender rec1 = null
        LenskitRecommender rec2 = null
        try {
            rec1 = engine.createRecommender(dao)
            rec2 = engine.createRecommender(dao)

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
        } finally {
            rec1?.close()
            rec2?.close()
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testParameter() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.set(ThresholdValue.class).to(0.042)
        config.addRoot(RealThreshold.class)
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, dao)
        LenskitRecommender rec = engine.createRecommender(dao)
        try {
            RealThreshold thresh = rec.get(RealThreshold.class)
            assertThat(thresh, notNullValue())
            assertThat(thresh.getValue(),
                       closeTo(0.042d, 1.0e-6d))
        } finally {
            rec.close()
        }
    }

    private static void assertNodeNotDAO(DAGNode<Component,Dependency> node) {
        def lbl = node.getLabel()
        if (lbl == null) {
            return
        }
        Satisfaction sat = lbl.getSatisfaction()
        if (sat instanceof InstanceSatisfaction) {
            assertThat((Class) sat.getErasedType(),
                       not(equalTo((Class) DataAccessObject.class)))
        }
    }

    /**
     * Test that we can configure data separately.
     */
    @Test
    @SuppressWarnings(["deprecation", "GrDeprecatedAPIUsage"])
    public void testSeparateBuild() throws RecommenderBuildException {
        LenskitRecommenderEngineBuilder reb = LenskitRecommenderEngine.newBuilder()
        reb.addConfiguration(configureBasicRecommender())
        LenskitConfiguration daoConfig = new LenskitConfiguration()
        daoConfig.addComponent(dao)
        reb.addConfiguration(daoConfig)
        LenskitRecommenderEngine engine = reb.build()
        LenskitRecommender rec = engine.createRecommender()
        try {
            verifyBasicRecommender(rec)
        } finally {
            rec.close()
        }
    }

    /**
     * Test that we can configure data separately and remove it
     */
    @Test
    public void testSeparateRemovableBuild() throws RecommenderBuildException {
        LenskitRecommenderEngineBuilder reb = LenskitRecommenderEngine.newBuilder()
        reb.addConfiguration(configureBasicRecommender())
        LenskitConfiguration daoConfig = new LenskitConfiguration()
        daoConfig.addComponent(dao)
        reb.addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
        LenskitRecommenderEngine engine = reb.build()
        LenskitRecommender rec = engine.createRecommender(dao)
        try {
            verifyBasicRecommender(rec)
        } finally {
            rec.close()
        }
    }

    /**
     * Test that no instance satisfaction contains an event collection DAO reference.
     */
    @Test
    public void testBasicNoInstance() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender()

        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .build(dao)

        def g = engine.graph
        // make sure we have no record of an instance dao
        for (n in g.reachableNodes) {
            assertNodeNotDAO(n)
        }
    }

    @Test
    public void testSerialize() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender()

        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .build(dao)

        // engine.setSymbolMapping(null)
        File tfile = File.createTempFile("lenskit", "engine")
        try {
            engine.write(tfile)
            def e2 = LenskitRecommenderEngine.newLoader()
                                             .load(tfile)
            // e2.setSymbolMapping(mapping)
            def rec = e2.createRecommender(dao)
            try {
                verifyBasicRecommender(rec)
            } finally {
                rec.close()
            }
        } finally {
            tfile.delete()
        }
    }

    @Test
    public void testSerializeAddConfig() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender()

        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .build(dao)

        def daoConfig = new LenskitConfiguration()
        daoConfig.addComponent(dao)

        // engine.setSymbolMapping(null)
        File tfile = File.createTempFile("lenskit", "engine")
        try {
            engine.write(tfile)
            def e2 = LenskitRecommenderEngine.newLoader()
                                             .addConfiguration(daoConfig)
                                             .load(tfile)
            // e2.setSymbolMapping(mapping)
            def rec = e2.createRecommender()
            try {
                verifyBasicRecommender(rec)
            } finally {
                rec.close()
            }
        } finally {
            tfile.delete()
        }
    }

    @Test
    public void testSerializeCompressed() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender()

        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .build(dao)

        // engine.setSymbolMapping(null)
        File tfile = File.createTempFile("lenskit", "engine.gz")
        try {
            engine.write(tfile, CompressionMode.GZIP)
            def e2 = LenskitRecommenderEngine.newLoader()
                                             .load(tfile)
            // e2.setSymbolMapping(mapping)
            def rec = e2.createRecommender(dao)
            try {
                verifyBasicRecommender(rec)
            } finally {
                rec.close()
            }
        } finally {
            tfile.delete()
        }
    }

    @Test
    public void testDeserializeValidate() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender()
        LenskitConfiguration other = new LenskitConfiguration()
        other.bind(ItemRecommender).to(TopNItemRecommender)

        LenskitRecommenderEngine engine =
                LenskitRecommenderEngine.newBuilder()
                                        .addConfiguration(config)
                                        .addConfiguration(config, ModelDisposition.EXCLUDED)
                                        .build(dao)

        // engine.setSymbolMapping(null)
        File tfile = File.createTempFile("lenskit", "engine")
        try {
            engine.write(tfile)
            shouldFail(RecommenderConfigurationException) {
                LenskitRecommenderEngine.newLoader().load(tfile)
            }
        } finally {
            tfile.delete()
        }
    }

    @Test
    @Ignore("broken for unknown reasons")
    public void testDeserializeDeferredValidate() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender()

        LenskitRecommenderEngine engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .build(dao)

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
        config.bind(ItemScorer.class)
              .to(FallbackItemScorer.class)
        config.bind(PrimaryScorer.class, ItemScorer.class)
                .to(PrecomputedItemScorer.newBuilder()
                                  .addScore(42, 15, 3.5)
                                  .build())
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(FallbackItemScorer.class)
        config.within(BaselineScorer.class, FallbackItemScorer.class)
              .bind(PrimaryScorer.class, ItemScorer.class)
              .to(PrecomputedItemScorer.newBuilder()
                                .addScore(38, 10, 4.0)
                                .build())
        config.within(BaselineScorer.class, FallbackItemScorer.class)
              .bind(BaselineScorer.class, ItemScorer.class)
              .to(new ConstantItemScorer(3.0))

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, dao)
        LenskitRecommender rec = engine.createRecommender(dao)
        try {
            ItemScorer scorer = rec.getItemScorer()
            assertThat(scorer, notNullValue())
            assert scorer != null
            // first scorer
            assertThat(scorer.score(42, 15).score, equalTo(3.5d))
            // first fallback
            assertThat(scorer.score(38, 10).score, equalTo(4.0d))
            // second fallback
            assertThat(scorer.score(42, 10).score, equalTo(3.0d))
        } finally {
            rec.close()
        }
    }

    /**
     * Verify that we can inject subclassed DAOs.
     */
    @Test
    public void testSubclassedDAO() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.addRoot(SubclassedDAODepComponent.class)
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, dao)
        LenskitRecommender rec = engine.createRecommender(dao)
        try {
            SubclassedDAODepComponent dep = rec.get(SubclassedDAODepComponent.class)
            assertThat(dep, notNullValue())
            assertThat(dep.dao, notNullValue())
        } finally {
            rec.close()
        }
    }

    public static class SubclassedDAODepComponent {
        private final EntityCollectionDAO dao

        @Inject
        public SubclassedDAODepComponent(EntityCollectionDAO dao) {
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
        config.bind(ItemScorer.class)
              .to(ConstantItemScorer.class)
        config.set(ConstantItemScorer.Value.class)
              .to(3.5)
        config.at(null)
              .bind(ItemScorer.class)
              .to(FallbackItemScorer.class)
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(GlobalMeanRatingItemScorer.class)
        LenskitRecommender rec = LenskitRecommender.build(config, dao)
        try {
            assertThat(rec.getItemScorer(), instanceOf(FallbackItemScorer.class))
            def rp = (SimpleRatingPredictor) rec.getRatingPredictor()
            assertThat(rp, notNullValue())
            assert rp != null
            assertThat(rp.itemScorer, instanceOf(ConstantItemScorer.class))
            assertThat(((FallbackItemScorer) rec.getItemScorer()).getPrimaryScorer(),
                       sameInstance(rp.itemScorer))
        } finally {
            rec.close()
        }
    }

    /**
     * Test that recommender engines verify that they are instantiable.
     */
    @Test
    @Ignore("broken for unknown reasons")
    public void testEngineChecksInstantiable() {
        def config = configureBasicRecommender()
        def engine = LenskitRecommenderEngine.newBuilder()
                                             .addConfiguration(config)
                                             .build(dao)
        shouldFail(IllegalStateException) {
            engine.createRecommender(dao)
        }
    }

    //region Test shareable providers
    @Test
    public void testShareableProvider() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration()
        config.addRoot(RootComp.class)
        config.bind(ByteBuffer.class)
              .toProvider(BufferProvider.class)
        config.bind(InputStream.class)
              .toProvider(StreamProvider.class)
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, dao)

        LenskitRecommender rec1 = null
        LenskitRecommender rec2 = null
        try {
            rec1 = engine.createRecommender(dao)
            rec2 = engine.createRecommender(dao)

            assertThat(rec2, not(sameInstance(rec1)))

            RootComp r1 = rec1.get(RootComp.class)
            RootComp r2 = rec2.get(RootComp.class)
            // byte buffers are shared
            assertThat(r2.getBuffer(), sameInstance(r1.getBuffer()))
            // streams are not
            assertThat(r2.getStream(), not(sameInstance(r1.getStream())))
        } finally {
            rec1?.close()
            rec2?.close()
        }
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
        LenskitRecommender rec = LenskitRecommender.build(config, dao)
        try {
            assertThat rec.getItemScorer(), instanceOf(LeastSquaresItemScorer)
            assertThat rec.get(RatingMatrix), nullValue()
        } finally {
            rec.close()
        }
    }
}
