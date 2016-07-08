package org.lenskit.mf.svdfeature;

import org.junit.Test;
import org.lenskit.data.dao.file.CollectionEntitySource;
import org.lenskit.data.entities.Entity;
import org.lenskit.featurizer.*;
import org.lenskit.solver.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuildTest {

    @Test
    public void testModelBuildFromInstanceDAO() throws IOException {
        String insFile = "";
        String modelFile = "";

        int biasSize = 10000;
        int factSize = 10000;
        int factDim = 20;

        ObjectiveFunction loss = new LogisticLoss();
        OptimizationMethod method = new StochasticGradientDescent(50, 0.0, 0.0001, 1.0);
        SVDFeatureInstanceDAO insDao = new SVDFeatureInstanceDAO(new File(insFile), " ");

        SVDFeatureModel model = new SVDFeatureModel(biasSize, factSize, factDim, loss);
        method.minimize(model, insDao);
        model.dump(new File(modelFile));
    }

    public void testModelBuildFromEntityDAO() throws IOException {

        int biasSize = 1;
        int factSize = 1;
        int factDim = 5;
        ObjectiveFunction loss = new L2NormLoss();
        OptimizationMethod method = new StochasticGradientDescent();
        List<Entity> entityList = new ArrayList<>(5);
        //TODO: add five entities into the list

        CollectionEntitySource entitySource = new CollectionEntitySource(entityList);
        List<FeatureExtractor> featureExtractors = new ArrayList<>();
        featureExtractors.add(new ConstantOneExtractor("biases", "globalBias", "globalBiasIdx"));
        featureExtractors.add(new StringToIdxExtractor("biases", "userId", "userBiasIdx", " "));
        featureExtractors.add(new StringToIdxExtractor("biases", "movieId", "itemBiasIdx", " "));
        featureExtractors.add(new StringToIdxExtractor("factors", "userId", "userFactIdx", " "));
        featureExtractors.add(new StringToIdxExtractor("factors", "movieId", "itemFactIdx", " "));
        String[] bFeas = {"globalBiasIdx", "userBiasIdx", "itemBiasIdx"};
        String[] uFeas = {"userFactIdx"};
        String[] iFeas = {"itemFactIdx"};
        SVDFeatureModelBuilder modelBuilder = new SVDFeatureModelBuilder(entitySource, featureExtractors,
                                                                         new HashSet<>(Arrays.asList(bFeas)),
                                                                         new HashSet<>(Arrays.asList(uFeas)),
                                                                         new HashSet<>(Arrays.asList(iFeas)),
                                                                         biasSize, factSize, factDim,
                                                                         "rating", "weight", loss, method);
        modelBuilder.get();
    }
}
