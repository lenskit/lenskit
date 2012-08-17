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
package org.grouplens.lenskit.eval.util.table;

import java.util.List;

/**
 * This is the interface for the in memory table which stores a list of rows. Users should be able
 * to call the filter method to find the rows that satisfy the conditions specified by users. And
 * table expose the functions of columns to enable users calling the functions on column.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public interface Table extends List<Row> {
    Table filter(String header, Object data);

    Column column(String col);

    List<String> getHeader();
}
