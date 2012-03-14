/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util.tablewriter;

import java.util.Collections;
import java.util.List;

/**
 * A layout for a table to be written. Specifies the columns in the table.
 * @author Michael Ekstrand
 * @since 0.10
 */
public class TableLayout {
    private List<String> columnHeaders;

    TableLayout(List<String> headers) {
        columnHeaders = headers;
    }

    /**
     * Get the headers of the columns.
     * @return The headers of the columns in the table layout.
     */
    public List<String> getColumnHeaders() {
        return Collections.unmodifiableList(columnHeaders);
    }

    /**
     * Get the number of columns in this layout.
     * @return The number of columns in the table layout.
     */
    public int getColumnCount() {
        return columnHeaders.size();
    }
}
