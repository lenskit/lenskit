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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.util.table.TableLayoutBuilder;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Predict metric that writes predictions to a file.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class OutputPredictMetric extends AbstractMetric<OutputPredictMetric.Context, Void, Void> {
    private static final Logger logger = LoggerFactory.getLogger(OutputPredictMetric.class);

    private final ExperimentOutputLayout outputLayout;
    private final TableWriter tableWriter;
    private final List<Pair<Symbol, String>> channels;

    public OutputPredictMetric(ExperimentOutputLayout layout, File file,
                               List<Pair<Symbol, String>> chans) throws IOException {
        super(Void.TYPE, Void.TYPE);
        outputLayout = layout;
        channels = chans;

        TableLayoutBuilder lb = TableLayoutBuilder.copy(layout.getCommonLayout())
                                                  .addColumn("User")
                                                  .addColumn("Item")
                                                  .addColumn("Rating")
                                                  .addColumn("Prediction");
        for (Pair<Symbol, String> chan: channels) {
            lb.addColumn(chan.getRight());
        }

        tableWriter = CSVWriter.open(file, lb.build());
    }

    @Override
    public Context createContext(Attributed algo, TTDataSet ds, Recommender rec) {
        return new Context(outputLayout.prefixTable(tableWriter, algo, ds));
    }

    @Override
    public Void doMeasureUser(TestUser user, Context context) {
        SparseVector ratings = user.getTestRatings();
        SparseVector predictions = user.getPredictions();
        if (predictions == null) {
            predictions = MutableSparseVector.create();
        }

        LongSortedSet items = ratings.keySet();
        if (!items.containsAll(predictions.keySet())) {
            items = LongUtils.setUnion(items, predictions.keySet());
        }

        logger.debug("outputting {} predictions for user {}", predictions.size(), user.getUserId());
        for (LongIterator iter = items.iterator(); iter.hasNext(); /* no increment */) {
            long item = iter.nextLong();
            Double rating = ratings.containsKey(item) ? ratings.get(item) : null;
            Double pred = predictions.containsKey(item) ? predictions.get(item) : null;
            try {
                List<Object> row = Lists.<Object>newArrayList(user.getUserId(), item, rating, pred);
                for (Pair<Symbol, String> chan: channels) {
                    SparseVector v = predictions.getChannelVector(chan.getLeft());
                    if (v != null && v.containsKey(item)) {
                        row.add(v.get(item));
                    } else {
                        row.add(null);
                    }
                }
                context.writer.writeRow(row);
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
        return null;
    }

    @Override
    protected Void getTypedResults(Context context) {
        return null;
    }

    @Override
    public void close() throws IOException {
        tableWriter.close();
    }

    public static class Context {
        private final TableWriter writer;

        Context(TableWriter tw) {
            writer = tw;
        }
    }

    public static class Factory extends MetricFactory<Context> {
        private final List<Pair<Symbol, String>> predictChannels;
        private final File file;

        public Factory(File f, List<Pair<Symbol, String>> pchans) {
            file = f;
            predictChannels = ImmutableList.copyOf(pchans);
        }

        @Override
        public OutputPredictMetric createMetric(TrainTestEvalTask task) throws IOException {
            return new OutputPredictMetric(task.getOutputLayout(), file, predictChannels);
        }

        @Override
        public List<String> getColumnLabels() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getUserColumnLabels() {
            return Collections.emptyList();
        }
    }

    /**
     * Configure the prediction output.
     */
    public static class FactoryBuilder implements Builder<Factory> {
        private File file;
        private List<Pair<Symbol,String>> channels = Lists.newLinkedList();

        public File getFile() {
            return file;
        }

        public void setFile(File f) {
            file = f;
        }

        public void setFile(String fn) {
            setFile(new File(fn));
        }

        public void addChannel(Symbol chan, String col) {
            channels.add(Pair.of(chan, col));
        }

        public void addChannel(String chan, String col) {
            channels.add(Pair.of(Symbol.of(chan), col));
        }

        public List<Pair<Symbol,String>> getChannels() {
            return Collections.unmodifiableList(channels);
        }

        @Override
        public Factory build() {
            if (file == null) {
                throw new IllegalStateException("no file specified");
            }
            return new Factory(file, channels);
        }
    }
}
