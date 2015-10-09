package org.lenskit.data.events;

import java.lang.annotation.*;

/**
 * Identify the builder for an event.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuiltBy {
    /**
     * The class that builds the event type.
     * @return The class that builds the event type.
     */
    Class<? extends EventBuilder<?>> value();
}
