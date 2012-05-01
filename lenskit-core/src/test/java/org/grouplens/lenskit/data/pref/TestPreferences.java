package org.grouplens.lenskit.data.pref;

import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Ekstrand
 */
public class TestPreferences {
    @Test
    public void testEmptyUPV() {
        SparseVector v = Preferences.userPreferenceVector(Collections.<Preference>emptyList());
        assertThat(v.isEmpty(), equalTo(true));
    }

    @Test
    public void testUPVSomePrefs() {
        List<Preference> prefs = new ArrayList<Preference>();
        prefs.add(new SimplePreference(1, 3, 2.5));
        prefs.add(new SimplePreference(1, 9, 4.0));
        prefs.add(new SimplePreference(1, 5, 2.8));
        SparseVector v = Preferences.userPreferenceVector(prefs);
        assertThat(v.size(), equalTo(3));
        assertThat(v.get(3), equalTo(2.5));
        assertThat(v.get(9), equalTo(4.0));
        assertThat(v.get(5), equalTo(2.8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUPVBadItems() {
        List<Preference> prefs = new ArrayList<Preference>();
        prefs.add(new SimplePreference(1, 3, 2.5));
        prefs.add(new SimplePreference(1, 9, 4.0));
        prefs.add(new SimplePreference(1, 5, 2.8));
        prefs.add(new SimplePreference(1, 9, Math.PI));
        SparseVector v = Preferences.userPreferenceVector(prefs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUPVBadUser() {
        List<Preference> prefs = new ArrayList<Preference>();
        prefs.add(new SimplePreference(1, 3, 2.5));
        prefs.add(new SimplePreference(1, 9, 4.0));
        prefs.add(new SimplePreference(2, 5, 2.8));
        SparseVector v = Preferences.userPreferenceVector(prefs);
    }
}
