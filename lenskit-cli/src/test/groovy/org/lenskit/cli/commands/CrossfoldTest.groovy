/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

        assertThat(cf.method, instanceOf(UserPartitionCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.RANDOM))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.holdout(10)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigureUserSample() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--sample-users"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method, instanceOf(UserSampleCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.RANDOM))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.holdout(10)))
        assertThat(cf.method.sampleSize, equalTo(1000))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigureSampleSize() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--sample-users", "--sample-size", 5000]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method, instanceOf(UserSampleCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.RANDOM))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.holdout(10)))
        assertThat(cf.method.sampleSize, equalTo(5000))
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

        assertThat(cf.method, instanceOf(UserPartitionCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.RANDOM))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.holdout(5)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testUserRetain() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--retain", "5"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method, instanceOf(UserPartitionCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.RANDOM))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.retain(5)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testHoldoutFraction() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--holdout-fraction", "0.2"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method, instanceOf(UserPartitionCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.RANDOM))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.holdoutFraction(0.2)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testTimestampOrder() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--timestamp-order"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method, instanceOf(UserPartitionCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.TIMESTAMP))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.holdout(10)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testConfigurePartitions() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--partition-count=10"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method, instanceOf(UserPartitionCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.RANDOM))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.holdout(10)))
        assertThat(cf.partitionCount, equalTo(10))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }

    @Test
    public void testCompressOutput() throws ArgumentParserException, IOException {
        String[] args = ["--output-dir", "crossfold", "--gzip-output"]
        Namespace options = parser.parseArgs(args)
        Crossfolder cf = command.configureCrossfolder(options)

        assertThat(cf.method, instanceOf(UserPartitionCrossfoldMethod))
        assertThat(cf.method.order, equalTo(SortOrder.RANDOM))
        assertThat(cf.method.partition, equalTo(HistoryPartitions.holdout(10)))
        assertThat(cf.partitionCount, equalTo(5))
        assertThat(cf.outputFormat, equalTo(OutputFormat.CSV_GZIP))
        assertThat(cf.outputDir, equalTo(Paths.get("crossfold")))
    }
}
