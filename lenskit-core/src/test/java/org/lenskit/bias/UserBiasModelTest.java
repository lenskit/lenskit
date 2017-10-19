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

public class UserBiasModelTest {
    @Test
    public void testNoUsers() {
        BiasModel model = new UserBiasModel(1.5, Long2DoubleMaps.EMPTY_MAP);
        assertThat(model.getIntercept(), equalTo(1.5));
        assertThat(model.getItemBias(42L), equalTo(0.0));
        assertThat(model.getUserBias(42L), equalTo(0.0));
    }

    @Test
    public void testWithUsers() {
        BiasModel model = new UserBiasModel(1.5, Long2DoubleMaps.singleton(42L, 1.0));
        assertThat(model.getIntercept(), equalTo(1.5));
        assertThat(model.getUserBias(42L), equalTo(1.0));
        assertThat(model.getUserBias(37L), equalTo(0.0));
        assertThat(model.getItemBias(42L), equalTo(0.0));
    }

    @Test
    public void testComputeMeans() {
        EntityFactory efac = new EntityFactory();
        EntityCollectionDAOBuilder daoBuilder = new EntityCollectionDAOBuilder();
        daoBuilder.addEntities(efac.rating(100, 200, 3.0),
                               efac.rating(101, 200, 4.0),
                               efac.rating(102, 201, 2.5),
                               efac.rating(102, 203, 4.5),
                               efac.rating(101, 203, 3.5));
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(BiasModel.class);
        config.bind(BiasModel.class).to(UserBiasModel.class);

        LenskitRecommender rec = LenskitRecommender.build(config, daoBuilder.build());
        BiasModel model = rec.get(BiasModel.class);

        assertThat(model.getIntercept(), closeTo(3.5, 1.0e-3));
        assertThat(model.getUserBias(100), closeTo(-0.5, 1.0e-3));
        assertThat(model.getUserBias(101), closeTo(0.25, 1.0e-3));
        assertThat(model.getUserBias(102), closeTo(0.0, 1.0e-3));
    }
}