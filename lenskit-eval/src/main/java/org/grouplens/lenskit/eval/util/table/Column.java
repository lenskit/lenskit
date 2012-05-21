package org.grouplens.lenskit.eval.util.table;

import java.util.List;

/**
 * List interface provides the basic functions such as sum and average on the column data.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public interface Column extends List<Object> {
    Double sum();
    Double average();
}
