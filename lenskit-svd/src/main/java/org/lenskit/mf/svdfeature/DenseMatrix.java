package org.lenskit.mf.svdfeature;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DenseMatrix {
    private int numRows;
    private int numCols;
    private double[][] matrix;

    public DenseMatrix(int outNumRows, int outNumCols) {
        numCols = outNumCols;
        numRows = outNumRows;
        matrix = new double[numRows][numCols];
        for (int i=0; i<numRows; i++) {
            ArrayHelper.randomInitialize(matrix[i]);
        }
    }

    public double getEntry(int rindex, int cindex) {
        return matrix[rindex][cindex];
    }

    public void setEntry(int rindex, int cindex, double entry) {
        matrix[rindex][cindex] = entry;
    }

    public double[] getRow(int rindex) {
        return matrix[rindex];
    }

    public int getNumOfRows() {
        return numRows;
    }

    public int getNumOfCols() {
        return numCols;
    }
}
