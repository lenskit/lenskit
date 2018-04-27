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
package org.lenskit.hybrid;

import org.junit.Test;
import org.lenskit.api.ResultList;
import org.lenskit.results.Results;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.lenskit.hybrid.RankBlendingItemRecommender.merge;

public class RankBlendingItemRecommenderTest {
    @Test
    public void testBlendScores() {
        ResultList left = Results.newResultList(Results.create(1, 2.0),
                                                Results.create(2, 1.5),
                                                Results.create(3, 1.0));
        ResultList right = Results.newResultList(Results.create(2, 2.0),
                                                 Results.create(1, 1.5),
                                                 Results.create(4, 1.0),
                                                 Results.create(3, 0.8));
        // test that a couple scores are blended
        ResultList res = merge(-1, left, right, 0.7);
        assertThat(res, hasSize(4));
        assertThat(res.idList(), contains(1L, 2L, 4L, 3L));
        // first of left, 2nd of right
        assertThat(res.get(0).getScore(),
                   closeTo(0.7 + 0.2, 1.0e-6));
        // 2nd of left, 1st of right
        assertThat(res.get(1).getScore(),
                   closeTo(0.7*0.5 + 0.3, 1.0e-6));
        // 3rd of right (1/3 * 0.3)
        assertThat(res.get(2).getScore(),
                   closeTo(0.1, 1.0e-6));
        assertThat(res.get(2).as(RankBlendResult.class).getLeft(),
                   nullValue());
        // last of each
        assertThat(res.get(3).getScore(),
                   closeTo(0.0, 1.0e-6));
    }
}
