package org.lenskit.featurize;

public interface EntityDAO {
    Entity getNextEntity();
    void restart();
}
