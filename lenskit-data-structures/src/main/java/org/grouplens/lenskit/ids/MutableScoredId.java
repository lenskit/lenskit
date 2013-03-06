package org.grouplens.lenskit.ids;

import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import org.grouplens.lenskit.symbols.Symbol;

/**
 * WARNING: This class should not be used under any circumstances, except to
 * implement fast iteration. We make no guarantees about the functionality of
 * this class in any other situation.
 */
public class MutableScoredId extends ScoredId {

    public MutableScoredId(long id, double score) {
        super(id, score);
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setChannelMap(Reference2DoubleMap<Symbol> channelMap) {
        this.channelMap = channelMap;
    }
}
