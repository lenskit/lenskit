/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.eval.data.crossfold;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.Scanner;

import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.eval.Preparable;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.PreparationException;
import org.grouplens.lenskit.eval.data.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class CrossfoldManager implements Preparable {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldManager.class);
    
    private static final String SPLIT_FILE = "split.dat";
    
    private static final Random random = new Random();
    
	private DataSource source;
	private final int foldCount;
	private final Holdout holdout;

	public CrossfoldManager(DataSource src, int folds, Holdout hout) {
	    source = src;
	    foldCount = folds;
	    holdout = hout;
    }
	
	/**
	 * Get the data source backing this crossfold manager.
     * @return The underlying data source.
     */
    public DataSource getSource() {
        return source;
    }

    /**
     * Get the number of folds.
     * @return The number of folds in this crossfold.
     */
    public int getFoldCount() {
        return foldCount;
    }

    /**
     * Get the holdout mode.
     * @return The holdout mode.
     */
    public Holdout getHoldout() {
        return holdout;
    }

    /**
     * Get the cache directory for this crossfolder.
     * 
     * @param context The preparation context.
     * @return The directory in which to cache data. If the manager is prepared,
     *         this directory will exist.
     */
    public File cacheDir(PreparationContext context) {
	    String name = "crossfold-" + source.getName();
	    try {
            name = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Java is broken", e);
        }
	    return new File(context.getCacheDirectory(), name);
	}

	@Override
	public long lastUpdated(PreparationContext context) {
	    File split = new File(cacheDir(context), SPLIT_FILE);
	    return split.exists() ? split.lastModified() : -1L;
	}

	@Override
	public void prepare(PreparationContext context) throws PreparationException {
	    context.prepare(source);
	    
	    if (!context.isUnconditional() && lastUpdated(context) >= source.lastUpdated(context)) {
	        logger.debug("Crossfold {} up to date", this);
	        return;
	    }
	    
	    DAOFactory factory = source.getDAOFactory();
	    DataAccessObject dao = factory.create();
	    try {
	        Long2IntMap splits = splitUsers(dao);
	        writeSplits(context, splits);
	    } catch (IOException e) {
	        throw new PreparationException("Error writing partitions to disk", e);
        } finally {
	        dao.close();
	    }
	}
	
	protected Long2IntMap splitUsers(DataAccessObject dao) {
        Long2IntMap userMap = new Long2IntOpenHashMap();
        LongArrayList users = Cursors.makeList(dao.getUsers());
        // Randomly allocate users in a Fisher-Yates shuffle
        int fold = 0;
        for (int i = users.size() - 1; i > 0; i--) {
            int j = random.nextInt(i+1);
            // put users[j] in a fold
            long u = j;
            userMap.put(u, fold+1);
            fold = (fold + 1) % foldCount;
            // replace users[j] with users[i] to remove used user
            if (i != j) {
                users.set(j, users.get(i));
            }
        }
        
        logger.info("Partitioned {} users", userMap.size());
        return userMap;
    }
	
	/**
	 * Write the user partitions out into a data file.
	 * 
	 * <p>This method uses a strategy based on the POSIX rename() pattern, but
	 * it does not have the safety guarantees of POSIX rename().  The file is
	 * deleted first if it exists, a temp file is then created and written to,
	 * and finally the temp file is renamed to the real file.  This should be
	 * safe so long as there aren't two processes trying to prepare with the
	 * same cache directory. 
	 * 
	 * @param context The preparation context.
	 * @param splits The user partition data.
	 * @throws IOException if there is an error writing the partitions to disk.
	 */
	protected void writeSplits(PreparationContext context, Long2IntMap splits) throws IOException {
	    File splitFile = new File(cacheDir(context), SPLIT_FILE);
	    File tmpFile = new File(cacheDir(context), SPLIT_FILE + ".tmp");
	    Files.createParentDirs(splitFile);
	    
	    logger.info("Writing user partitions to {}", splitFile);
	    
	    if (splitFile.exists()) {
	        logger.debug("Deleting {}", splitFile);
	        splitFile.delete();
	    }
	    
	    logger.debug("Writing to {}", tmpFile);
	    PrintWriter writer = new PrintWriter(tmpFile);
	    try {
	        writer.println(foldCount);
	        for (Long2IntMap.Entry entry: splits.long2IntEntrySet()) {
	            writer.print(entry.getLongKey());
	            writer.print('\t');
	            writer.print(entry.getIntValue());
	            writer.println();
	        }
	    } finally {
	        writer.close();
	    }
	    
	    logger.debug("Renaming temp file");
	    if (!tmpFile.renameTo(splitFile)) {
	        throw new IOException("Failed to rename temp file");
	    }
	}
	
	public LongList getFoldUsers(PreparationContext context, int fold) {
	    File split = new File(cacheDir(context), SPLIT_FILE);
	    
	    LongList users = new LongArrayList();
	    
	    Scanner input;
        try {
            input = new Scanner(split);
            try {
                int nfolds = input.nextInt();
                if (fold < 1 || fold > nfolds) {
                    throw new IllegalArgumentException("Invalid fold number " + fold);
                }
                while (input.hasNextLong()) {
                    long uid = input.nextLong();
                    int f = input.nextInt();
                    if (f == fold) {
                        users.add(uid);
                    }
                }
            } finally {
                input.close();
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Crossfold manager not prepared", e);
        }
	    
        return users;
	}
	
	@Override
	public String toString() {
	    return String.format("{CXManager %s}", source);
	}
}
