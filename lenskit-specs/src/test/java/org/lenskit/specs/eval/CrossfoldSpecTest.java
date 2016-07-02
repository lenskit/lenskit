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
package org.lenskit.specs.eval;

import org.junit.Before;
import org.junit.Test;
import org.lenskit.specs.data.PackedDataSourceSpec;
import org.lenskit.specs.data.PrefDomainSpec;
import org.lenskit.specs.data.TextDataSourceSpec;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class CrossfoldSpecTest {
    TextDataSourceSpec input;

    @Before
    public void createDataSource() {
        input = new TextDataSourceSpec();
        input.setFile(Paths.get("ratings.csv"));
        input.setName("TestData");
    }

    @Test
    public void testDefaults() {
        CrossfoldSpec spec = new CrossfoldSpec();
        spec.setName("TestData");
        spec.setSource(input);
        spec.setOutputDir(Paths.get("crossfold.out"));

        assertThat(spec.getPartitionCount(), equalTo(5));
        assertThat(spec.getOutputFormat(), equalTo(OutputFormat.CSV));
        assertThat(spec.getOutputDir(), equalTo(Paths.get("crossfold.out")));
        assertThat(spec.getIncludeTimestamps(), equalTo(true));
        assertThat(spec.getMethod(), equalTo(CrossfoldMethod.PARTITION_USERS));
        assertThat(spec.getUserPartitionMethod(),
                   instanceOf(PartitionMethodSpec.Holdout.class));
        PartitionMethodSpec.Holdout holdout = (PartitionMethodSpec.Holdout) spec.getUserPartitionMethod();
        assertThat(holdout.getCount(), equalTo(10));
        assertThat(holdout.getOrder(), equalTo("random"));
    }

    @Test
    public void testBasicDataSets() {
        CrossfoldSpec spec = new CrossfoldSpec();
        spec.setName("TestData");
        spec.setSource(input);
        input.setDomain(PrefDomainSpec.fromString("[1,5]/1.0"));
        spec.setOutputDir(Paths.get("crossfold.out"));
        List<DataSetSpec> sets = spec.getDataSets();
        assertThat(sets, hasSize(5));
        for (int i = 0; i < 5; i++) {
            DataSetSpec set = sets.get(i);
            assertThat(set.getName(), equalTo("TestData." + (i+1)));
            assertThat(set.getAttributes(), hasEntry("DataSet", (Object) "TestData"));
            assertThat(set.getAttributes(), hasEntry("Partition", (Object) (i+1)));

            assertThat(set.getTrainSource(), instanceOf(TextDataSourceSpec.class));
            TextDataSourceSpec tds = (TextDataSourceSpec) set.getTrainSource();
            assertThat(tds.getFile(), equalTo(spec.getOutputDir().resolve(String.format("part%02d.train.csv", i+1))));
            assertThat(tds.getDomain(),
                       equalTo(PrefDomainSpec.fromString("[1,5]/1.0")));
            assertThat(tds.getDelimiter(), equalTo(","));

            assertThat(set.getTestSource(), instanceOf(TextDataSourceSpec.class));
            tds = (TextDataSourceSpec) set.getTestSource();
            assertThat(tds.getFile(), equalTo(spec.getOutputDir().resolve(String.format("part%02d.test.csv", i+1))));
            assertThat(tds.getDomain(),
                       equalTo(PrefDomainSpec.fromString("[1,5]/1.0")));
            assertThat(tds.getDelimiter(), equalTo(","));
        }
    }
}
