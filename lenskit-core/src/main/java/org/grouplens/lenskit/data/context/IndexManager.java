package org.grouplens.lenskit.data.context;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.ArrayList;

/**
 * Class for tracking accumulated global indexes for ratings.  It's basically
 * a simple mapping from (int,int) pairs to ints that assumes that the domain
 * ints are usable as array indices.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
final class IndexManager {
	// Things are stored as an array list mapping uidx's to maps, which in turn
	// map iidx's to global indices.
	final private ArrayList<Int2IntMap> mapping;
	
	public IndexManager() {
		mapping = new ArrayList<Int2IntMap>();
	}
	
	public IndexManager(int ucap) {
		mapping = new ArrayList<Int2IntMap>(ucap);
	}
	
	/**
	 * Get the interned index for the uidx,iidx pair.
	 * @param uidx User index.
	 * @param iidx Item index.
	 * @return The previously-stored global index, or -1 if no such index
	 * has been stored.
	 */
	public int getIndex(int uidx, int iidx) {
		if (uidx < 0) throw new IndexOutOfBoundsException();
		if (uidx > mapping.size())
			return -1;
		return mapping.get(uidx).get(iidx);
	}
	
	/**
	 * Save a global index for the uidx,iidx pair.  Overwrites any index already
	 * stored.
	 * @param uidx User index.
	 * @param iidx Item index.
	 * @param idx Global index.
	 */
	public void putIndex(int uidx, int iidx, int idx) {
		Int2IntMap imap = null;
		while (uidx >= mapping.size()) {
			imap = new Int2IntOpenHashMap();
			imap.defaultReturnValue(-1);
			mapping.add(imap);
		}
		if (imap == null)
			imap = mapping.get(uidx);
		imap.put(iidx, idx);
	}
}
