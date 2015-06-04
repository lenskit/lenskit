package org.lenskit.results;

import org.lenskit.api.Result;

/**
 * Utility functions for working with results.
 */
public final class Results {
    private Results() {}

    /**
     * Create a new result with just and ID and a score.
     * @param id The ID.
     * @param score The score.
     * @return The result instance.
     */
    public static BasicResult create(long id, double score) {
        return new BasicResult(id, score);
    }

    /**
     * Create a basic result that has the same ID and score as another result (a basic copy of the result).
     *
     * @param r The result to copy.
     * @return A basic result with the same ID and score as `r`.  This may be the same object as `r`, since basic
     * results are immutable.
     */
    public static BasicResult basicCopy(Result r) {
        if (r instanceof BasicResult) {
            return (BasicResult) r;
        } else {
            return create(r.getId(), r.getScore());
        }
    }
}
