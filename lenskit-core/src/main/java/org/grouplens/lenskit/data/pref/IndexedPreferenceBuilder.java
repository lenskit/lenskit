package org.grouplens.lenskit.data.pref;

import org.apache.commons.lang3.builder.Builder;

/**
 * Build an indexed preference.
 * @author Michael Ekstrand
 * @since 0.11
 */
public final class IndexedPreferenceBuilder implements Builder<IndexedPreference> {
    private int index;
    private long user;
    private int userIndex;
    private long item;
    private int itemIndex;
    private double value;

    public static IndexedPreferenceBuilder copy(IndexedPreference pref) {
        return new IndexedPreferenceBuilder()
                .setIndex(pref.getIndex())
                .setUserId(pref.getUserId())
                .setUserIndex(pref.getUserIndex())
                .setItemId(pref.getItemId())
                .setItemIndex(pref.getItemIndex())
                .setValue(pref.getValue());
    }

    public IndexedPreferenceBuilder setIndex(int i) {
        index = i;
        return this;
    }

    public IndexedPreferenceBuilder setUserId(long u) {
        user = u;
        return this;
    }

    public IndexedPreferenceBuilder setUserIndex(int uidx) {
        userIndex = uidx;
        return this;
    }

    public IndexedPreferenceBuilder setItemId(long i) {
        item = i;
        return this;
    }

    public IndexedPreferenceBuilder setItemIndex(int iidx) {
        itemIndex = iidx;
        return this;
    }

    public IndexedPreferenceBuilder setValue(double v) {
        value = v;
        return this;
    }

    public IndexedPreference build() {
        return new SimpleIndexedPreference(user, item, value,
                                           index, userIndex, itemIndex);
    }
}
