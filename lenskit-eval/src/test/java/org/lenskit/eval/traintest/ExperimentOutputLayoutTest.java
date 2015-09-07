package org.lenskit.eval.traintest;

import com.google.common.collect.ImmutableSet;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.source.GenericDataSource;
import org.grouplens.lenskit.util.table.*;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.junit.Test;

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
                .setTrain(new GenericDataSource("train", EventCollectionDAO.empty()))
                .setTest(new GenericDataSource("test", EventCollectionDAO.empty()))
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
                .setTrain(new GenericDataSource("train", EventCollectionDAO.empty()))
                .setTest(new GenericDataSource("test", EventCollectionDAO.empty()))
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
