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
package org.grouplens.lenskit.core;

import com.sun.org.apache.bcel.internal.generic.LREM;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.InstanceSatisfaction;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.baseline.BaselineItemScorer;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ConstantPredictor;
import org.grouplens.lenskit.baseline.GlobalMeanPredictor;
import org.grouplens.lenskit.basic.SimpleRatingPredictor;
import org.grouplens.lenskit.basic.TopNItemRecommender;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.iterative.StoppingThreshold;
import org.grouplens.lenskit.iterative.ThresholdStoppingCondition;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitRecommenderEngineTest {
    private DAOFactory daoFactory;

    @Before
    public void setup() {
        daoFactory = new EventCollectionDAO.Factory(Collections.<Event>emptyList());
    }

    @Test
    public void testBasicRec() throws RecommenderBuildException {
        LenskitConfiguration config = configureBasicRecommender();

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(daoFactory, config);
        verifyBasicRecommender(engine);
    }

    private LenskitConfiguration configureBasicRecommender() {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class)
              .to(BaselineItemScorer.class);
        config.bind(ItemRecommender.class)
              .to(TopNItemRecommender.class);
        config.bind(BaselinePredictor.class)
              .to(ConstantPredictor.class);
        return config;
    }

    private void verifyBasicRecommender(LenskitRecommenderEngine engine) {LenskitRecommender rec = engine.open();
        try {
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
            assertThat(rec.getItemScorer(),
                       instanceOf(BaselineItemScorer.class));
            assertThat(rec.getRatingPredictor(),
                       instanceOf(SimpleRatingPredictor.class));
            assertThat(rec.get(BaselinePredictor.class),
                       instanceOf(ConstantPredictor.class));
            // Since we have an item scorer, we should have a recommender too
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
        } finally {
            rec.close();
        }
    }

    @Test
    public void testArbitraryRoot() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(BaselinePredictor.class)
              .to(ConstantPredictor.class);
        config.addRoot(BaselinePredictor.class);

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(daoFactory, config);
        LenskitRecommender rec = engine.open();
        try {
            assertThat(rec.get(BaselinePredictor.class),
                       instanceOf(ConstantPredictor.class));
        } finally {
            rec.close();
        }
    }

    @Test
    public void testSeparatePredictor() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(BaselinePredictor.class)
              .to(GlobalMeanPredictor.class);
        config.bind(ItemScorer.class)
              .to(BaselineItemScorer.class);

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(daoFactory, config);

        LenskitRecommender rec1 = engine.open();
        LenskitRecommender rec2 = engine.open();
        try {
            assertThat(rec1.getItemScorer(),
                       instanceOf(BaselineItemScorer.class));
            assertThat(rec2.getItemScorer(),
                       instanceOf(BaselineItemScorer.class));

            // verify that recommenders have different scorers
            assertThat(rec1.getItemScorer(),
                       not(sameInstance(rec2.getItemScorer())));

            // verify that recommenders have different rating predictors
            assertThat(rec1.getRatingPredictor(),
                       not(sameInstance(rec2.getRatingPredictor())));

            // verify that recommenders have same baseline
            assertThat(rec1.get(BaselinePredictor.class),
                       sameInstance(rec2.get(BaselinePredictor.class)));
        } finally {
            rec1.close();
            rec2.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testParameter() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.set(StoppingThreshold.class).to(0.042);
        config.addRoot(ThresholdStoppingCondition.class);
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(daoFactory, config);
        LenskitRecommender rec = engine.open();
        ThresholdStoppingCondition stop = rec.get(ThresholdStoppingCondition.class);
        assertThat(stop, notNullValue());
        assertThat(stop.getThreshold(),
                   closeTo(0.042, 1.0e-6));
    }

    @SuppressWarnings({"rawtypes"})
    private void assertNodeNotEVDao(Node node) {
        CachedSatisfaction lbl = node.getLabel();
        if (lbl == null) {
            return;
        }
        Satisfaction sat = lbl.getSatisfaction();
        if (sat instanceof InstanceSatisfaction) {
            assertThat((Class) sat.getErasedType(),
                       not(equalTo((Class) EventCollectionDAO.class)));
        }
    }

    /**
     * Test that no instance satisfaction contains an event collection DAO reference.
     */
    @Test
    public void testBasicNoInstance() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender();

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(daoFactory, config);

        Graph g = engine.getDependencies();
        // make sure we have no record of an instance dao
        for (Node n: g.getNodes()) {
            assertNodeNotEVDao(n);
            for (Edge e: g.getOutgoingEdges(n)) {
                assertNodeNotEVDao(e.getTail());
            }
            for (Edge e: g.getIncomingEdges(n)) {
                assertNodeNotEVDao(e.getHead());
            }
        }
    }

    @Test
    public void testSerialize() throws RecommenderBuildException, IOException, ClassNotFoundException {
        LenskitConfiguration config = configureBasicRecommender();

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(daoFactory, config);
        File tfile = File.createTempFile("lenskit", "engine");
        try {
            engine.write(tfile);
            LenskitRecommenderEngine e2 = LenskitRecommenderEngine.load(daoFactory, tfile);
            verifyBasicRecommender(e2);
        } finally {
            tfile.delete();
        }
    }

    /**
     * Verify that we can inject subclassed DAOs.
     */
    @Test
    public void testSubclassedDAO() throws RecommenderBuildException {
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(SubclassedDAODepComponent.class);
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(daoFactory, config);
        LenskitRecommender rec = engine.open();
        try {
            SubclassedDAODepComponent dep = rec.get(SubclassedDAODepComponent.class);
            assertThat(dep, notNullValue());
            assertThat(dep.dao, notNullValue());
        } finally {
            rec.close();
        }
    }

    public static class SubclassedDAODepComponent {
        private final EventCollectionDAO dao;

        @Inject
        public SubclassedDAODepComponent(EventCollectionDAO dao) {
            this.dao = dao;
        }
    }
}
