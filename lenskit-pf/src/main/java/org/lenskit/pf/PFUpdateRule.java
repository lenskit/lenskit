package org.lenskit.pf;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.apache.commons.math3.linear.RealMatrix;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.inject.Shareable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Random;

/**
 * Configuration and initialization for computing poisson factorization
 *
 */
@Shareable
public final class PFUpdateRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double a;
    private final double aPrime;
    private final double bPrime;
    private final double c;
    private final double cPrime;
    private final double dPrime;
    private final int featureCount;
    private final Random random;
    private final RatingMatrix snapshot;


    @Inject
    public PFUpdateRule(@HyperParameterA double a,
                        @HyperParameterAPrime double aP,
                        @HyperParameterBPrime double bP,
                        @HyperParameterC double c,
                        @HyperParameterCPrime double cP,
                        @HyperParameterDPrime double dP,
                        @FeatureCount int k,
                        Random rnd,
                        RatingMatrix ratings) {
        this.a = a;
        aPrime = aP;
        bPrime = bP;
        this.c = c;
        cPrime = cP;
        dPrime = dP;
        featureCount = k;
        random = rnd;
        snapshot = ratings;

    }

    public void get() {
        Long2ObjectMap<Long2DoubleMap> gammaShp = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<Long2DoubleMap> gammaRte = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<Long2DoubleMap> lambdaShp = new Long2ObjectOpenHashMap<>();
        Long2ObjectMap<Long2DoubleMap> lambdaRte = new Long2ObjectOpenHashMap<>();
        Long2DoubleMap kappaShp = new Long2DoubleOpenHashMap();
        Long2DoubleMap kappaRte = new Long2DoubleOpenHashMap();
        Long2DoubleMap tauShp = new Long2DoubleOpenHashMap();
        Long2DoubleMap tauRte = new Long2DoubleOpenHashMap();

        random.setSeed(0L);
        int size = snapshot.getRatings().size();
        int validationSize = size/100;
        random.ints(validationSize, 0, size).distinct().iterator().hasNext();
        snapshot.getRatings();



    }
}
