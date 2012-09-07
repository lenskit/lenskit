/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import static org.grouplens.lenskit.eval.config.ParameterTransforms.pickInvokable
import org.apache.commons.lang3.reflect.ConstructorUtils
import org.grouplens.lenskit.eval.Command
import org.codehaus.groovy.runtime.GroovyCategorySupport

/**
 * Helper methods for invoking configuration methods.
 * @author Michael Ekstrand
 * @since 0.10
 */
class ConfigHelpers {
    static <T> Object makeCommandDelegate(EvalConfigEngine engine, Command<T> command) {
        def annot = command.class.getAnnotation(ConfigDelegate)
        if (annot == null) {
            return new CommandDelegate<T>(engine, command)
        } else {
            Class<?> dlgClass = annot.value()
            // try two-arg constructor
            def ctor = dlgClass.constructors.find {
                def formals = it.parameterTypes
                formals.length == 2 && formals[0].isAssignableFrom(EvalConfigEngine) && formals[1].isInstance(command)
            }
            if (ctor != null) {
                return ctor.newInstance(engine, command)
            } else {
                ctor = dlgClass.constructors.find {
                    def formals = it.parameterTypes
                    formals.length == 1 && formals[0].isInstance(command)
                }
            }
            if (ctor != null) {
                return ctor.newInstance(command)
            } else {
                return dlgClass.newInstance()
            }
        }
    }

    /**
     * Resolve a method invocation with a command factory. Takes the name of a method and its
     * arguments and, if possible, constructs a closure that returns the result of configuring
     * a command and running it.
     * @param name The name of the method
     * @param args The arguments to the method.
     * @return A closure invoking and configuring the command, returning the built object,
     * or {@code null} if the command cannot be invoked.
     * @throws IllegalArgumentException if the command can be found but {@code args} is
     * inappropriate.
     */
    static Closure findCommandMethod(EvalConfigEngine engine, String name, args) {
        Class<? extends Command> commandClass = engine.getCommand(name)
        if (commandClass == null) return null

        def command = makeCommandClosure(commandClass, engine, args)
        if (command == null) {
            def msg = "cannot instantiate ${commandClass.name}: no suitable constructor found"
            throw new InstantiationException(msg)
        }

        // finally have validated the arguments, move on
        return {
            def obj = command(args)
            obj
        }
    }

    static def makeCommandClosure(Class<? extends Command> cmd, EvalConfigEngine engine, Object[] args) {
        Closure block = null
        Object[] trimmedArgs
        if (args.length > 0 && args[args.length - 1] instanceof Closure) {
            block = args[args.length - 1] as Closure
            trimmedArgs = Arrays.copyOf(args, args.length - 1)
        } else {
            trimmedArgs = args
        }
        def bestCtor = pickInvokable(trimmedArgs) {
            ConstructorUtils.getMatchingAccessibleConstructor(cmd, it)
        }
        if (bestCtor != null) {
            def runCfg = cmd.getAnnotation(ConfigRunner)
            def runner = null
            if (runCfg != null) {
                runner = runCfg.value().newInstance(engine)
            } else {
                runner = new DefaultCommandRunner(engine)
            }
            return {
                Object[] txargs = bestCtor.right.collect({it.get()})
                def command = bestCtor.left.newInstance(txargs)
                runner.invoke(command, block)
            }
        } else {
            return null
        }
    }

    static def callWithDelegate(Closure closure, delegate, Object... args) {
        closure.setDelegate(delegate)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        closure.call(args)
    }
}
