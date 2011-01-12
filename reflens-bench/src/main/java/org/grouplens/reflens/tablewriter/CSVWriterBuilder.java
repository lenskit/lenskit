package org.grouplens.reflens.tablewriter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Write tables as CSV files.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CSVWriterBuilder implements TableWriterBuilder {
	private List<String> columns;
	
	public CSVWriterBuilder() {
		columns = new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriterBuilder#addColumn(java.lang.String)
	 */
	@Override
	public int addColumn(String name) {
		columns.add(name);
		return columns.size() - 1;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriterBuilder#makeWriter(java.io.Writer)
	 */
	@Override
	public CSVWriter makeWriter(Writer output) throws IOException {
		return new CSVWriter(output, columns.toArray(new String[columns.size()]));
	}

}
