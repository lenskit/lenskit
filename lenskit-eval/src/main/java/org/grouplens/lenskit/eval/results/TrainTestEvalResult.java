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
import java.util.List;

/**
 * This class contains the table of the result similar to the output file. The result is the exactly
 * same table with that in the output file stored in the memory. The way to get the field in the table
 * is to use {@link #getRows} and {@link #getValues} functions.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class TrainTestEvalResult {
    private ArrayList<ResultRow> result;
    private final ArrayList<String> fields;
    private int size;

    public TrainTestEvalResult() {
        result = new ArrayList<ResultRow>();
        fields = new ArrayList<String>();
        size = 0;
    }

    public TrainTestEvalResult(List<String> f) {
        this();
        setFields(f);
    }

    public void setFields(List<String> f) {
        fields.addAll(f);
    }

    /**
     * Put a new algorithm in the result.
     *
     * @param list the list of objects to insert to the result table
     * @throws org.grouplens.lenskit.eval.CommandException When the same algorithm is already in the result
     */
    public void addResultRow(Object[] list)  {
        ResultRow row = new ResultRow(list, fields.toArray(new String[fields.size()]));
        result.add(row);
        size++;
    }

    public void addResultRow(ResultRow row) {
        result.add(row);
        size++;
    }

    /**
     * Get a list of rows that satisfy the query.
     * @param col The header name of the column
     * @param val The value in the field
     * @return The list of rows satisfying the query warpped in the TrainTestEvalResult
     *          to enable cascading
     */
    public TrainTestEvalResult getRows(String col, Object val) {
        TrainTestEvalResult rows = new TrainTestEvalResult(this.fields);
        for(ResultRow row: result) {
            if(row.getValue(col).getClass().equals(val.getClass())) {
                if(row.getValue(col).equals(val)) {
                    rows.addResultRow(row);
                }
            }
        }
        return rows;
    }


    public String[] getField() {
        String[] res = new String[fields.size()];
        return fields.toArray(res);
    }

    public int getSize() {
        return size;
    }

    /**
     * Return all the values in the specified column
     * @param col The name of the column
     * @return  An array of the values
     */
    public Object[] getValues(String col) {
        Object[] values = new Object[result.size()];
        int idx = 0;
        for(ResultRow row: result) {
            values[idx] = row.getValue(col);
            idx++;
        }
        return values;
    }
}
