package org.lenskit.bias;

import org.junit.Test;
import org.lenskit.data.dao.EntityCollectionDAOBuilder;
import org.lenskit.data.entities.EntityFactory;

import javax.inject.Provider;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class GlobalBiasModelTest {
    @Test
    public void testZeroBias() {
        BiasModel model = new GlobalBiasModel(0);
        assertThat(model.getIntercept(), equalTo(0.0));
        assertThat(model.getUserBias(42L), equalTo(0.0));
        assertThat(model.getItemBias(42L), equalTo(0.0));
    }

    @Test
    public void testBasicBias() {
        BiasModel model = new GlobalBiasModel(Math.PI);
        assertThat(model.getIntercept(), equalTo(Math.PI));
        assertThat(model.getUserBias(42L), equalTo(0.0));
        assertThat(model.getItemBias(42L), equalTo(0.0));
    }

    @Test
    public void testComputeGlobalMean() {
        EntityFactory efac = new EntityFactory();
        EntityCollectionDAOBuilder daoBuilder = new EntityCollectionDAOBuilder();
        daoBuilder.addEntities(efac.rating(100, 200, 3.0),
                               efac.rating(101, 200, 4.0),
                               efac.rating(101, 201, 2.5),
                               efac.rating(102, 203, 4.5));
        Provider<GlobalBiasModel> biasProvider = new GlobalAverageRatingBiasModelProvider(daoBuilder.build());
        BiasModel model = biasProvider.get();

        assertThat(model.getIntercept(), closeTo(3.5, 1.0e-1));
    }
}