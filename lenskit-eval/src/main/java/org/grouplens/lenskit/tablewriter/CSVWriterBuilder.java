/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.tablewriter;

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
     * @see org.grouplens.lenskit.tablewriter.TableWriterBuilder#addColumn(java.lang.String)
     */
    @Override
    public int addColumn(String name) {
        columns.add(name);
        return columns.size() - 1;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.tablewriter.TableWriterBuilder#makeWriter(java.io.Writer)
     */
    @Override
    public CSVWriter makeWriter(Writer output) throws IOException {
        return new CSVWriter(output, columns.toArray(new String[columns.size()]));
    }

}
