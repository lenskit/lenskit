/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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