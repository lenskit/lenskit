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

import com.google.common.collect.Lists;
import org.junit.Test;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.ratings.Rating;

import java.util.List;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.*;

/**
 * Created by MichaelEkstrand on 10/1/2016.
 */
public class LiveUserItemBiasModelTest {
    @Test
    public void testComputeAllMeans() {
        EntityFactory efac = new EntityFactory();
        List<Rating> ratings = Lists.newArrayList(efac.rating(100, 200, 3.0),
                                                  efac.rating(101, 200, 4.0),
                                                  efac.rating(102, 201, 2.5),
                                                  efac.rating(102, 203, 4.5),
                                                  efac.rating(101, 203, 3.5));
        LenskitConfiguration config = new LenskitConfiguration();
        config.addRoot(BiasModel.class);
        config.bind(BiasModel.class).to(LiveUserItemBiasModel.class);

        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, EntityCollectionDAO.create(ratings));

        ratings.add(efac.rating(105, 200, 4.5));
        ratings.add(efac.rating(105, 203, 4.8));

        LenskitRecommender rec = engine.createRecommender(EntityCollectionDAO.create(ratings));
        BiasModel model = rec.get(BiasModel.class);

        assertThat(model.getIntercept(), closeTo(3.5, 1.0e-3));
        assertThat(model.getItemBias(200), closeTo(0.0, 1.0e-3));
        assertThat(model.getItemBias(201), closeTo(-1.0, 1.0e-3));
        assertThat(model.getItemBias(203), closeTo(0.5, 1.0e-3));
        assertThat(model.getUserBias(100), closeTo(-0.5, 1.0e-3));
        assertThat(model.getUserBias(101), closeTo(0, 1.0e-3));
        assertThat(model.getUserBias(102), closeTo(0.25, 1.0e-3));

        assertThat(model.getUserBias(105), closeTo(0.9, 1.0e-3));
    }
}