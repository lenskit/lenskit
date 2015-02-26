/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.data.source

import org.grouplens.lenskit.data.pref.PreferenceDomain
import org.grouplens.lenskit.specs.SpecificationContext
import org.grouplens.lenskit.util.test.MiscBuilders
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TextDataSourceSpecHandlerTest {
    def context = SpecificationContext.create()

    @Test
    public void testCSVFile() {
        def cfg = MiscBuilders.configObj {
            type "csv"
            file "ratings.csv"
        }
        def src = context.build(DataSource, cfg)
        assertThat src, instanceOf(TextDataSource)
        src = src as TextDataSource
        assertThat src.format.delimiter, equalTo(",")
        assertThat src.file.name, equalTo("ratings.csv")
        assertThat src.domain, nullValue()
    }

    @Test
    public void testTSVFile() {
        def cfg = MiscBuilders.configObj {
            type "tsv"
            file "ratings.tsv"
        }
        def src = context.build(DataSource, cfg)
        assertThat src, instanceOf(TextDataSource)
        src = src as TextDataSource
        assertThat src.format.delimiter, equalTo("\t")
        assertThat src.file.name, equalTo("ratings.tsv")
        assertThat src.domain, nullValue()
    }

    @Test
    public void testMLFile() {
        def cfg = MiscBuilders.configObj {
            type "text"
            file "ratings.dat"
            delimiter "::"
        }
        def src = context.build(DataSource, cfg)
        assertThat src, instanceOf(TextDataSource)
        src = src as TextDataSource
        assertThat src.format.delimiter, equalTo("::")
        assertThat src.file.name, equalTo("ratings.dat")
        assertThat src.domain, nullValue()
    }

    @Test
    public void testMLFileWithDomain() {
        def src = SpecificationContext.buildWithHandler(DataSourceSpecHandler,
                                                        getClass().getResource("csvsource.conf").toURI())
        assertThat src, instanceOf(TextDataSource)
        src = src as TextDataSource
        assertThat src.format.delimiter, equalTo("::")
        assertThat src.file.name, equalTo("ratings.dat")
        assertThat src.domain, equalTo(PreferenceDomain.fromString("[1.0,5.0]/1.0"))
    }
}
