package org.grouplens.lenskit.vectors;

/**
 * Mutable extension of {@link SparseVectorMap}.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class MutableSparseVectorMap extends SparseVectorMap {
    private final MutableSparseVector msv;

    MutableSparseVectorMap(MutableSparseVector vec) {
        super(vec);
        msv = vec;
    }

    @Override
    public Double put(long key, Double value) {
        if (msv.containsKey(key)) {
            return msv.set(key, value);
        } else if (msv.keyDomain().contains(key)) {
            msv.set(key, value);
            return defaultReturnValue();
        } else {
            return defaultReturnValue();
        }
    }

    @Override
    public Double remove(long key) {
        Double rv = defaultReturnValue();
        if (msv.containsKey(key)) {
            rv = msv.get(key);
            msv.unset(key);
        }
        return rv;
    }

    @Override
    public void clear() {
        msv.clear();
    }
}
