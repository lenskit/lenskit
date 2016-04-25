package org.lenskit.mf.svdfeature;

public enum SVDFeatureIndexName {
    BIASES("biases"),
    FACTORS("factors");

    private final String indexName;

    SVDFeatureIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String get() {
        return indexName;
    }
}
