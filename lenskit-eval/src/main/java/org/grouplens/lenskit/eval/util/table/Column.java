package org.grouplens.lenskit.eval.util.table;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: schang
 * Date: 5/14/12
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Column extends List<Object> {
    Double sum();
    Double average();
    Object[] values();
}
