package org.grouplens.lenskit.eval;

import java.util.Map;

/**
 * Interface for objects with names and attributes.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface Attributed {
    String getName();
    Map<String,Object> getAttributes();
}
