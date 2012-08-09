package org.grouplens.lenskit.core;

import java.lang.annotation.*;

/**
 * Mark a component implementation as shareable. Shareable components can be shared
 * between recommender sessions. Things like item-item models should be shareable.
 * <p>
 * Shareable components must meet the following requirements:
 * <ul>
 * <li>Be thread-safe</li>
 * <li>Be serializable (or externalizable)</li>
 * </ul>
 * <p>
 * Shareable components will be reused as much as possible. If a shareable component
 * has no non-transient non-shareable dependencies, then it will be created once per
 * recommender <i>engine</i> rather than per-recommender.
 * <p>
 * The Shareable annotation should be on the component implementation, not interface.
 *
 * @author Michael Ekstrand
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Shareable {
}
