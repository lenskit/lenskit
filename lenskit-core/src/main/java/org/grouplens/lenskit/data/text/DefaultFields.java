package org.grouplens.lenskit.data.text;

import java.lang.annotation.*;

/**
 * Specify the default fields, in order, for building an event.  This annotation should be applied to
 * {@link org.lenskit.data.events.EventBuilder}s.  It is used by column-loading event parsers such as
 * {@link DelimitedColumnEventFormat} to read event types.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultFields {
    /**
     * The names of the default columns.  A column name can be suffixed with &lquot;?&rquot; to indicate that it is
     * optional.
     *
     * @return An array of default column names.
     */
    String[] value();
}
