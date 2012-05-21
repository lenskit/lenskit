package org.grouplens.lenskit.eval.util.table;

import java.util.List;

/**
 * This is the interface for the in memory table which stores a list of rows. Users should be able
 * to call the filter method to find the rows that satisfy the conditions specified by users. And
 * table expose the functions of columns to enable users calling the functions on column.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public interface Table extends List<Row> {
    Table filter(String header, Object data);
    Column column(String col);
    List<String> getHeader();
}
