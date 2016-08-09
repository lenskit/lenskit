/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

package org.lenskit.mf.svdfeature;

import org.junit.Test;

import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.entities.*;
import org.lenskit.featurizer.*;
import org.lenskit.solver.*;

import java.io.*;
import java.util.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuildTest {

    @Test
    public void testModelBuildFromEntityDAO() {

        int biasSize = 1;
        int factSize = 1;
        int factDim = 5;
        ObjectiveFunction loss = new L2NormLoss();
        OptimizationMethod method = new StochasticGradientDescent();
        EntityType entityType = CommonTypes.RATING;
        List<Entity> entityList = new ArrayList<>(10);
        Random random = new Random();
        for (int i=0; i<20; i++) {
            EntityBuilder entityBuilder = new BasicEntityBuilder(entityType);
            entityBuilder.setAttribute(CommonAttributes.USER_ID, random.nextLong() % 5);
            entityBuilder.setAttribute(CommonAttributes.ITEM_ID, random.nextLong() % 10);
            entityBuilder.setAttribute(CommonAttributes.RATING, (double)(random.nextInt(5) + 1));
        }
        DataAccessObject dao = EntityCollectionDAO.create(entityList);
        SVDFeatureModelProvider modelBuilder = new SVDFeatureModelProvider(entityType, dao, null, null,
                                                                           null,
                                                                           null,
                                                                           null,
                                                                           biasSize, factSize, factDim,
                                                                           CommonAttributes.RATING.getName(),
                                                                           "weight", loss, method);
        modelBuilder.get();
    }
}
