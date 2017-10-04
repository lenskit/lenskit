package org.lenskit.pf;


import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.lenskit.data.ratings.RatingMatrixEntry;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class PMFModel {

    private Int2ObjectMap<ModelEntry> rows;

    public PMFModel() {
        rows = new Int2ObjectOpenHashMap<>();
    }



    public PMFModel addEntry(ModelEntry entry) {
        int row = entry.getRowNumber();
        rows.put(row, entry);
        return this;
    }

    public PMFModel addAll(PMFModel other) {
        rows.putAll(other.getRows());
        return this;
    }

    public Int2ObjectMap<ModelEntry> getRows() {
        return rows;
    }



    public ModelEntry computeItemUpdate(List<RatingMatrixEntry> ratings) {

        //TODO change this placeholder
        return new ModelEntry(ratings.get(0).getItemIndex(), 100);
    }


    public class ModelEntry {
        private double[] gammaOrLambdaShp;
        private double[] gammaOrLambdaRte;
        private double[] kappaOrTauShp;
        private double[] kappaOrTauRte;
        private final int rowNumber;

        public ModelEntry(int row, int k) {
            gammaOrLambdaShp = new double[k];
            gammaOrLambdaRte = new double[k];
            kappaOrTauShp = new double[k];
            kappaOrTauRte = new double[k];
            rowNumber = row;
        }

        public void setEntryGammaOrLambdaShp(int index, double value) {
            assert index < gammaOrLambdaShp.length && index >= 0;
            gammaOrLambdaShp[index] = value;
        }

        public void setEntryGammaOrLambdaRte(int index, double value) {
            assert index < gammaOrLambdaRte.length && index >= 0;
            gammaOrLambdaRte[index] = value;
        }

        public void setEntryKappaOrTauShp(int index, double value) {
            assert index < kappaOrTauShp.length && index >= 0;
            kappaOrTauShp[index] = value;
        }

        private void setEntryKappaOrTauRte(int index, double value) {
            assert index < kappaOrTauRte.length && index >= 0;
            kappaOrTauRte[index] = value;
        }

        private int getRowNumber() {
            return rowNumber;
        }


    }
}
