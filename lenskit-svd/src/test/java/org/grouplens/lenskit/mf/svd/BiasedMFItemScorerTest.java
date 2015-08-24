package org.grouplens.lenskit.mf.svd;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.grouplens.lenskit.indexes.MutableIdIndexMapping;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.basic.PrecomputedItemScorer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BiasedMFItemScorerTest {
    private ItemScorer baseline;
    private MFModel model;
    private BiasedMFItemScorer scorer;

    @Before
    public void createModel() {
        baseline = PrecomputedItemScorer.newBuilder()
                                        .addScore(1, 42, 3.0)
                                        .addScore(1, 39, 2.5)
                                        .addScore(1, 25, 4.2)
                                        .addScore(5, 42, 3.7)
                                        .addScore(5, 39, 2.8)
                                        .addScore(3, 42, 2.2)
                                        .addScore(3, 39, 3.2)
                                        .addScore(17, 42, 2.5)
                                        .build();

        RealMatrix umat = MatrixUtils.createRealMatrix(3, 2);
        umat.setRow(0, new double[]{0.1, 0.3});
        umat.setRow(1, new double[]{-0.2, 0.2});
        umat.setRow(2, new double[]{0.0, 0.15});
        MutableIdIndexMapping uidx = new MutableIdIndexMapping();
        uidx.internId(1);
        uidx.internId(5);
        uidx.internId(3);

        RealMatrix imat = MatrixUtils.createRealMatrix(2, 2);
        imat.setRow(0, new double[]{0.52, 0.29});
        imat.setRow(1, new double[]{0.3, -1.2});
        MutableIdIndexMapping iidx = new MutableIdIndexMapping();
        iidx.internId(42);
        iidx.internId(39);

        model = new MFModel(umat, imat, uidx, iidx);

        scorer = new BiasedMFItemScorer(model, new DotProductKernel(), baseline);
    }

    @Test
    public void testGoodRecs() {
        Result score = scorer.score(1, 42);
        assertThat(score, notNullValue());
        assertThat(score.getScore(),
                   closeTo(3.0 + 0.1*0.52 + 0.3*0.29, 1.0e-6));

        score = scorer.score(3, 39);
        assertThat(score, notNullValue());
        assertThat(score.getScore(),
                   closeTo(3.2 - 0.15 * 1.2, 1.0e-6));
    }

    @Test
    public void testSkipUnscorable() {
        Result score = scorer.score(1, 25);
        assertThat(score, nullValue());
    }

    @Test
    public void baselineForUser() {
        Result score = scorer.score(17, 42);
        assertThat(score, nullValue());
    }
}
