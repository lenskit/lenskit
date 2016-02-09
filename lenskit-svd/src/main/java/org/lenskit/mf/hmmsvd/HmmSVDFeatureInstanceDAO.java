package org.lenskit.mf.hmmsvd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureInstanceDAO {
    private final File sourceFile;
    private BufferedReader reader;
    private final String delimiter;

    public HmmSVDFeatureInstanceDAO(File file, String delim) throws FileNotFoundException {
        sourceFile = file;
        delimiter = delim;
        reader = new BufferedReader(new FileReader(file));
    }

    public HmmSVDFeatureInstance getNextInstance() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        } else {
            HmmSVDFeatureInstance ins = new HmmSVDFeatureInstance();
            String[] fields = line.split(delimiter);
            int gfeaNum = Integer.parseInt(fields[0]);
            int ufeaNum = Integer.parseInt(fields[1]);
            ins.numPos = Integer.parseInt(fields[2]);
            int ifeaNum = Integer.parseInt(fields[3]);
            ins.numObs = Integer.parseInt(fields[4]);
            int meta = 5, start = 5;
            for (int i=0; i<gfeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.gfeas.add(fea);
            }
            start = meta + 2 * gfeaNum;
            for (int i=0; i<ufeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.ufeas.add(fea);
            }
            for (int j=0; j<ins.numPos; j++) {
                ArrayList<Feature> ifeas = new ArrayList<Feature>();
                start = meta + 2 * gfeaNum + 2 * ufeaNum + 2 * j * ifeaNum;
                for (int i=0; i<ifeaNum; i++) {
                    Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                              Double.parseDouble(fields[start + 1 + 2 * i]));
                    ifeas.add(fea);
                }
                ins.pos2ifeas.add(ifeas);
            }
            start = meta + 2 * gfeaNum + 2 * ufeaNum + 2 * ins.numPos * ifeaNum;
            for (int i=0; i<ins.numObs; i++) {
                ins.IntArrayList.add(Integer.parseInt(fields[start + i]));
            }
            return ins;
        }
    }

    public void goBackToBeginning() throws IOException {
        reader.close();
        reader = new BufferedReader(new FileReader(sourceFile));
    }
}
