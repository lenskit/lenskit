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
