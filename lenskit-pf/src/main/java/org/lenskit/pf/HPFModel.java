package org.lenskit.pf;


import org.apache.commons.math3.linear.RealMatrix;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.mf.svd.MFModel;
import org.lenskit.util.keys.KeyIndex;

@DefaultProvider(HPFModelProvider.class)
@Shareable
public class HPFModel extends MFModel {
    private static final long serialVersionUID = 4L;

    public HPFModel(RealMatrix umat, RealMatrix imat,
                    KeyIndex uidx, KeyIndex iidx) {
        super(umat, imat, uidx, iidx);
    }
}
