package org.grouplens.lenskit.eval.util.table;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Row stores a data row. The user can use it as a map of header string to data object. Internally
 * it keeps the order of the data by using both a map of header string to index number and a list
 * of dat aobject.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public interface Row extends Map<String, Object>{
    Object value(String key);
    Object value(int idx);
}
