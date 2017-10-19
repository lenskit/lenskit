/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.eval.traintest;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.util.table.*;
import org.lenskit.util.table.writer.TableWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ExperimentOutputLayoutTest {
    @Test
    public void testSingleton() {
        Set<String> dcols = Collections.singleton("DataSet");
        Set<String> acols = Collections.singleton("Algorithm");
        ExperimentOutputLayout layout = new ExperimentOutputLayout(dcols, acols);
        assertThat(layout.getConditionLayout(), notNullValue());
        assertThat(layout.getConditionLayout().getColumns(),
                   contains("DataSet", "Algorithm"));
        assertThat(layout.getConditionColumnCount(), equalTo(2));
    }

    @Test
    public void testAttributes() {
        Set<String> dcols = ImmutableSet.of("DataSet", "Partition");
        Set<String> acols = ImmutableSet.of("Algorithm", "NeighborhoodSize");
        ExperimentOutputLayout layout = new ExperimentOutputLayout(dcols, acols);
        assertThat(layout.getConditionLayout(), notNullValue());
        assertThat(layout.getConditionLayout().getColumns(),
                   contains("DataSet", "Partition", "Algorithm", "NeighborhoodSize"));
        assertThat(layout.getConditionColumnCount(), equalTo(4));
    }

    @Test
    public void testPrefixNullTable() {
        Set<String> dcols = Collections.singleton("DataSet");
        Set<String> acols = Collections.singleton("Algorithm");
        ExperimentOutputLayout layout = new ExperimentOutputLayout(dcols, acols);
        AlgorithmInstance ai = new AlgorithmInstanceBuilder("Wombat").build();
        DataSet ds = new DataSetBuilder("Wumpus")
                .setTrain(new StaticDataSource("train"))
                .setTest(new StaticDataSource("test"))
                .build();
        TableWriter tw = layout.prefixTable(null, ds, ai);
        assertThat(tw, nullValue());
    }

    @Test
    public void testPrefixTable() throws IOException {
        Set<String> dcols = Collections.singleton("DataSet");
        Set<String> acols = Collections.singleton("Algorithm");
        ExperimentOutputLayout layout = new ExperimentOutputLayout(dcols, acols);
        AlgorithmInstance ai = new AlgorithmInstanceBuilder("Wombat").build();
        DataSet ds = new DataSetBuilder("Wumpus")
                .setTrain(new StaticDataSource("train"))
                .setTest(new StaticDataSource("test"))
                .build();

        TableLayoutBuilder tlb = TableLayoutBuilder.copy(layout.getConditionLayout());
        tlb.addColumn("Data");
        TableLayout tl = tlb.build();
        TableBuilder tw = new TableBuilder(tl);

        TableWriter prefixed = layout.prefixTable(tw, ds, ai);
        assertThat(tw, notNullValue());
        prefixed.writeRow(38);

        prefixed.close();

        Table result = tw.build();
        assertThat(result, hasSize(1));
        Row row = result.get(0);
        assertThat(row.value("DataSet"), equalTo((Object) "Wumpus"));
        assertThat(row.value("Algorithm"), equalTo((Object) "Wombat"));
        assertThat(row.value("Data"), equalTo((Object) 38));
    }
}
