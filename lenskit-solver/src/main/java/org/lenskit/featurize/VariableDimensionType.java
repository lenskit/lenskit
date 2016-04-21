package org.lenskit.featurize;

public enum VariableDimensionType {
    SCALAR("scalar"),
    VECTOR("vector");

    private final String key;

    private VariableDimensionType(String key) {
        this.key = key;
    }

    public String get() {
        return key;
    }
}
