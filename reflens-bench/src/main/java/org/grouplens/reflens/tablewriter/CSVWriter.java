/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.tablewriter;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link TableWriter} for CSV files.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CSVWriter implements TableWriter {
	private Writer writer;
	private final String[] columns;
	private String[] values;
	
	CSVWriter(Writer w, String[] cnames) throws IOException {
		writer = w;
		columns = cnames;
		writeRow(new ObjectArrayList<String>(cnames));
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriter#finish()
	 */
	@Override
	public void finish() throws IOException {
		if (values != null)
			finishRow();
		writer.close();
		writer = null;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriter#finishRow()
	 */
	@Override
	public void finishRow() throws IOException {
		requireRow();
		for (int i = 0; i < columns.length; i++) {
			if (i > 0) writer.write(',');
			if (values[i] != null) writer.write(values[i]);
		}
		writer.write('\n');
		writer.flush();
		values = null;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriter#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columns.length;
	}
	
	private void requireRow() {
		if (values == null)
			values = new String[columns.length];
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriter#setValue(int, long)
	 */
	@Override
	public void setValue(int col, long val) {
		requireRow();
		values[col] = Long.toString(val);
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriter#setValue(int, double)
	 */
	@Override
	public void setValue(int col, double val) {
		requireRow();
		values[col] = Double.toString(val);
	}
	
	private String quote(String e) {
		if (e == null)
			return "";
		
		if (e.matches("[\r\n,\"]")) {
			return "\"" + e.replaceAll("\"", "\"\"") + "\"";
		} else {
			return e;
		}
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriter#setValue(int, java.lang.String)
	 */
	@Override
	public void setValue(int col, String val) {
		requireRow();
		values[col] = quote(val);
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriter#writeRow(java.util.List)
	 */
	@Override
	public void writeRow(List<String> row) throws IOException {
		if (row.size() > columns.length)
			throw new RuntimeException("row too long");
		if (values != null)
			finishRow();
		int i = 0;
		for (String s: row) {
			setValue(i, s);
			i++;
		}
		finishRow();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.tablewriter.TableWriter#writeRow(java.util.Map)
	 */
	@Override
	public <V> void writeRow(Map<String, V> data) throws IOException {
		if (values != null)
			finishRow();
		for (int i = 0; i < columns.length; i++) {
			V v = data.get(columns[i]);
			if (v != null)
				setValue(i, v.toString());
		}
		finishRow();
	}
	
	@Override
	public void writeRow(Object... columns) throws IOException {
		List<String> cols = new ArrayList<String>(columns.length);
		for (int i = 0; i < columns.length; i++) {
			Object o = columns[i];
			cols.add(o == null ? null : o.toString());
		}
		writeRow(cols);
	}

}
