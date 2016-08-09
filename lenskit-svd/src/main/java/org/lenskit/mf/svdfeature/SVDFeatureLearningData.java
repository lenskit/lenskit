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

import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.featurizer.Featurizer;
import org.lenskit.solver.LearningData;
import org.lenskit.solver.LearningInstance;
import org.lenskit.util.io.ObjectStream;

public class SVDFeatureLearningData implements LearningData {

    private final Featurizer featurizer;
    private final DataAccessObject dao;
    private final EntityType entityType;
    private ObjectStream<Entity> entityStream = null;

    public SVDFeatureLearningData(EntityType entityType,
                                  DataAccessObject dao,
                                  Featurizer featurizer) {
        this.entityType = entityType;
        this.dao = dao;
        this.featurizer = featurizer;
    }

    public LearningInstance getLearningInstance() {
        if (entityStream == null) {
            return null;
        }
        Entity entity = entityStream.readObject();
        if (entity == null) {
            entityStream.close();
            return null;
        } else {
            return featurizer.featurize(entity, true);
        }
    }

    public void startNewIteration() {
        entityStream = dao.streamEntities(entityType);
    }
}
