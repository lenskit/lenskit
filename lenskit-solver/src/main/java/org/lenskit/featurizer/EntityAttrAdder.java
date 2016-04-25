package org.lenskit.featurize;

public interface EntityAttrAdder {
    void addIntoEntity(Entity entity, String name, String value);
}
