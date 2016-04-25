package org.lenskit.mf.svdfeature;

import org.junit.Test;
import org.lenskit.featurizer.*;
import org.lenskit.solver.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuildTest {

    public void testModelBuildFromInstanceDAO() throws IOException {
        String train = "";
        String modelFile = "";
        int biasSize = 38544;
        int factSize = 38543;
        int factDim = 20;
        SVDFeatureInstanceDAO dao = new SVDFeatureInstanceDAO(new File(train), " ");
        LogisticLoss loss = new LogisticLoss();
        OptimizationMethod method = new StochasticGradientDescent();
        SVDFeatureModelBuilder modelBuilder = new SVDFeatureModelBuilder(biasSize, factSize, factDim,
                                                                         dao, loss, method);
        SVDFeatureModel model = modelBuilder.get();
        ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(modelFile));
        fout.writeObject(model);
        fout.close();
    }

    @Test
    public void testModelBuildFromEntityDAO() throws IOException {

        String train = "/home/qian/Study/pyml/RecCtr/data/lenskit-svdfeature-entity.tsv";
        String modelFile = "/home/qian/Study/pyml/RecCtr/data/lenskit-svdfeature.bin";
        int biasSize = 38544;
        int factSize = 38543;
        int factDim = 20;
        ObjectiveFunction loss = new L2NormLoss();
        OptimizationMethod method = new StochasticGradientDescent();
        EntityDAO entityDAO = new BasicEntityDAO(new File(train));
        List<FeatureExtractor> featureExtractors = new ArrayList<>();
        featureExtractors.add(new IdentityExtractor("biases", "intercept", "globalBias"));
        featureExtractors.add(new IdToIdxExtractor("biases", "userId", "userBiasIdx"));
        featureExtractors.add(new IdToIdxExtractor("biases", "movieId", "itemBiasIdx"));
        featureExtractors.add(new IdToIdxExtractor("factors", "userId", "userFactIdx"));
        featureExtractors.add(new IdToIdxExtractor("factors", "movieId", "itemFactIdx"));
        String[] bFeas = {"globalBias", "userBiasIdx", "itemBiasIdx"};
        String[] uFeas = {"userFactIdx"};
        String[] iFeas = {"itemFactIdx"};
        SVDFeatureModelBuilder modelBuilder = new SVDFeatureModelBuilder(entityDAO, featureExtractors,
                                                                         new HashSet<>(Arrays.asList(bFeas)),
                                                                         new HashSet<>(Arrays.asList(uFeas)),
                                                                         new HashSet<>(Arrays.asList(iFeas)),
                                                                         biasSize, factSize, factDim,
                                                                         "rating", "weight", loss, method);
        SVDFeatureModel model = modelBuilder.get();
        ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(modelFile));
        fout.writeObject(model);
        fout.close();
    }
}
