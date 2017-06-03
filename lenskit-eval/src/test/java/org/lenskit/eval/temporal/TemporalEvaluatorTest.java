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
package org.lenskit.eval.temporal;

import net.java.quickcheck.Generator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lenskit.LenskitConfiguration;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.baseline.UserMeanBaseline;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.test.LenskitGenerators;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

public class TemporalEvaluatorTest {
    public static final int RATING_COUNT = 35;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    public DataAccessObject dao;
    public File predictOutputFile;
    public TemporalEvaluator tempEval = new TemporalEvaluator();

    @Before
    public void initialize() throws IOException {
        predictOutputFile = folder.newFile("predictions.csv");
        List<Rating> ratings = new ArrayList<>();
        Generator<Rating> rgen = LenskitGenerators.ratings();
        Set<Pair<Long,Long>> used = new HashSet<>();

        while (ratings.size() < RATING_COUNT) {
            Rating r = rgen.next();
            long uid = r.getUserId() % 5;
            Pair<Long,Long> ui = ImmutablePair.of(uid, r.getItemId());
            if (used.contains(ui)) {
                continue;
            }

            used.add(ui);
            Rating r2 = r.copyBuilder()
                         .setUserId(r.getUserId() % 5)
                         .build();
            ratings.add(r2);
        }

        assumeThat(ratings, hasSize(RATING_COUNT));

        dao = StaticDataSource.fromList(ratings).get();

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(ItemScorer.class).to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class).to(ItemMeanRatingItemScorer.class);

        tempEval.setRebuildPeriod(1L);
        tempEval.setDataSource(dao);
        tempEval.setAlgorithm("UserMeanBaseline", config);
        tempEval.setOutputFile(predictOutputFile);
    }

    /**
     * Test that we can run it, and it produces enough data.
     */
    @Test
    public void ExecuteTest() throws IOException, RecommenderBuildException {
        tempEval.execute();
        assertTrue(predictOutputFile.isFile());
        try (FileReader reader = new FileReader(predictOutputFile)) {
            try (LineNumberReader lnr = new LineNumberReader(reader)) {
                lnr.skip(Long.MAX_VALUE);
                int lines = lnr.getLineNumber();
                assertThat(lines, equalTo(RATING_COUNT + 1));
            }
        }
    }
}
