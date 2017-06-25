package org.lenskit.pf;



import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.*;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.special.Gamma;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.RatingMatrixEntry;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HPFModelProvider implements Provider<HPFModel> {
    private final RandomInitializationStrategy randomInitials;
    private final RandomDataSplitStrategy ratings;
    private final StoppingCondition stoppingCondition;
    private final PFHyperParameters hyperParameters;

    @Nullable
    private PreferenceDomain domain;


    @Inject
    public HPFModelProvider(RandomInitializationStrategy rndInitls,
                            RandomDataSplitStrategy rndRatings,
                            StoppingCondition stop,
                            @Nullable PreferenceDomain dom,
                            PFHyperParameters hyperParams) {
        randomInitials = rndInitls;
        ratings = rndRatings;
        stoppingCondition = stop;
        domain = dom;
        hyperParameters = hyperParams;

    }

    @Override
    public HPFModel get() {
        RealMatrix gammaShp = randomInitials.getGammaShp().copy();
        RealMatrix gammaRte = randomInitials.getGammaRte().copy();
        RealVector kappaShp = randomInitials.getKappaShp().copy();
        RealVector kappaRte = randomInitials.getKappaRte().copy();
        RealMatrix lambdaShp = randomInitials.getLambdaShp().copy();
        RealMatrix lambdaRte = randomInitials.getLambdaRte().copy();
        RealVector tauShp = randomInitials.getTauShp().copy();
        RealVector tauRte = randomInitials.getTauRte().copy();
        final int userNum = gammaRte.getData().length;
        final int itemNum = lambdaRte.getData().length;
        final int featureCount = hyperParameters.getFeatureCount();
        final Int2ObjectMap<ImmutableSet<Integer>> userItems = ratings.getUserItemIndices();
        final double a = hyperParameters.getA();
        final double aPrime = hyperParameters.getAPrime();
        final double bPrime = hyperParameters.getBPrime();
        final double c = hyperParameters.getC();
        final double cPrime = hyperParameters.getCPrime();
        final double dPrime = hyperParameters.getDPrime();

        // TODO Add loop with convergence here
        Int2ObjectMap<Int2ObjectMap<RealVector>> phi = new Int2ObjectOpenHashMap<>(userNum);
        Int2ObjectMap<Int2DoubleMap> train = ratings.getTrainingMatrix();
        List<RatingMatrixEntry> validation = ratings.getValidationRatings();

        // update phi
        Iterator<Map.Entry<Integer,Int2DoubleMap>> itemIter = train.entrySet().iterator();
        while (itemIter.hasNext()) {
            Map.Entry<Integer,Int2DoubleMap> itemEntry = itemIter.next();
            int item = itemEntry.getKey();
            Int2DoubleMap itemRaings = itemEntry.getValue();
            IntIterator userIter = itemRaings.keySet().iterator();
            while (userIter.hasNext()) {
                int user = userIter.nextInt();
                Int2ObjectMap<RealVector> phiUIsVec = phi.get(user);
                if (phiUIsVec == null) phiUIsVec = new Int2ObjectOpenHashMap<>(itemNum);
                RealVector phiUI = phiUIsVec.get(item);
                if (phiUI == null) phiUI = MatrixUtils.createRealVector(new double[featureCount]);

                for (int k = 0; k < featureCount; k++) {
                    double gammaShpUK = gammaShp.getEntry(user, k);
                    double gammaRteUK = gammaRte.getEntry(user, k);
                    double lambdaShpIK = lambdaShp.getEntry(item, k);
                    double lambdaRteIK = lambdaRte.getEntry(item, k);
                    double power = Gamma.digamma(gammaShpUK) - Math.log(gammaRteUK) + Gamma.digamma(lambdaShpIK) - Math.log(lambdaRteIK);
                    double phiUIK = Math.exp(power);
                    phiUI.setEntry(k, phiUIK);
                }
                double sumOfElements = phiUI.getL1Norm();
                phiUI.mapDivideToSelf(sumOfElements);
            }
        }

        // update user parameters
        Iterator<Map.Entry<Integer,ImmutableSet<Integer>>> userParamsIter = userItems.entrySet().iterator();
        while (userParamsIter.hasNext()) {
            Map.Entry<Integer,ImmutableSet<Integer>> entry = userParamsIter.next();
            int user = entry.getKey();
            ImmutableSet<Integer> items = entry.getValue();
            double kappaRteU = 0.0;

            for (int k = 0; k < featureCount; k++) {
                double gammaShpUK = 0.0;
                double gammaRteUK = 0.0;
                for (int item : items) {
                    double yUI = train.get(item).get(user);
                    double phiUIK = phi.get(user).get(item).getEntry(k);
                    //plus second term
                    gammaShpUK += yUI * phiUIK;
                    gammaRteUK += lambdaShp.getEntry(item, k) / lambdaRte.getEntry(item, k);
                }
                // plus first term
                gammaShpUK += a;
                gammaRteUK += kappaShp.getEntry(user) / kappaRte.getEntry(user);
                gammaShp.setEntry(user, k, gammaShpUK);
                gammaRte.setEntry(user, k, gammaRteUK);
                // update kappaRteU second term
                kappaRteU += gammaShpUK / gammaRteUK;
            }
            kappaRteU += aPrime / bPrime;
            kappaRte.setEntry(user, kappaRteU);
        }
        
        // update item parameters
        Iterator<Map.Entry<Integer,Int2DoubleMap>> itemParamsIter = train.entrySet().iterator();
        while (itemParamsIter.hasNext()) {
            Map.Entry<Integer,Int2DoubleMap> entry = itemParamsIter.next();
            int item = entry.getKey();
            Int2DoubleMap itemRatings = entry.getValue();
            double tauRteI = 0.0;

            for (int k = 0; k < featureCount; k++) {
                double lambdaShpIK = 0.0;
                double lambdaRteIK = 0.0;
                Iterator<Map.Entry<Integer,Double>> itemRatingsIter = itemRatings.entrySet().iterator();
                while (itemRatingsIter.hasNext()) {
                    Map.Entry<Integer,Double> itemRatingEntry = itemRatingsIter.next();
                    int user = itemRatingEntry.getKey();
                    double yUI = itemRatingEntry.getValue();
                    double phiUIK = phi.get(user).get(item).getEntry(k);
                    //plus second term
                    lambdaShpIK += yUI * phiUIK;
                    lambdaRteIK += gammaShp.getEntry(user, k) / gammaRte.getEntry(user, k);
                }
                // plus first term
                lambdaShpIK += c;
                lambdaRteIK += tauShp.getEntry(item) / tauRte.getEntry(item);
                lambdaShp.setEntry(item, k, lambdaShpIK);
                lambdaRte.setEntry(item, k, lambdaRteIK);
                // update tauRteI second term
                tauRteI += lambdaShpIK / lambdaRteIK;
            }
            tauRteI += cPrime / dPrime;
            tauRte.setEntry(item, tauRteI);
        }

        return null;
    }
}
