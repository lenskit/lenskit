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
package org.lenskit.rerank;

import org.junit.Test;
import org.lenskit.api.Result;
import org.lenskit.results.RescoredResult;
import org.lenskit.results.Results;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractScoringGreedyRerankStrategyTest {
    @Test
    public void testParameterPassingAndMethodCallOrder() {
        final long theUserId = 10;
        final int theN = 100;
        final List<Result> theItems = new ArrayList<>();
        theItems.add(Results.create(0,0));

        AbstractScoringGreedyRerankStrategy selector = new AbstractScoringGreedyRerankStrategy() {
            private boolean order = true; // true - we are about to call satisfies constraint

            @Override
            protected double scoreCandidate(long userId, int n, List<? extends Result> items, Result candidate) {
                assertFalse(order);
                order = true;
                assertEquals(theUserId, userId);
                assertEquals(theN, n);
                assertEquals(theItems, items);
                return candidate.getScore();
            }

            @Override
            protected boolean satisfiesConstraint(long userId, int n, List<? extends Result> items, Result candidate) {
                // variables passed correctly
                assertTrue(order);
                order = false;
                assertEquals(theUserId, userId);
                assertEquals(theN, n);
                assertEquals(theItems, items);
                return true;
            }
        };

        List<Result> candidates = new ArrayList<>();
        candidates.add(Results.create(1,1));
        candidates.add(Results.create(2,2));
        candidates.add(Results.create(3,3));
        candidates.add(Results.create(4,4));
        candidates.add(Results.create(5,5));
        Result result = selector.nextItem(theUserId, theN, theItems, candidates);
        assertNotNull(result);
        assertEquals(5, result.getId());
    }

    @Test
    public void testFiltering() {
        AbstractScoringGreedyRerankStrategy selector = new AbstractScoringGreedyRerankStrategy() {
            private boolean order = true; // true - we are about to call satisfies constraint

            @Override
            protected double scoreCandidate(long userId, int n, List<? extends Result> items, Result candidate) {
                assertFalse(order);
                order = true;
                return candidate.getScore();
            }

            @Override
            protected boolean satisfiesConstraint(long userId, int n, List<? extends Result> items, Result candidate) {
                // variables passed correctly
                assertTrue(order);
                if((candidate.getId()%2)==0) {
                    order = false;
                    return true;
                } else {
                    order = true;
                    return false;
                }
            }
        };

        List<Result> theItems = new ArrayList<>();

        List<Result> candidates = new ArrayList<>();
        candidates.add(Results.create(1,1));
        candidates.add(Results.create(2,2));
        candidates.add(Results.create(3,3));
        candidates.add(Results.create(4,4));
        candidates.add(Results.create(5,5));
        Result result = selector.nextItem(0, -1, theItems, candidates);
        assertNotNull(result);
        assertEquals(4, result.getId());

        candidates = new ArrayList<>();
        candidates.add(Results.create(1,1));
        candidates.add(Results.create(3,3));
        candidates.add(Results.create(5,5));
        result = selector.nextItem(0, -1, theItems, candidates);
        assertNull(result);
    }

    @Test public void testScoring() {
        AbstractScoringGreedyRerankStrategy selector = new AbstractScoringGreedyRerankStrategy() {
            @Override
            protected double scoreCandidate(long userId, int n, List<? extends Result> items, Result candidate) {
                return candidate.getId()*candidate.getId();
            }
        };

        List<Result> selected = new ArrayList<>();
        List<Result> candidates = new ArrayList<>();

        candidates.add(Results.create(1,5));
        candidates.add(Results.create(3,1));
        candidates.add(Results.create(10,2));

        Result result = selector.nextItem(0, -1, selected, candidates);
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals(100, result.getScore(), 0.000001);
        assertNotNull(result.as(RescoredResult.class));
        assertNotNull(result.as(RescoredResult.class).getOriginalResult());
        assertEquals(candidates.get(2), result.as(RescoredResult.class).getOriginalResult());
    }
}
