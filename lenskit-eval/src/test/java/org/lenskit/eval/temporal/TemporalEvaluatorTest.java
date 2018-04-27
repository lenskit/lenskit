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
