package org.grouplens.lenskit.ids;

import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.symbols.Symbol;

import java.util.Comparator;

public class ScoredId {

    protected long id;
    protected double score;
    protected Reference2DoubleMap<Symbol> channelMap;

    public static final Comparator<ScoredId> DESCENDING_SCORE_COMPARATOR = new Comparator<ScoredId>() {
        @Override
        public int compare(ScoredId o1, ScoredId o2) {
            return DoubleComparators.OPPOSITE_COMPARATOR.compare(o1.getScore(), o2.getScore());
        }
    };

    public long getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    public boolean hasChannel(Symbol s) {
        return channelMap != null && channelMap.containsKey(s);
    }

    public double channel(Symbol s) {
        if (hasChannel(s)) {
            return channelMap.get(s);
        }
        throw new IllegalArgumentException("No existing channel under name " + s.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ScoredId) {
            ScoredId oid = (ScoredId) o;
            return getId() == oid.getId() && getScore() == oid.getScore();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(score)
                .toHashCode();
    }

    public static class Builder implements org.apache.commons.lang3.builder.Builder {

        private ScoredId sid;

        public Builder(long id, double score) {
            sid = new ScoredId();
            sid.id = id;
            sid.score = score;
        }

        public Builder addChannel(Symbol s, double value) {
            if (sid.channelMap == null) {
                sid.channelMap = new Reference2DoubleArrayMap<Symbol>();
            }
            sid.channelMap.put(s, value);
            return this;
        }

        @Override
        public ScoredId build() {
            return sid;
        }
    }
}