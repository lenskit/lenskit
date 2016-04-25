package org.lenskit.featurizer;

public enum VariableValueType implements EntityAttrAdder {
    CATEGORICAL("categorical") {
        public void addIntoEntity(Entity entity, String name, String value) {
           entity.addCatAttr(name, value.substring(1, value.length() - 1));
        }
    },
    NUMERICAL("numerical") {
        public void addIntoEntity(Entity entity, String name, String value) {
            entity.setNumAttr(name, Double.parseDouble(value));
        }
    };

    private final String key;

    public static VariableValueType parseValueType(String value) {
        if ((value.indexOf("\"") == 0 && value.lastIndexOf("\"") == value.length() - 1) ||
                (value.indexOf("'") == 0 && value.lastIndexOf("'") == value.length() - 1)) {
            return CATEGORICAL;
        } else {
            return NUMERICAL;
        }
    }

    private VariableValueType(String key) {
        this.key = key;
    }

    public String get() {
        return key;
    }
}
