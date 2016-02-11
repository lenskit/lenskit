package org.lenskit.mf.hmmsvd;

import org.lenskit.mf.svdfeature.Feature;

import java.io.*;
import java.util.ArrayList;

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
            ins.setNumPos(Integer.parseInt(fields[2]));
            int bifeaNum = Integer.parseInt(fields[3]);
            int fifeaNum = Integer.parseInt(fields[4]);
            ins.numObs = Integer.parseInt(fields[5]);
            int meta = 6, start = 6;
            for (int i=0; i<gfeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]),
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.addGlobalFeas(fea);
            }
            start = meta + 2 * gfeaNum;
            for (int i=0; i<ufeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.ufeas.add(fea);
            }
            for (int j=0; j<ins.numPos; j++) {
                start = meta + 2 * gfeaNum + 2 * ufeaNum + 2 * j * (bifeaNum + fifeaNum);
                for (int i=0; i<bifeaNum; i++) {
                    Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                              Double.parseDouble(fields[start + 1 + 2 * i]));
                    ins.pos2gfeas.get(j).add(fea);
                }
                start = meta + 2 * gfeaNum + 2 * ufeaNum + 2 * j * (bifeaNum + fifeaNum) + 2 * bifeaNum;
                for (int i=0; i<fifeaNum; i++) {
                    Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]),
                                              Double.parseDouble(fields[start + 1 + 2 * i]));
                    ins.pos2ifeas.get(j).add(fea);
                }
            }
            start = meta + 2 * gfeaNum + 2 * ufeaNum + 2 * ins.numPos * (bifeaNum + fifeaNum);
            for (int i=0; i<ins.numObs; i++) {
                ins.obs.add(Integer.parseInt(fields[start + i]));
            }
            return ins;
        }
    }

    public void goBackToBeginning() throws IOException {
        reader.close();
        reader = new BufferedReader(new FileReader(sourceFile));
    }
}
