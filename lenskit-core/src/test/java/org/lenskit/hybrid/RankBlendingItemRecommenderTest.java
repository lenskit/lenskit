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
