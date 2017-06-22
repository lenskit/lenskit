package org.lenskit.pf;


import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public interface InitializationStrategy {

    RealMatrix getGammaShp();

    RealMatrix getGammaRte();

    RealVector getKappaShp();

    RealVector getKappaRte();

    RealMatrix getLambdaShp();

    RealMatrix getLambdaRte();

    RealVector getTauShp();

    RealVector getTauRte();
}
