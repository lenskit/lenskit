package org.lenskit.results;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;

/**
 * LensKit refinement of the general result map type.
 * @param <E> The result type.
 */
public interface LenskitResultMap<E extends Result> extends ResultMap<E>, Long2ObjectMap<E> {
    Long2DoubleMap scoreMap();
}
