package org.grouplens.lenskit.knn.user;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.RatingDataAccessObject;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.knn.params.UserSimilarity;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * User-user CF implementation that caches user data for faster computation.
 *
 * <p>This implementation does nothing to update its caches, so it does not
 * update to reflect changes in ratings by users other than the current user.
 *
 * @todo Make it support updating its caches in response to changes in the data
 * source.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CachedUserUserRatingRecommender extends AbstractUserUserRatingRecommender {
    private final Long2ObjectMap<Collection<UserRatingProfile>> cache;
    private final int userCount;
    private final Similarity<? super SparseVector> similarity;
    private final int neighborhoodSize;

    @Inject
    CachedUserUserRatingRecommender(@UserSimilarity Similarity<? super SparseVector> sim,
            @NeighborhoodSize int nnbrs,
            Provider<RatingDataAccessObject> dataProvider) {
        similarity = sim;
        neighborhoodSize = nnbrs;
        int nusers = 0;
        cache = new Long2ObjectOpenHashMap<Collection<UserRatingProfile>>();
        RatingDataAccessObject data = dataProvider.get();
        Cursor<UserRatingProfile> users = data.getUserRatingProfiles();
        try {
            LongSet visitedItems = new LongOpenHashSet();
            for(UserRatingProfile user: users) {
                visitedItems.clear();
                nusers++;
                for (Rating r: user.getRatings()) {
                    long iid = r.getItemId();
                    if (!visitedItems.contains(iid)) {
                        Collection<UserRatingProfile> cxn = cache.get(iid);
                        if (cxn == null) {
                            cxn = new ArrayList<UserRatingProfile>(100);
                            cache.put(iid, cxn);
                        }
                        cxn.add(user);
                    }
                }
            }
        } finally {
            users.close();
        }
        for (Collection<UserRatingProfile> c: cache.values()) {
            ((ArrayList<UserRatingProfile>) c).trimToSize();
        }
        userCount = nusers;
    }

    protected Long2ObjectMap<? extends Collection<Neighbor>>
        findNeighbors(long uid, SparseVector vector, LongSet items) {
        final Comparator<Neighbor> comp = new NeighborSimComparator();

        if (items == null)
            items = cache.keySet();

        Long2ObjectMap<Collection<Neighbor>> neighborhoods =
            new Long2ObjectOpenHashMap<Collection<Neighbor>>(items.size());
        Long2ObjectMap<Neighbor> neighborCache = new Long2ObjectOpenHashMap<Neighbor>(userCount);

        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            final long item = iter.next();
            Collection<UserRatingProfile> users = cache.get(item);
            PriorityQueue<Neighbor> neighbors =
                new PriorityQueue<Neighbor>(neighborhoodSize + 1, comp);
            neighborhoods.put(item, neighbors);
            if (users == null) continue;

            for (UserRatingProfile user: users) {
                final long id = user.getUser();
                Neighbor nbr = neighborCache.get(id);
                if (nbr == null) {
                    SparseVector v = user.getRatingVector();
                    double sim = similarity.similarity(vector, v);
                    nbr = new Neighbor(id, v, sim);
                    neighborCache.put(id, nbr);
                }
                neighbors.add(nbr);
                if (neighbors.size() > neighborhoodSize) {
                    assert neighbors.size() == neighborhoodSize + 1;
                    neighbors.remove();
                }
            }
        }

        return neighborhoods;
    }
}
