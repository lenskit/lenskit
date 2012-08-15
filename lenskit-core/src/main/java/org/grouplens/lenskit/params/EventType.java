package org.grouplens.lenskit.params;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * A type of event, such as events to count in a history summarizer.
 * @author Michael Ekstrand
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
public @interface EventType {
}
