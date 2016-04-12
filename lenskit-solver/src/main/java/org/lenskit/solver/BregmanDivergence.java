package org.lenskit.solver;

public interface BregmanDivergence {
    double distance(RealVector rva, RealVector rvb);
}
