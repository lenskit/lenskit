/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.item;

import it.unimi.dsi.fastutil.objects.ObjectCollections;

import java.io.Serializable;

import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.util.IndexedItemScore;
import org.grouplens.reflens.util.SimilarityMatrix;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemModel implements Serializable {

	private static final long serialVersionUID = 7040201805529926395L;
	
	private final Index itemIndexer;
	private final SimilarityMatrix matrix;
	
	public ItemItemModel(Index indexer, SimilarityMatrix matrix) {
		this.itemIndexer = indexer;
		this.matrix = matrix;
	}
	
	public Iterable<IndexedItemScore> getNeighbors(long item) {
		int idx = itemIndexer.getIndex(item);
		if (idx >= 0) {
			return matrix.getNeighbors(itemIndexer.getIndex(item));
		} else {
			return new ObjectCollections.EmptyCollection<IndexedItemScore>() {};
		}
	}
	
	public int getItemIndex(long id) {
		return itemIndexer.getIndex(id);
	}
	
	public long getItem(int idx) {
		return itemIndexer.getId(idx);
	}
}