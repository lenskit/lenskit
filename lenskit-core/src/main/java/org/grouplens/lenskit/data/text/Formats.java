package org.grouplens.lenskit.data.text;

/**
 * Utility classes for event formats.
 *
 * @since 2.2
 */
public final class Formats {
    private Formats() {}

    /**
     * Get a format for reading the ML-100K data set.
     *
     * @return A format for using {@link TextEventDAO} to read the ML-100K data set.
     */
    public static DelimitedColumnEventFormat ml100kFormat() {
        DelimitedColumnEventFormat fmt = new DelimitedColumnEventFormat(new RatingEventType());
        fmt.setDelimiter("\t");
        fmt.setFields(Fields.user(), Fields.item(), Fields.rating(), Fields.timestamp());
        return fmt;
    }

    /**
     * Get a format for reading the MovieLens data sets.
     *
     * @return A format for using {@link TextEventDAO} to read the ML-1M and ML-10M data sets.
     */
    public static DelimitedColumnEventFormat movieLensFormat() {
        DelimitedColumnEventFormat fmt = new DelimitedColumnEventFormat(new RatingEventType());
        fmt.setDelimiter("::");
        fmt.setFields(Fields.user(), Fields.item(), Fields.rating(), Fields.timestamp());
        return fmt;
    }
}
