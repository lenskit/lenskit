/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.results;

import org.grouplens.lenskit.eval.CommandException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the table of the result similar to the output file. The table is a
 * HashMap, in which the key is the name of the algorithm and the value is a list of result rows
 * indexed by the partition counter. Therefore, the table is essentially the output file starting
 * from the column "BuildTime" with the previous columns left out.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class TrainTestEvalResult {
    private HashMap<String, ArrayList<ResultRow>> result;
    private static ArrayList<String> fields;
    private int partition;

    public TrainTestEvalResult(int partition) {
        this.partition = partition;
        result = new HashMap<String, ArrayList<ResultRow>>();
        fields = new ArrayList<String>();
    }

    /**
     * Put a new algorithm in the result.
     *
     * @param algo The name of the algorithm to evaluate
     * @throws org.grouplens.lenskit.eval.CommandException When the same algorithm is already in the result
     */
    public void putAlgorithm(String algo) throws CommandException {
        ArrayList<ResultRow> value = new ArrayList<ResultRow>();
        for(int i = 0; i < partition; i++) {
            value.add(new ResultRow());
        }
        if(result.put(algo, value)!=null) {
            throw new CommandException("A single train test has duplicated algorithm instances");
        }
    }

    public void addField(String s) {
        fields.add(s);
    }

    public static String getField(int i) {
        return fields.get(i);
    }

    public ResultRow getRow(String row, int p) {
        return result.get(row).get(p);
    }

    public String getValue(String row, String col, int p) {
        return getRow(row, p).getRow().get(col);
    }

    public int getPartition() {
        return partition;
    }

}
