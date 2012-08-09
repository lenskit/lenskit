package org.grouplens.lenskit.util.tablewriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.grouplens.lenskit.eval.util.table.TableImpl;

public class MultiplexedTableWriter implements TableWriter {

	private TableLayout layout;
	private List<TableWriter> writers;
	
	public MultiplexedTableWriter(TableLayout layout, List<TableWriter> writers) {
		this.layout = layout;
		this.writers = writers;
	}
	
	public MultiplexedTableWriter(TableLayout layout, TableWriter... writers) {
		this(layout, Arrays.asList(writers));
	}
		
	@Override
	public TableLayout getLayout() {
		return layout;
	}

	@Override
	public void writeRow(Object[] row) throws IOException {
		for (TableWriter w: writers) {
			w.writeRow(row);
		}

	}

	@Override
	public void close() throws IOException {
		for (TableWriter w : writers) {
			w.close();
		}
	}
	
	public TableImpl getResult() {
		for (TableWriter writer : writers) {
			if (writer instanceof InMemoryWriter) {
				return ((InMemoryWriter) writer).getResult();
			}
		}
		return null;
	}
}