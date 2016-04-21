package org.lenskit.featurize;

public enum VariableValueType {
    CATEGORICAL("categorical"),
    NUMERICAL("numerical");

    private final String key;

    private VariableValueType(String key) {
        this.key = key;
    }

    public String get() {
        return key;
    }
}
