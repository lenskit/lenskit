package org.lenskit.solver;

import org.apache.commons.math3.linear.RealVector;

public interface BregmanDivergence {
    double distance(RealVector rva, RealVector rvb);
}
