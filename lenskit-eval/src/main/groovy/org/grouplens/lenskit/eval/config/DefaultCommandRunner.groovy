/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval.config

import org.grouplens.lenskit.eval.Command
import javax.annotation.Nullable
import javax.annotation.Nonnull

/**
 * Default runner for commands.
 *
 * @since 1.0
 * @author Michael Ekstrand
 */
class DefaultCommandRunner implements CommandRunner {
    protected final EvalScriptEngine engine

    DefaultCommandRunner(EvalScriptEngine eng) {
        engine = eng;
    }

    /**
     * Get the delegate for configuring a command. The delegate is specified with
     * the {@link ConfigDelegate} annotation on the command, with the default being
     * {@link CommandDelegate}.
     *
     * @param cmd
     */
    protected def getDelegate(@Nonnull Command cmd) {
        use(ConfigHelpers) {
            return engine.makeCommandDelegate(cmd)
        }
    }

    /**
     * Attach a delegate to a closure. The default attaches the delegate
     * with {@link Closure#setDelegate(Object)}, then sets the resolve strategy
     * to {@link Closure#DELEGATE_FIRST}.
     *
     * @param closure The closure.
     * @param delegate The delegate.
     */
    protected void attachDelegate(@Nonnull Closure closure, delegate) {
        closure.setDelegate(delegate)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
    }

    /**
     * Invoke the closure. Default implementation just calls {@link Closure#call()}.
     * @param cl The closure.
     */
    protected <V> V invokeClosure(@Nonnull Closure<V> closure) {
        closure.call()
    }

    /**
     * Call the command.
     * @param cmd The command.
     * @return The return value of the command.
     */
    protected <V> V callCommand(@Nonnull Command<V> cmd) {
        return cmd.call();
    }

    /**
     * Invoke the command. This implementation:
     * <ol>
     * <li>gets a delegate with {@link #getDelegate(Command)}
     * <li>attaches the delegate with {@link #attachDelegate(Closure, Object)}
     * <li>invokes the closure with {@link #invokeClosure(Closure)}
     * <li>calls the command with {@link #callCommand(Command)}
     * </ol>
     *
     * @param command The command to run.
     * @param closure The closure to configure it with.
     * @return The return value of the command.
     */
    @Override
    <V> V invoke(@Nonnull Command<V> command,
                 @Nullable Closure<?> closure) {
        if (closure != null) {
            def dlg = getDelegate(command)
            attachDelegate(closure, dlg)
            invokeClosure(closure)
        }
        callCommand(command)
    }
}
