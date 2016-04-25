package org.lenskit.featurizer;

import java.io.*;

public class BasicEntityDAO implements EntityDAO {
    private final File sourceFile;
    private final BufferedReader reader;
    private final String delimiter = "\t";

    public BasicEntityDAO(File sourceFile) throws FileNotFoundException {
        this.sourceFile = sourceFile;
        this.reader = new BufferedReader(new FileReader(this.sourceFile));
    }

    public Entity getNextEntity() {
        try {
            String line = reader.readLine();
            if (line == null) {
                return null;
            } else {
                Entity entity = new Entity();
                String[] fields = line.split(delimiter);
                int attrCnt = Integer.parseInt(fields[0]);
                int base = 1;
                for (int i=0; i<attrCnt; i++) {
                    String key = fields[base];
                    int valCnt = Integer.parseInt(fields[base + 1]);
                    String val1 = fields[base + 2];
                    VariableValueType valueType = VariableValueType.parseValueType(val1);
                    for (int j=0; j<valCnt; j++) {
                        valueType.addIntoEntity(entity, key, fields[base + 2 + j]);
                    }
                    base += (2 + valCnt);
                }
                return entity;
            }
        } catch (IOException e) {
            //TODO: add logging
            return null;
        }
    }

    public void restart() {

    }
}
