package org.lenskit.featurizer;

public interface EntityAttrAdder {
    void addIntoEntity(Entity entity, String name, String value);
}
