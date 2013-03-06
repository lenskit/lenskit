package org.grouplens.lenskit.ids;

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import org.grouplens.lenskit.symbols.Symbol;

public class ScoredId {

    protected long id;
    protected double score;
    protected Reference2DoubleMap<Symbol> channelMap;

    public ScoredId(long id, double score) {
        this.id = id;
        this.score = score;
        channelMap = null;
    }

    public long getId() {
        return id;
    }

    public double getScore() {
        return score;
    }

    public boolean hasChannel(Symbol s) {
        return channelMap != null && channelMap.containsKey(s);
    }

    public double addChannel(Symbol s, double val) {
        if (channelMap == null) {
            channelMap = new Reference2DoubleArrayMap<Symbol>();
        } else if (hasChannel(s)) {
            throw new IllegalArgumentException("Channel already exists under name " + s.getName());
        }
        return channelMap.put(s, val);
    }

    public double alwaysAddChannel(Symbol s, double val) {
        if (channelMap == null) {
            channelMap = new Reference2DoubleArrayMap<Symbol>();
        }
        return channelMap.put(s,val);
    }

    public double channel(Symbol s) {
        if (hasChannel(s)) {
            return channelMap.get(s);
        }
        throw new IllegalArgumentException("No existing channel under name " + s.getName());
    }

    public double removeChannel(Symbol s) {
        if (hasChannel(s)) {
            return channelMap.remove(s);
        }
        throw new IllegalArgumentException("No existing channel under name " + s.getName());
    }
}
