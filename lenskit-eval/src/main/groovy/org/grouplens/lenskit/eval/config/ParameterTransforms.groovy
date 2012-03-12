/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import org.apache.commons.lang3.tuple.Pair
import com.google.common.base.Function

/**
 * Helper methods for transforming parameters.
 * @author Michael Ekstrand
 */
class ParameterTransforms {
    abstract static class Transform {
        final Class resultType
        Transform(Class type) {
            resultType = type
        }
        abstract def get()
    }

    static Transform transform(Class type, Closure transformer) {
        return new Transform(type) {
            def get() {
                transformer.call()
            }
        }
    }

    static List<Transform> computeArgTransforms(Object arg) {
        List<Transform> res = new LinkedList<Transform>()
        if (arg instanceof String) {
            res.add transform(File) {
                new File(arg as String)
            }
        } else if (arg instanceof GString) {
            res.add transform(File) {
                new File(arg as String)
            }
        }
        if (arg instanceof Class) {
            res.add transform(arg) {
                (arg as Class).newInstance()
            }
        }
        if (arg instanceof BigDecimal) {
            res.add transform(Float) {
                (arg as BigDecimal).floatValue()
            }
        }
        if (arg instanceof Number && !(arg instanceof Double)) {
            res.add transform(Double) {
                (arg as Number).doubleValue()
            }
        }
        if (!(arg instanceof String)) {
            res.add transform(String) {
                arg.toString()
            }
        }
        if (arg instanceof Closure) {
            res.add transform(Function) {
                new Function() {
                    def apply(x) {
                        arg(x)
                    }
                }
            }
        }

        return res
    }

    private static Transform identity(Object arg) {
        transform(arg.class, {arg})
    }

    private static void computeTransforms(Object[] args, List<Transform> soFar, List<Transform[]> accum) {
        def i = soFar.size()
        if (i == args.length) {
            accum.add(soFar.toArray(new Transform[i]))
        } else if (i < args.length) {
            def arg = args[i]
            // no-op transform
            computeTransforms(args, soFar + identity(arg), accum)
            // all transforms of this argument
            for (tx in computeArgTransforms(arg)) {
                if (tx.resultType != arg.class) {
                    computeTransforms(args, soFar + tx, accum)
                }
            }
        }
    }

    private static List<Transform[]> findAllTransforms(Object[] args) {
        def accum = new LinkedList<Transform[]>();
        computeTransforms(args, [], accum)
        return accum
    }

    /**
     * Pick in invokable (method or constructor) that is invokable by the
     * arguments.
     * @param args The arguments.
     * @param query A closure that queries whether an invokable exists for
     * some argument types. It takes an array of argument types as its parameter,
     * and returns the method or constructor or null if none exists.
     * @return An (invokable,transforms) pair, or {@code null} if no match is found.
     */
    static def pickInvokable(Object[] args, Closure query) {
        // get all transforms that yield a valid object
        def transforms = findAllTransforms(args)
        def candidates = transforms.collect({tx ->
            Class[] types = tx.collect({it.resultType})
            def x = query(types)
            x == null ? null : Pair.of(x, tx)
        }).findAll()

        if (candidates.size() == 1) {
            return candidates.get(0)
        } else if (candidates.size() > 1) {
            throw new RuntimeException("too many options for method")
        } else {
            return null
        }
    }
}
