package org.lenskit.results;

import it.unimi.dsi.fastutil.longs.LongList;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

/**
 * LensKit refinement of the general result list type.
 * @param <E> The result type.
 */
public interface LenskitResultList<E extends Result> extends ResultList<E> {
    LongList idList();
}
