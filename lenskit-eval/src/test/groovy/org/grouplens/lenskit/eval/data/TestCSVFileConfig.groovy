/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.junit.Test

import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.grouplens.lenskit.data.pref.PreferenceDomain

/**
 * @author Michael Ekstrand
 */
class TestCSVFileConfig extends ConfigTestBase {
    @Test
    void testBuilderAvailable() {
        def cmdClass = engine.getCommand("csvfile")
        assertThat(cmdClass, notNullValue())
        assertThat(cmdClass, equalTo(CSVDataSourceCommand))
    }

    @Test
    void testBasic() {
        def source = eval {
            csvfile("ml-100k.dat")
        }
        assertThat(source, notNullValue())
        assertThat(source.name, equalTo("ml-100k.dat"))
        assertThat(source.file, equalTo(new File("ml-100k.dat")))
        assertThat(source.delimiter, equalTo(","))
    }

    @Test
    void testFileDelim() {
        def source = eval {
            csvfile("ml-100k") {
                file "ml-100k/u.data"
                delimiter "::"
            }
        }
        assertThat(source.name, equalTo("ml-100k"))
        assertThat(source.file, equalTo(new File("ml-100k/u.data")))
        assertThat(source.delimiter, equalTo("::"))
    }

    @Test
    void testFileNoName() {
        def source = eval {
            csvfile {
                file "ml-100k.dat"
            }
        }
        assertThat(source.name, equalTo("ml-100k.dat"))
        assertThat(source.file, equalTo(new File("ml-100k.dat")))
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
        }
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
        }
        assertThat(source.name, equalTo("ml-100k"))
        assertThat(source.file, equalTo(new File("ml-100k.csv")))
    }
}
