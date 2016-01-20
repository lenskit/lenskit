/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.mf.svdfeature;

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
            ins.setLabel(Double.parseDouble(fields[0]));
            int gfeaNum = Integer.parseInt(fields[1]);
            int ufeaNum = Integer.parseInt(fields[2]);
            int ifeaNum = Integer.parseInt(fields[3]);
            int start = 4;
            for (int i=0; i<gfeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.addGlobalFea(fea);
            }
            start = 4 + 2 * gfeaNum;
            for (int i=0; i<ufeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.addUserFea(fea);
            }
            start = 4 + 2 * gfeaNum + 2 * ufeaNum;
            for (int i=0; i<ifeaNum; i++) {
                Feature fea = new Feature(Integer.parseInt(fields[start + 2 * i]), 
                                          Double.parseDouble(fields[start + 1 + 2 * i]));
                ins.addItemFea(fea);
            }
            return ins;
        }
    }

    public void goBackToBeginning() throws IOException {
        reader.close();
        reader = new BufferedReader(new FileReader(sourceFile));
    }
}
