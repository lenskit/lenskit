package org.grouplens.lenskit.util.tablewriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		ArrayList<IOException> closeExceptions = new ArrayList<IOException>(writers.size());
		for (TableWriter w : writers) {
			try {
				w.close();
			} catch (IOException e) {
				closeExceptions.add(e);
			}
		}
		if (!closeExceptions.isEmpty()) {
			throw closeExceptions.get(0);
		}
	}
}