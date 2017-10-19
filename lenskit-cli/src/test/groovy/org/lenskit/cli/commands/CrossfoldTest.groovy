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
package org.lenskit.cli.commands

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.junit.Before
import org.junit.Test
import org.lenskit.data.output.OutputFormat
import org.lenskit.eval.crossfold.*

import java.nio.file.Paths

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat

public class CrossfoldTest {
    ArgumentParser parser
    Crossfold command

    @Before
    public void initialize() {
        parser = ArgumentParsers.newArgumentParser("crossfold")
        command = new Crossfold()
        command.configureArguments(parser)
    }

    @Test
    public void testDefaults() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdout(10))))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigureUserPartition() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--partition-users"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdout(10))))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigureUserSample() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--sample-users"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.sampleUsers(SortOrder.RANDOM, HistoryPartitions.holdout(10), 1000)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigureItemPartition() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--partition-items"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionItems(HistoryPartitions.holdout(10))))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigureItemSample() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--sample-items"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.sampleItems(HistoryPartitions.holdout(10), 1000)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigureSampleSize() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--sample-users", "--sample-size", 5000]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.sampleUsers(SortOrder.RANDOM, HistoryPartitions.holdout(10), 5000)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testPartitionRatings() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--partition-ratings"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method, instanceOf(EntityPartitionCrossfoldMethod))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testUserHoldout() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--holdout-count", 5]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)
        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdout(5))))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testUserRetain() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--retain", "5"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.retain(5))))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testHoldoutFraction() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--holdout-fraction", "0.2"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdoutFraction(0.2))))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testTimestampOrder() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--timestamp-order"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionUsers(SortOrder.TIMESTAMP, HistoryPartitions.holdout(10))))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigurePartitions() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--partition-count=10"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdout(10))))
        assertThat(cf.partitionCount, equalTo(10))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testCompressOutput() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--gzip-output"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method,
                equalTo(CrossfoldMethods.partitionUsers(SortOrder.RANDOM, HistoryPartitions.holdout(10))))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV_GZIP))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }
}
