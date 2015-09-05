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
package org.grouplens.lenskit.eval.data

import org.lenskit.data.ratings.PreferenceDomain
import org.grouplens.lenskit.data.source.CSVDataSourceBuilder
import org.grouplens.lenskit.data.source.TextDataSource
import org.grouplens.lenskit.eval.script.ConfigTestBase
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class CSVFileConfigTest extends ConfigTestBase {
    @Test
    void testBuilderAvailable() {
        def cmdClass = engine.lookupMethod("csvfile")
        assertThat(cmdClass, notNullValue())
        assertThat(cmdClass, equalTo(CSVDataSourceBuilder))
    }

    @Test
    void testBasic() {
        def source = eval {
            csvfile("ml-100k.dat")
        } as TextDataSource
        assertThat(source, notNullValue())
        assertThat(source.name, equalTo("ml-100k.dat"))
        assertThat(source.sourceFile, equalTo(new File("ml-100k.dat")))
        assertThat(source.format.delimiter, equalTo(","))
    }

    @Test
    void testFileDelim() {
        def source = eval {
            csvfile("ml-100k") {
                file "ml-100k/u.data"
                delimiter "::"
            }
        } as TextDataSource
        assertThat(source.name, equalTo("ml-100k"))
        assertThat(source.sourceFile, equalTo(new File("ml-100k/u.data")))
        assertThat(source.format.delimiter, equalTo("::"))
    }

    @Test
    void testFileNoName() {
        def source = eval {
            csvfile {
                file "ml-100k.dat"
            }
        } as TextDataSource
        assertThat(source.name, equalTo("ml-100k.dat"))
        assertThat(source.sourceFile, equalTo(new File("ml-100k.dat")))
    }

    @Test
    void testDomain() {
        def source = eval {
            csvfile("ml-100k") {
                domain {
                    minimum 1.0
                    maximum 5.0
                    precision 1.0
                }
            }
        } as TextDataSource
        assertThat(source.domain, equalTo(new PreferenceDomain(1.0, 5.0, 1.0)))
    }

    /**
     * Can we use {@link GString}s?
     */
    @Test
    void testGString() {
        def name = "ml-100k"
        def source = eval {
            csvfile(name) {
                file "${name}.csv"
            }
        } as TextDataSource
        assertThat(source.name, equalTo("ml-100k"))
        assertThat(source.sourceFile, equalTo(new File("ml-100k.csv")))
    }
}
