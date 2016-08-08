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
    public void testModelBuildFromEntityDAO() throws IOException {

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
        SVDFeatureModelProvider modelBuilder = new SVDFeatureModelProvider(dao, null, null,
                                                                           null,
                                                                           null,
                                                                           null,
                                                                           biasSize, factSize, factDim,
                                                                           CommonAttributes.RATING.getName(),
                                                                           "weight", loss, method);
        modelBuilder.get();
    }
}
