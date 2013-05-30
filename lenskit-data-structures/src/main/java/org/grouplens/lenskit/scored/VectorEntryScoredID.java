package org.grouplens.lenskit.scored;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import java.util.Set;

/**
 * Scored ID implementation backed by a sparse vector.
 *
 * @since 1.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class VectorEntryScoredID extends AbstractScoredId {

    private final SparseVector vector;
    private VectorEntry ent;

    public VectorEntryScoredID(SparseVector v) {
        vector = v;
    }

    @Override
    public long getId() {
        return ent.getKey();
    }

    @Override
    public double getScore() {
        return ent.getValue();
    }

    @Override
    public Set<Symbol> getChannels() {
        ReferenceArraySet<Symbol> res = new ReferenceArraySet<Symbol>();
        for (Symbol s: vector.getChannels()) {
            // FIXME Make this O(1)
            if (vector.channel(s).containsKey(ent.getKey())) {
                res.add(s);
            }
        }
        return res;
    }

    @Override
    public double channel(Symbol s) {
        return vector.channel(s).get(ent);
    }

    @Override
    public boolean hasChannel(Symbol s) {
        return vector.hasChannel(s) && vector.channel(s).containsKey(ent.getKey());
    }

    public void setEntry(VectorEntry e) {
        Preconditions.checkArgument(e.getVector() == vector, "entry must be associated with vector");
        ent = e;
    }
}
