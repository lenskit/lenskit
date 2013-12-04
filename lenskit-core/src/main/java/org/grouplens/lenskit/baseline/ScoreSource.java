package org.grouplens.lenskit.baseline;

import org.grouplens.lenskit.symbols.TypedSymbol;

/**
 * Enum expressing where a score came from in recommender that uses a baseline fallback.
 *
 * @since 2.1
 * @see FallbackItemScorer
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public enum ScoreSource {
    /**
     * The score came from the primary item scorer.
     */
    PRIMARY,
    /**
     * The score came from the baseline item scorer.
     */
    BASELINE;

    public static final TypedSymbol<ScoreSource> SYMBOL =
            TypedSymbol.of(ScoreSource.class, "org.grouplens.lenskit.baseline.ScoreSource");
}
