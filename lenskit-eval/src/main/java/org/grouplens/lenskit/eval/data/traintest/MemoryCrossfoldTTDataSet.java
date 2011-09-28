package org.grouplens.lenskit.eval.data.traintest;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.PreparationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Crossfold data set that caches data in-memory.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MemoryCrossfoldTTDataSet implements TTDataSet {
    private static final Logger logger = LoggerFactory.getLogger(MemoryCrossfoldTTDataSet.class);
    
    private CrossfoldManager manager;
    private int foldNumber;
    private LongSet testEvents;
    private DAOFactory trainFactory;
    private DAOFactory testFactory;

    /**
     * Construct a new crossfold-based data set.
     * @param mgr The crossfold manager
     * @param n The fold number.
     */
    public MemoryCrossfoldTTDataSet(CrossfoldManager mgr, int n) {
        manager = mgr;
        foldNumber = n;
    }

    @Override
    public long lastUpdated(PreparationContext context) {
        return manager.lastUpdated(context);
    }

    @Override
    public void prepare(PreparationContext context) throws PreparationException {
        context.prepare(manager);
        
        logger.debug("Preparing data source {}", getName());
        DAOFactory factory = manager.getSource().getDAOFactory();
        
        HoldoutMode mode = manager.getHoldoutMode();
        
        testEvents = new LongOpenHashSet();
        
        DataAccessObject dao = factory.snapshot();
        try {
            LongList testUsers = manager.getFoldUsers(context, foldNumber);
            LongIterator iter = testUsers.iterator();
            while (iter.hasNext()) {
                UserHistory<Rating> history = dao.getUserHistory(iter.nextLong(), Rating.class);
                List<Rating> ratings = new ArrayList<Rating>(history);
                final int p = mode.partition(ratings, manager.getHoldoutCount());
                final int n = ratings.size();
                for (int i = p; i < n; i++) {
                    testEvents.add(ratings.get(i).getId());
                }
            }
        } finally {
            dao.close();
        }
        
        logger.debug("Found {} test events");
        
        EventProvider trainP = new EventProvider(factory, Predicates.not(testRatingPredicate()));
        EventProvider testP = new EventProvider(factory, testRatingPredicate());
        trainFactory = new EventCollectionDAO.SoftFactory(trainP);
        testFactory = new EventCollectionDAO.SoftFactory(testP);
    }

    @Override
    public String getName() {
        return String.format("%s:%d", manager.getSource().getName(), foldNumber);
    }

    @Override
    public void release() {
        testEvents = null;
        trainFactory = null;
        testFactory = null;
    }
    
    protected Predicate<Rating> testRatingPredicate() {
        return new Predicate<Rating>() {
            @Override public boolean apply(Rating r) {
                return testEvents.contains(r.getId());
            }
        };
    }

    @Override
    public DAOFactory getTrainFactory() {
        if (trainFactory == null) {
            throw new IllegalStateException("data set not prepared");
        }
        return trainFactory;
    }

    @Override
    public DAOFactory getTestFactory() {
        if (testFactory == null) {
            throw new IllegalStateException("data set not prepared");
        }
        return testFactory;
    }
    
    static class EventProvider implements Provider<List<Rating>> {
        final Predicate<? super Rating> predicate;
        final DAOFactory baseFactory;
        
        public EventProvider(DAOFactory base, Predicate<? super Rating> pred) {
            baseFactory = base;
            predicate = pred;
        }
        
        @Override
        public List<Rating> get() {
            DataAccessObject bdao = baseFactory.create();
            List<Rating> ratings;
            try {
                Cursor<Rating> cursor = 
                        Cursors.filter(bdao.getEvents(Rating.class), predicate);
                ratings = Cursors.makeList(cursor);
            } finally {
                bdao.close();
            }
            return ratings;
        }
    }
}
