package org.lenskit.featurize;

import java.io.File;

public class BasicEntityDAO implements EntityDAO {
    private final File sourceFile;

    public BasicEntityDAO(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Entity getNextEntity() {
        return null;
    }

    public void restart() {

    }
}
