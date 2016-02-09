package org.lenskit.mf.svdfeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureInstanceDAO {
    private final File sourceFile;
    private BufferedReader reader;
    private final String delimiter;

    public SVDFeatureInstanceDAO(File file, String delim) throws FileNotFoundException {
        sourceFile = file;
        delimiter = delim;
        reader = new BufferedReader(new FileReader(file));
    }

    public SVDFeatureInstance getNextInstance() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        } else {
            SVDFeatureInstance ins = new SVDFeatureInstance();
            String[] fields = line.split(delimiter);
            ins.weight = Double.parseDouble(fields[0]);
            ins.label = Double.parseDouble(fields[1]);
            int gfeaNum = Integer.parseInt(fields[2]);
            int ufeaNum = Integer.parseInt(fields[3]);
            int ifeaNum = Integer.parseInt(fields[4]);
            int start = 5;
            for (int i=0; i<gfeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.gfeas.add(fea);
            }
            start = 5 + 2 * gfeaNum;
            for (int i=0; i<ufeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.ufeas.add(fea);
            }
            start = 5 + 2 * gfeaNum + 2 * ufeaNum;
            for (int i=0; i<ifeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.ifeas.add(fea);
            }
            return ins;
        }
    }

    public void goBackToBeginning() throws IOException {
        reader.close();
        reader = new BufferedReader(new FileReader(sourceFile));
    }
}
