package org.grouplens.lenskit.eval.results;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The row of result stored in a HashMap. Note that the values are string.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class ResultRow{
    private HashMap<String, String> row;


    public ResultRow() {
        row = new HashMap<String, String>();
    }

    public HashMap<String, String> getRow() {
        return row;
    }

}
