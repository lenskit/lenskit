package org.lenskit.bias;

import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.data.dao.EntityCollectionDAOBuilder;
import org.lenskit.data.entities.EntityFactory;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ItemBiasModelTest {
    @Test
    public void testNoItems() {
        BiasModel model = new ItemBiasModel(1.5, Long2DoubleMaps.EMPTY_MAP);
        assertThat(model.getIntercept(), equalTo(1.5));
        assertThat(model.getItemBias(42L), equalTo(0.0));
        assertThat(model.getUserBias(42L), equalTo(0.0));
    }

    @Test
    public void testWithItems() {
        BiasModel model = new ItemBiasModel(1.5, Long2DoubleMaps.singleton(42L, 1.0));
        assertThat(model.getIntercept(), equalTo(1.5));
        assertThat(model.getItemBias(42L), equalTo(1.0));
        assertThat(model.getItemBias(37L), equalTo(0.0));
        assertThat(model.getUserBias(42L), equalTo(0.0));
    }

    @Test
    public void testComputeMeans() {
        EntityFactory efac = new EntityFactory();
        EntityCollectionDAOBuilder daoBuilder = new EntityCollectionDAOBuilder();
        daoBuilder.addEntities(efac.rating(100, 200, 3.0),
                               efac.rating(101, 200, 4.0),
                               efac.rating(101, 201, 2.5),
                               efac.rating(102, 203, 4.5),
                               efac.rating(103, 203, 3.5));
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(BiasModel.class);
        config.bind(BiasModel.class).toProvider(ItemAverageRatingBiasModelProvider.class);

        LenskitRecommender rec = LenskitRecommender.build(config, daoBuilder.build());
        BiasModel model = rec.get(BiasModel.class);

        assertThat(model.getIntercept(), closeTo(3.5, 1.0e-3));
        assertThat(model.getItemBias(200), closeTo(0.0, 1.0e-3));
        assertThat(model.getItemBias(201), closeTo(-1.0, 1.0e-3));
        assertThat(model.getItemBias(203), closeTo(0.5, 1.0e-3));
    }
}