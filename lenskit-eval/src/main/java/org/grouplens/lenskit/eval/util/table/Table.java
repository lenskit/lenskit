package org.grouplens.lenskit.eval.util.table;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: schang
 * Date: 4/24/12
 * Time: 9:34 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Table extends List<Row> {
    Table filter(String header, Object data);
    Column column(String col);
}
