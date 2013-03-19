package org.grouplens.lenskit.util.table;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.util.tablewriter.TableLayout;
import org.grouplens.lenskit.util.tablewriter.TableLayoutBuilder;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to construct tables.
 *
 * @author Shuo Chang
 * @author Michael Ekstrand
 */
public class TableBuilder implements Builder<Table>, TableWriter {
    private static final Logger logger = LoggerFactory.getLogger(TableBuilder.class);
    private final TableLayout layout;
    private final Object2IntMap<String> headerIndex;
    private final List<Row> rows;

    /**
     * Construct a new builder using a particular layout.
     *
     * @param layout The table layout.
     */
    public TableBuilder(TableLayout layout) {
        this.layout = layout;
        rows = new ArrayList<Row>();
        headerIndex = indexHeaders();
    }

    public TableBuilder(List<String> columns) {
        TableLayoutBuilder bld = new TableLayoutBuilder();
        for (String col: columns) {
            bld.addColumn(col);
        }
        layout = bld.build();

        rows = new ArrayList<Row>();
        headerIndex = indexHeaders();
    }

    private Object2IntMap<String> indexHeaders() {
        Object2IntMap<String> idx = new Object2IntOpenHashMap<String>();
        int i = 0;
        for (String col: layout.getColumnHeaders()) {
            if (idx.containsKey(col)) {
                logger.warn("duplicate column {}", col);
            } else {
                idx.put(col, i);
                i++;
            }
        }
        idx.defaultReturnValue(-1);
        return idx;
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    @Override
    public void close() {}


    @Override
    public void writeRow(Object[] row) {
        addRow(row);
    }

    /**
     * Add a row to the table.
     *
     * @param row The row to add.
     */
    public synchronized void addRow(Object[] row) {
        rows.add(new RowImpl(headerIndex, row));
    }

    public Table build() {
        return new TableImpl(layout, headerIndex, rows);
    }
}
