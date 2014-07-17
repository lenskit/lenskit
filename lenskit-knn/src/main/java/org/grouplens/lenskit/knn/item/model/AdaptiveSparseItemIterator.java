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
package org.grouplens.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.*;

import java.util.NoSuchElementException;

/**
 * Iterator over potential neighboring items, based on users.  This iterator makes no
 * guarantees about the order in which it returns items.  It may also return items that
 * are not associated with any user, if it determines that filtering is likely to be more
 * expensive.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class AdaptiveSparseItemIterator extends AbstractLongIterator {
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
