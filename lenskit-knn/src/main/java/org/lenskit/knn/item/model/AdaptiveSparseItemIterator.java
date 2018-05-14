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
package org.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

/**
 * Iterator over potential neighboring items, based on users.  This iterator makes no
 * guarantees about the order in which it returns items.  It may also return items that
 * are not associated with any user, if it determines that filtering is likely to be more
 * expensive.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class AdaptiveSparseItemIterator implements LongIterator {
    private static final Logger logger = LoggerFactory.getLogger(AdaptiveSparseItemIterator.class);
    private final ItemItemBuildContext context;
    private final LongSet users;
    private final long lowerBound;
    private final int universeSize;

    private LongSet seen;

    private boolean advanced;
    private boolean atEnd;
    private long nextItem;
    // The iterator of users. So long as this is not-null, we'll keep trying new users
    private LongIterator userIter;
    // The current set of items we're iterating
    // When this is null, try to get a new set from userIter
    // If userIter is empty or null, we are done
    private LongIterator currentItems;
    private int usersSeen;

    public AdaptiveSparseItemIterator(ItemItemBuildContext context, LongSet users) {
        this(context, users, Long.MIN_VALUE);
    }

    public AdaptiveSparseItemIterator(ItemItemBuildContext context, LongSet users, long lowerBound) {
        this.context = context;
        this.users = users;
        this.lowerBound = lowerBound;
        if (lowerBound == Long.MIN_VALUE) {
            universeSize = context.getItems().size();
        } else {
            // since universe size is only used for sparsity estimation, it is
            // fine to have an off-by-1 discrepancy between this & iterator behavior
            universeSize = context.getItems().tailSet(lowerBound).size();
        }

        seen = new LongOpenHashSet(context.getItems().size());
        userIter = users.iterator();
    }

    private void maybeAdvance() {
        if (advanced) {
            return; // already done
        }
        // now we need to look for a new item
        // we'll iterate until we've successfully advanced
        while (!advanced) {
            // make sure we have a set of items to iterate
            if (currentItems == null && userIter != null) {
                // try to estimate cost - if we have seen 75% of items, and have at least
                // 50% users left to go, just use all the items
                LongSortedSet items = null;
                if (universeSize - seen.size() <= universeSize / 4 && usersSeen <= users.size() / 2) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("dropping sparsity, using full universe (saw {} of {} items, {} of {} users)",
                                     seen.size(), universeSize, usersSeen, users.size());
                    }
                    items = context.getItems();
                    userIter = null; // so this is the last set of items we consider
                } else if (userIter.hasNext()) {
                    long user = userIter.nextLong();
                    usersSeen += 1;
                    items = context.getUserItems(user);
                }
                if (items != null) {
                    if (lowerBound == Long.MIN_VALUE) {
                        currentItems = items.iterator();
                    } else {
                        currentItems = items.iterator(lowerBound);
                    }
                }
            }
            // now try to advance to the next item
            if (currentItems == null) {
                // still no set of items, this means we're done
                advanced = true;
                atEnd = true;
            } else if (currentItems.hasNext()) {
                // there is a new item, try it
                nextItem = currentItems.nextLong();
                if (!seen.contains(nextItem)) {
                    // it is unseen, we will let this be the next item
                    advanced = true;
                    seen.add(nextItem);
                }
            } else {
                // there is no next item in the current set, clear it and try again
                currentItems = null;
                // the next iteration of the loop will update currentItems
            }
        }
    }

    @Override
    public boolean hasNext() {
        maybeAdvance();
        return !atEnd;
    }

    @Override
    public long nextLong() {
        maybeAdvance();
        if (atEnd) {
            throw new NoSuchElementException();
        } else {
            advanced = false;
            return nextItem;
        }
    }
}
