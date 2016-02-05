package org.lenskit.solver.objective;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.Random;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RandomInitializer {
    double multi;
    Random rand;

    public RandomInitializer() {
        multi = 0.001;
        rand = new Random();
    }

    public RandomInitializer(long seed, double multiplier) {
        multi = multiplier;
        rand = new Random(seed);
    }

    public void randInitVector(RealVector vec) {
        int len = vec.getDimension();
        for (int i=0; i<len; i++) {
            vec.setEntry(i, rand.nextDouble() * multi);
        }
    }

    public void randInitMatrix(RealMatrix mat) {
        int len = mat.getRowDimension();
        for (int i=0; i<len; i++) {
            randInitVector(mat.getRowVector(i));
        }
    }
}
