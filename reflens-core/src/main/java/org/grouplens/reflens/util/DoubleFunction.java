package org.grouplens.reflens.util;

public interface DoubleFunction {
	double apply(double v);
	
	public static final class Identity implements DoubleFunction {
		public double apply(double v) {
			return v;
		}
	}
}
