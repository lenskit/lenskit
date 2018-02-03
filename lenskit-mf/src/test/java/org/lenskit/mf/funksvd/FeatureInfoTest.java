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
package org.lenskit.mf.funksvd;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FeatureInfoTest {
    @Test
    public void testBasic() {
        FeatureInfo.Builder bld = new FeatureInfo.Builder(1);
        bld.setItemAverage(1)
           .setUserAverage(2)
           .setSingularValue(3);
        FeatureInfo info = bld.build();
        assertThat(info, notNullValue());
        assertThat(info.getIterCount(), equalTo(0));
        assertThat(info.getTrainingErrors(), hasSize(0));
        assertThat(info.getFeature(), equalTo(1));
        assertThat(info.getItemAverage(), equalTo(1.0));
        assertThat(info.getUserAverage(), equalTo(2.0));
        assertThat(info.getSingularValue(), equalTo(3.0));
    }

    @Test
    public void testTrainError() {
        FeatureInfo.Builder bld = new FeatureInfo.Builder(5);
        bld.setItemAverage(1)
           .setUserAverage(2)
           .setSingularValue(3);
        bld.addTrainingRound(0.990)
           .addTrainingRound(0.985);
        FeatureInfo info = bld.build();
        assertThat(info, notNullValue());
        assertThat(info.getFeature(), equalTo(5));
        assertThat(info.getItemAverage(), equalTo(1.0));
        assertThat(info.getUserAverage(), equalTo(2.0));
        assertThat(info.getSingularValue(), equalTo(3.0));

        assertThat(info.getIterCount(), equalTo(2));
        assertThat(info.getTrainingErrors(), hasSize(2));
        assertThat(info.getLastRMSE(), closeTo(0.985, 1.0e-5));
        assertThat(info.getLastDeltaRMSE(), closeTo(0.005, 1.0e-5));
    }
}
