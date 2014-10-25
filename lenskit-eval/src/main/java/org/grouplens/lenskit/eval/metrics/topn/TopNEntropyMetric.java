/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval.metrics.topn;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;

import java.util.Collections;
import java.util.List;

/**
 * Metric that measures the entropy of the top N recommendations across all users.
 * 
 * This tell us essentially how large of a range of the items your recommender is covering.
 * 
 * Small values indicate that the algorithm tends to prefer a small number of items which it recomments
 * to all users. Large values mean that the algorithm recommends many different items (to many different 
 * users) 
 * 
 * The smallest value happens when the topN list is the same for all users (which would give an entropy
 * of roughly log_2(N)). The largest value happens when each item is recommended the same number of times
 * (for an entropy of roughly log_2(number of items)).
 * 
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TopNEntropyMetric extends AbstractMetric<TopNEntropyMetric.Context, TopNEntropyMetric.Result, Void> {
    
    private final String prefix;
    private final String suffix;
    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    
    public TopNEntropyMetric(String pre, String sfx, int listSize, ItemSelector candidates, ItemSelector exclude) {
        super(Result.class, Void.TYPE);
        prefix = pre;
        suffix = sfx;
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
    }

    @Override
    protected String getPrefix() {
        return prefix;
    }

    @Override
    protected String getSuffix() {
        return suffix;
    }

    @Override
    public Context createContext(Attributed algo, TTDataSet ds, Recommender rec) {
        return new Context();
    }

    @Override
    public List<String> getUserColumnLabels() {
        return Collections.emptyList();
    }

    @Override
    public Void doMeasureUser(TestUser user, Context context) {
        List<ScoredId> recs;
        recs = user.getRecommendations(listSize, candidates, exclude);
        if (recs != null) {
            context.addUser(recs);
        }
        return null;
    }

    @Override
    protected Result getTypedResults(Context context) {
        return context.finish();
    }

    public static class Result {
        @ResultColumn("TopN.Entropy")
        public final double entropy;
        public Result(double e) {
            entropy = e;
        }
    }

    public class Context {
        private Long2IntMap counts = new Long2IntOpenHashMap();
        private int recCount = 0;
        
        private void addUser(List<ScoredId> recs) {
            for (ScoredId s: recs) {
                counts.put(s.getId(), counts.get(s.getId()) +1);
                recCount +=1;
            }
        }

        public Result finish() {
            if (recCount > 0) {
                double entropy = 0;
                for (Long2IntMap.Entry e : counts.long2IntEntrySet()) {
                    double p = (double) e.getIntValue()/ recCount;
                    entropy -= p*Math.log(p)/Math.log(2);
                }
                return new Result(entropy);
            } else {
                return null;
            }
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<TopNEntropyMetric> {
        @Override
        public TopNEntropyMetric build() {
            return new TopNEntropyMetric(prefix, suffix, listSize, candidates, exclude);
        }
    }

}
