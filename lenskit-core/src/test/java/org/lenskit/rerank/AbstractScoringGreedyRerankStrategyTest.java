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
