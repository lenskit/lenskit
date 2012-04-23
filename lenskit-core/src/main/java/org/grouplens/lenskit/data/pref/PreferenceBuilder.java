package org.grouplens.lenskit.data.pref;

import org.apache.commons.lang3.builder.Builder;

/**
 * Build a preference.
 * @author Michael Ekstrand
 * @since 0.11
 */
public final class PreferenceBuilder implements Builder<SimplePreference> {
    protected long user;
    protected long item;
    protected double value;

    public static PreferenceBuilder copy(Preference pref) {
        return new PreferenceBuilder()
                .setUserId(pref.getUserId())
                .setItemId(pref.getItemId())
                .setValue(pref.getValue());
    }

    public PreferenceBuilder setUserId(long u) {
        user = u;
        return this;
    }

    public PreferenceBuilder setItemId(long i) {
        item = i;
        return this;
    }

    public PreferenceBuilder setValue(double v) {
        value = v;
        return this;
    }

    public SimplePreference build() {
        return new SimplePreference(user, item, value);
    }
}
