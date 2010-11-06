/**
 * 
 */
package org.grouplens.reflens.baseline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.data.DataSet;
import org.grouplens.reflens.data.ScoredObject;
import org.grouplens.reflens.data.UserRatingProfile;

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

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(java.lang.Object, java.util.Map, java.lang.Object)
	 */
	@Override
	public ScoredObject<Long> predict(long user, Map<Long, Double> ratings, long item) {
		return new ScoredObject<Long>(item, value);
	}

	public static class Builder implements RatingPredictorBuilder {
		private final double value;
		@Inject
		public Builder(@Value double value) {
			this.value = value;
		}
		@Override
		public RatingPredictor build(DataSet<UserRatingProfile> data) {
			return new ConstantBaselinePredictor(value);
		}
	}
}
