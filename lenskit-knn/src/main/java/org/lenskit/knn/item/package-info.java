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
/**
 * Implementation of item-item collaborative filtering.
 * <p>
 * The item-item CF implementation is built up of several pieces. The
 * {@linkplain org.lenskit.knn.item.model.ItemItemModelProvider model builder} takes the rating data
 * and several parameters and components, such as the
 * {@linkplain org.lenskit.similarity.VectorSimilarity similarity function} and {@linkplain ModelSize model size},
 * and computes the {@linkplain org.lenskit.knn.item.model.SimilarityMatrixModel similarity matrix}. The
 * {@linkplain ItemItemScorer scorer}
 * uses this model to score items.
 * <p>
 * The basic idea of item-item CF is to compute similarities between items, typically
 * based on the users that have rated them, and the recommend items similar to the items
 * that a user likes. The model is then truncated — only the {@link ModelSize} most similar
 * items are retained for each item – to save space. Neighborhoods are further truncated
 * when doing recommendation; only the {@link org.lenskit.knn.NeighborhoodSize} most similar items that
 * a user has rated are used to score any given item. {@link ModelSize} is typically
 * larger than {@link org.lenskit.knn.NeighborhoodSize} to improve the ability of the recommender to find
 * neighbors.
 * <p>
 * When the similarity function is asymmetric (\(s(i,j)=s(j,i)\) does not hold), some care
 * is needed to make sure that the function is used in the correct direction. Following
 * Deshpande and Karypis, we use the similarity function as \(s(j,i)\), where \(j\) is the
 * item the user has purchased or rated and \(i\) is the item that is going to be scored. This
 * function is then stored in row \(i\) and column \(j\) of the matrix. Rows are then truncated
 * (so we retain the {@link ModelSize} most similar items for each \(i\)); this direction differs
 * from Deshpande &amp; Karypis, as row truncation is more efficient &amp; simpler to write within
 * LensKit's item-item algorithm structure, and performs better in offline tests against the
 * MovieLens 1M data set
 * (see <a href="http://dev.grouplens.org/trac/lenskit/wiki/ItemItemTruncateDirection">writeup</a>).
 * Computation against a particular item the user has rated is done down that item's column.
 * <p>
 * The scorers and recommenders actually operate on a generic {@link org.lenskit.knn.item.model.ItemItemModel}, so the
 * item-based scoring algorithm can be used against other sources of similarity, such as
 * similarities stored in a database or text index.
 */
package org.lenskit.knn.item;

