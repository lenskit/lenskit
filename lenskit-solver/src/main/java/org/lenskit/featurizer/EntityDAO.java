package org.lenskit.featurizer;

public interface EntityDAO {
    Entity getNextEntity();
    void restart();
    void close();
}
