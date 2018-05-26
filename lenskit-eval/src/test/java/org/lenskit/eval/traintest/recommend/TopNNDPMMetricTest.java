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
package org.lenskit.eval.traintest.recommend;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someNonEmptyLists;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static net.java.quickcheck.generator.PrimitiveGenerators.integers;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.lenskit.eval.traintest.recommend.TopNMRRMetricTest.closeTo;

public class TopNNDPMMetricTest {
    @Test
    public void testMetricRangesAreValid() {
        for (List<Double> scores: someNonEmptyLists(doubles(0, 5))) {
            RealVector v = new ArrayRealVector(scores.size());
            for (int i = 0; i < scores.size(); i++) {
                v.setEntry(i, scores.get(i));
            }

            double dpm = TopNNDPMMetric.computeDPM(v);
            assertThat(dpm, greaterThanOrEqualTo(0.0));
            double norm = TopNNDPMMetric.computeNormalizingFactor(v);
            assertThat(norm, greaterThanOrEqualTo(0.0));
            assertThat(dpm, lessThanOrEqualTo(norm));
        }
    }

    @Test
    @Ignore("NDPM is broken")
    public void testMetricRangesAreValidWithQuantization() {
        for (List<Integer> scores: someNonEmptyLists(integers(1, 5))) {
            RealVector v = new ArrayRealVector(scores.size());
            for (int i = 0; i < scores.size(); i++) {
                v.setEntry(i, scores.get(i));
            }

            double dpm = TopNNDPMMetric.computeDPM(v);
            assertThat(dpm, greaterThanOrEqualTo(0.0));
            double norm = TopNNDPMMetric.computeNormalizingFactor(v);
            assertThat(norm, greaterThanOrEqualTo(0.0));
            assertThat(dpm, lessThanOrEqualTo(norm));
        }
    }

    @Test
    @Ignore("NDPM is broken")
    public void testDPMFromExample3() {
        // user preference: {d1,d2} > d3 > {d4,d5}
        // system ranking: d1 d5 d2 d4 d3
        // put user pref scores in order of system ranking
        double[] scores = {3, 1, 3, 1, 2};
        RealVector v = new ArrayRealVector(scores);
        double dpm = TopNNDPMMetric.computeDPM(v);
        assertThat(dpm, closeTo(8.0));
    }

    @Test
    public void testPerfectDPMFromExample3() {
        // user preference: {d1,d2} > d3 > {d4,d5}
        // system ranking: d1 d5 d2 d4 d3
        // put user pref scores in order of system ranking
        double[] scores = {3, 3, 2, 1, 1};
        RealVector v = new ArrayRealVector(scores);
        double dpm = TopNNDPMMetric.computeDPM(v);
        assertThat(dpm, closeTo(0.0));
    }
}