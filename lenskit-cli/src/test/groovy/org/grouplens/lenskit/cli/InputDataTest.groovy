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
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

class InputDataTest {
    static InputData parse(String... args) {
        def parser = ArgumentParsers.newArgumentParser("lenskit-input");
        InputData.configureArguments(parser)
        def options = parser.parseArgs(args)
        return new InputData(options)
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
        def input = data.source as InputData.TextInput
        assertThat(input.inputFile.name, equalTo('foo.csv'))
        assertThat(input.delimiter, equalTo(','))
    }

    @Test
    public void testTSVFile() {
        def data = parse('--tsv-file', 'foo.tsv')
        def input = data.source as InputData.TextInput
        assertThat(input.inputFile.name, equalTo('foo.tsv'))
        assertThat(input.delimiter, equalTo('\t'))
    }

    @Test
    public void testRatingFile() {
        def data = parse('--ratings-file', 'foo.tsv', '-d', '\t')
        def input = data.source as InputData.TextInput
        assertThat(input.inputFile.name, equalTo('foo.tsv'))
        assertThat(input.delimiter, equalTo('\t'))
    }

    @Test
    public void testRatingFileOddDelim() {
        def data = parse('--ratings-file', 'ratings.dat', '-d', '::')
        def input = data.source as InputData.TextInput
        assertThat(input.inputFile.name, equalTo('ratings.dat'))
        assertThat(input.delimiter, equalTo('::'))
    }

    @Test
    public void testPackFile() {
        def data = parse('--pack-file', 'ratings.pack')
        def input = data.source
        assertThat(input, instanceOf(InputData.PackedInput))
        def pack = input as InputData.PackedInput
        assertThat(pack.inputFile.name, equalTo('ratings.pack'))
    }
}
