package org.lenskit.mf.svdfeature;

import org.lenskit.solver.LearningData;

import javax.inject.Inject;
import java.io.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureInstanceDAO implements LearningData {
    private final File sourceFile;
    private BufferedReader reader;
    private final String delimiter;

    //add default setting for file and delim
    @Inject
    public SVDFeatureInstanceDAO(File file, String delim) throws FileNotFoundException {
        sourceFile = file;
        delimiter = delim;
        reader = new BufferedReader(new FileReader(file));
    }

    public SVDFeatureInstance getLearningInstance() {
        try {
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
                for (int i = 0; i < gfeaNum; i++) {
                    Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]),
                                              Double.parseDouble(fields[start + 1 + 2 * i]));
                    ins.gfeas.add(fea);
                }
                start = 5 + 2 * gfeaNum;
                for (int i = 0; i < ufeaNum; i++) {
                    Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]),
                                              Double.parseDouble(fields[start + 1 + 2 * i]));
                    ins.ufeas.add(fea);
                }
                start = 5 + 2 * gfeaNum + 2 * ufeaNum;
                for (int i = 0; i < ifeaNum; i++) {
                    Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]),
                                              Double.parseDouble(fields[start + 1 + 2 * i]));
                    ins.ifeas.add(fea);
                }
                return ins;
            }
        } catch (IOException e) {
            //TODO: add logging
            return null;
        }
    }

    public void startNewIteration() {
        try {
            reader.close();
            reader = new BufferedReader(new FileReader(sourceFile));
        } catch (IOException e) {
            //TODO: add logging
        }
    }
}
