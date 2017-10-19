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
package org.lenskit.predict.ordrec;

import org.lenskit.LenskitConfiguration;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.test.CrossfoldTestSuite;
import org.lenskit.util.table.Table;
import org.junit.Ignore;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.baseline.*;
import org.lenskit.mf.funksvd.FeatureCount;
import org.lenskit.mf.funksvd.FunkSVDItemScorer;
import org.lenskit.transform.quantize.PreferenceDomainQuantizer;
import org.lenskit.transform.quantize.Quantizer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Do major tests on the OrdRec recommender.
 */
@Ignore("not ready to really test accuracy")
public class OrdRecAccuracyTest extends CrossfoldTestSuite {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        config.bind(ItemScorer.class)
              .to(FunkSVDItemScorer.class);
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(ItemMeanRatingItemScorer.class);
        config.within(BaselineScorer.class, ItemScorer.class)
              .set(MeanDamping.class)
              .to(10);
        config.set(FeatureCount.class).to(25);
        config.set(IterationCount.class).to(125);
        config.bind(RatingPredictor.class)
              .to(OrdRecRatingPredictor.class);
        config.bind(Quantizer.class)
              .to(PreferenceDomainQuantizer.class);
    }

    @Override
    protected void checkResults(Table table) {
        assertThat(table.column("MAE.ByRating").average(),
                   closeTo(0.74, 0.025));
        assertThat(table.column("RMSE.ByUser").average(),
                   closeTo(0.92 , 0.05));
    }
}
