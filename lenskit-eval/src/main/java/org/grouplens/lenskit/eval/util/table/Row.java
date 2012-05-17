package org.grouplens.lenskit.eval.util.table;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: schang
 * Date: 4/24/12
 * Time: 9:34 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Row extends Map<String, Object>{
    Object value(String key);
    Object value(int idx);
}
