/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * NOTE: the copyright permissions for this file are different from those
 * applying to the rest of RefLens.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.grouplens.reflens.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.util.Types;

/**
 * Utilities for working with types.  Useful for doing weird tricks with Guice.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@SuppressWarnings("unchecked")
public class TypeUtils {
	private static ParameterizedType typeOfClass(Class c) {
		return Types.newParameterizedType(c, c.getTypeParameters());
	}
	
	public static Type reifyType(Type template, Class clazz) {
		if (clazz.getTypeParameters().length == 0)
			return clazz;
		
		ParameterizedType t = typeOfClass(clazz);
		TypeAssignment asn = findAssignment(template, t, null);
		if (asn == null)
			return clazz;
		
		TypeVariable[] params = clazz.getTypeParameters();
		Type[] bindings = new Type[params.length];
		for (int i = 0; i < params.length; i++) {
			bindings[i] = asn.resolve(params[i]);
		}
		return Types.newParameterizedType(clazz, bindings);
	}
	
	private static TypeAssignment findAssignment(Type template, ParameterizedType t, TypeAssignment base) {
		TypeAssignment asn = new TypeAssignment(base);
		if (!asn.bindParams(t))
			return null;
		
		if (unifyTypes(template, t, asn)) {
			return asn;
		} else {
			Type raw = t.getRawType();
			if (raw instanceof Class) {
				Class c = (Class) raw;
				asn = new TypeAssignment(base);
				asn.bindParams(t);
				for (Type i: c.getGenericInterfaces()) {
					if (!(i instanceof ParameterizedType))
						continue;
					ParameterizedType pti = (ParameterizedType) i;
					TypeAssignment sup = findAssignment(template, pti, asn);
					if (sup != null)
						return sup;
				}
				Type i = c.getGenericSuperclass();
				if (i != null && i instanceof ParameterizedType) { // we have an interface
					ParameterizedType pti = (ParameterizedType) i;
					TypeAssignment sup = findAssignment(template, pti, asn);
					if (sup != null)
						return sup;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Compute the set of unbound type variables used in <var>t</var>.
	 * @param t A type
	 * @return The set of type variables used unbound in <var>t</var>
	 */
	public static Set<TypeVariable> findFreeVariables(Type t) {
		Set<TypeVariable> s = new HashSet<TypeVariable>();
		if (t instanceof TypeVariable) {
			s.add((TypeVariable) t);
		} else if (t instanceof Class) {
			for (TypeVariable to: ((Class) t).getTypeParameters()) {
				s.add(to);
			}
		} else if (t instanceof ParameterizedType) {
			for (Type to: ((ParameterizedType) t).getActualTypeArguments()) {
				s.addAll(findFreeVariables(to));
			}
		} else if (t instanceof GenericArrayType) {
			s.addAll(findFreeVariables(((GenericArrayType) t).getGenericComponentType()));
		} else {
			throw new RuntimeException("invalid type");
		}
		return s;
	}
	
	private static boolean unifyTypes(Type fixed, Type open, TypeAssignment assignment) {
		assert findFreeVariables(fixed).isEmpty();
		
		Type ropen = assignment.resolve(open);
		if (fixed.equals(ropen))
			return true;
		
		if (ropen instanceof TypeVariable) {
			// open is a type variable; assign it.  don't need occurance check
			// since fixed has no free variables.
			return assignment.set((TypeVariable) ropen, fixed);
		} 
		
		ParameterizedType fpt;
		ParameterizedType pt;
		try {
			fpt = (ParameterizedType) fixed;
			if (ropen instanceof Class) {
				pt = typeOfClass((Class) ropen);
			} else { // generic arrays will fail. that's fine.
				pt = (ParameterizedType) ropen;
			}
		} catch (ClassCastException e) {
			return false; // whoops, we cannot unify.
		}
		
		if (fpt.getRawType().equals(pt.getRawType())) {
			Type[] fargs = fpt.getActualTypeArguments();
			Type[] targs = pt.getActualTypeArguments();
			assert fargs.length == targs.length;
			
			for (int i = 0; i < fargs.length; i++) {
				if (!unifyTypes(fargs[i], targs[i], assignment))
					return false;
			}
			// we could unify everything
			return true;
		} else {
			return false;
		}
	}
	
	private static class TypeAssignment {
		private Map<TypeVariable,Type> assignments;
		
		@SuppressWarnings("unused")
		public TypeAssignment() {
			this(null);
		}
		
		public TypeAssignment(TypeAssignment base) {
			if (base == null)
				assignments = new HashMap<TypeVariable, Type>();
			else
				assignments = new HashMap<TypeVariable, Type>(base.assignments);
		}
		
		public boolean set(TypeVariable v, Type t) {
			if (v.equals(t))
				return true;
			
			if (assignments.containsKey(v))
				return false;
			
			assignments.put(v, t);
			return true;
		}
		
		public Type resolve(Type t) {
			if (t instanceof TypeVariable) {
				TypeVariable v = (TypeVariable) t;
				Type tgt = assignments.get(v);
				if (tgt == null)
					return t;
				else
					return resolve(tgt);
			} else {
				return t;
			}
		}
		
		public boolean bindParams(ParameterizedType t) {
			try {
				Class c = (Class) t.getRawType();
				return bindParams(t, c);
			} catch (ClassCastException e) {
				return false;
			}
		}
		
		public boolean bindParams(ParameterizedType t, Class c) {
			Type[] actuals = t.getActualTypeArguments();
			TypeVariable[] formals = c.getTypeParameters();
			assert actuals.length == formals.length;
			for (int i = 0; i < actuals.length; i++) {
				if (!set(formals[i], actuals[i]))
					return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			return assignments.toString();
		}
	}
}
