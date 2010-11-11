/**
 * 
 */
package org.grouplens.reflens.baseline;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ConstantBaselinePredictor implements RatingPredictor {
	private final double value;
	
	@BindingAnnotation
	@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Value {
		public static final String PROPERTY_NAME
			= "org.grouplens.reflens.baseline.constant.value";
	}
	
	protected ConstantBaselinePredictor(double value) {
		this.value = value;
	}
	
	public Map<Long,Double> predict(long user, Map<Long,Double> ratings, Collection<Long> items) {
		Map<Long,Double> preds = new Long2DoubleOpenHashMap();
		for (long item: items) {
			preds.put(item, value);
		}
		return preds;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(java.lang.Object, java.util.Map, java.lang.Object)
	 */
	@Override
	public ScoredId predict(long user, Map<Long, Double> ratings, long item) {
		return new ScoredId(item, value);
	}

	public static class Builder implements RatingPredictorBuilder {
		private final double value;
		@Inject
		public Builder(@Value double value) {
			this.value = value;
		}
		@Override
		public RatingPredictor build(RatingDataSource data) {
			return new ConstantBaselinePredictor(value);
		}
	}
}
