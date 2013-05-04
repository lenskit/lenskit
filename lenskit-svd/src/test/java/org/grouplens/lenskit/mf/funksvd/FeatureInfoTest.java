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
package org.grouplens.lenskit.mf.funksvd;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
