/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.cli

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.data.ratings.PreferenceDomain

import org.junit.Test
import org.lenskit.cli.util.InputData

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
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(),
                   equalTo('foo.csv'))
        assertThat(input.sources[0].format.delimiter, equalTo(','))
    }

    @Test
    public void testTSVFile() {
        def data = parse('--tsv-file', 'foo.tsv')
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(), equalTo('foo.tsv'))
        assertThat(input.sources[0].format.delimiter, equalTo('\t'))
    }

    @Test
    public void testRatingFile() {
        def data = parse('--ratings-file', 'foo.tsv', '-d', '\t')
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(), equalTo('foo.tsv'))
        assertThat(input.sources[0].format.delimiter, equalTo('\t'))
    }

    @Test
    public void testRatingFileOddDelim() {
        def data = parse('--ratings-file', 'ratings.dat', '-d', '::')
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(), equalTo('ratings.dat'))
        assertThat(input.sources[0].format.delimiter, equalTo('::'))
    }

    @Test
    public void testDataSource() {
        def file = File.createTempFile("input", ".json")
        file.text = """{
  "file": "foo.tsv",
  "delimiter": "\\t",
  "metadata": {
  "domain": {
    "minimum": 0.5,
    "maximum": 5.0,
    "precision": 0.5
  }
  }
}"""
        def data = parse('--data-source', file.absolutePath)
        assertThat(data.source, instanceOf(StaticDataSource))
        def input = data.source as StaticDataSource
        assertThat(input.sources[0].file.fileName.toString(), equalTo('foo.tsv'))
        assertThat(input.sources[0].format.delimiter, equalTo('\t'))
        assertThat(input.preferenceDomain, equalTo(PreferenceDomain.fromString("[0.5,5.0]/0.5")))
    }
}
