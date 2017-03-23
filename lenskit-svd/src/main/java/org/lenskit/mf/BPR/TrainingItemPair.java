package org.lenskit.mf.BPR;

/**
 * class to represent a training pair in pairwise learning to rank items.
 * By convention the pair represents the knowledge that g>l (according to user u)
 */
public class TrainingItemPair {
    // REVIEW: if I make these ints (rows in the matrix as per the snapshot) the code would theoretically be faster.
    public final long u;
    public final long g;
    public final long l;

    public TrainingItemPair(long u, long g, long l) {
        this.u = u;
        this.g = g;
        this.l = l;
    }
}
