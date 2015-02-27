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
package org.grouplens.lenskit.cli

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import org.grouplens.lenskit.data.dao.packed.BinaryRatingDAO
import org.grouplens.lenskit.data.pref.PreferenceDomain
import org.grouplens.lenskit.data.source.PackedDataSource
import org.grouplens.lenskit.data.source.TextDataSource
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

class InputDataTest {
    static InputData parse(String... args) {
        def parser = ArgumentParsers.newArgumentParser("lenskit-input");
        InputData.configureArguments(parser, true)
        def options = parser.parseArgs(args)
        return new InputData(null, options)
    }

    @Test
    public void testNoFiles() {
        shouldFail(ArgumentParserException) {
            parse()
        }
    }

    @Test
    public void testCSVFile() {
        def data = parse('--csv-file', 'foo.csv')
        def input = data.source as TextDataSource
        assertThat(input.file.name, equalTo('foo.csv'))
        assertThat(input.format.delimiter, equalTo(','))
    }

    @Test
    public void testTSVFile() {
        def data = parse('--tsv-file', 'foo.tsv')
        def input = data.source as TextDataSource
        assertThat(input.file.name, equalTo('foo.tsv'))
        assertThat(input.format.delimiter, equalTo('\t'))
    }

    @Test
    public void testRatingFile() {
        def data = parse('--ratings-file', 'foo.tsv', '-d', '\t')
        def input = data.source as TextDataSource
        assertThat(input.file.name, equalTo('foo.tsv'))
        assertThat(input.format.delimiter, equalTo('\t'))
    }

    @Test
    public void testRatingFileOddDelim() {
        def data = parse('--ratings-file', 'ratings.dat', '-d', '::')
        def input = data.source as TextDataSource
        assertThat(input.file.name, equalTo('ratings.dat'))
        assertThat(input.format.delimiter, equalTo('::'))
    }

    @Test
    public void testPackFile() {
        def data = parse('--pack-file', 'ratings.pack')
        def input = data.source
        assertThat(input, instanceOf(PackedDataSource))
        def pack = input as PackedDataSource
        assertThat(pack.packedFile.name, equalTo('ratings.pack'))
    }

    @Test
    public void testDataSource() {
        def file = File.createTempFile("input", ".conf")
        file.text = """\
type: text
file: foo.tsv
delimiter: "\\t"
domain: {
  minimum: 0.5
  maximum: 5.0
  precision: 0.5
}
"""
        def data = parse('--data-source', file.absolutePath)
        def input = data.source as TextDataSource
        assertThat(input.file.name, equalTo('foo.tsv'))
        assertThat(input.format.delimiter, equalTo('\t'))
        assertThat(input.preferenceDomain, equalTo(PreferenceDomain.fromString("[0.5,5.0]/0.5")))
    }

    @Test
    public void testPackDataSource() {
        def file = File.createTempFile("input", ".conf")
        file.text = """\
type: pack
file: foo.pack
domain: {
  minimum: 0.5
  maximum: 5.0
  precision: 0.5
}
"""
        def data = parse('--data-source', file.absolutePath)
        def input = data.source as PackedDataSource
        assertThat(input.file.name, equalTo('foo.pack'))
        assertThat(input.eventDAO, instanceOf(BinaryRatingDAO))
        assertThat(input.preferenceDomain, equalTo(PreferenceDomain.fromString("[0.5,5.0]/0.5")))
    }
}
