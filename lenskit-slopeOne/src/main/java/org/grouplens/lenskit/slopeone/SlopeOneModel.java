package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.params.meta.DefaultBuilder;

@Built
@DefaultBuilder (SlopeOneModelBuilder.class)
public class SlopeOneModel {
	
	private CoratingMatrix coMatrix;
	private DeviationMatrix devMatrix;
	private BaselinePredictor baseline;
	
	public SlopeOneModel(CoratingMatrix coData, DeviationMatrix devData, BaselinePredictor predictor) {
		coMatrix = coData;
		devMatrix = devData;
		baseline = predictor;
	}
	
	public CoratingMatrix getCoratingMatrix() {
		return coMatrix;
	}
	
	public DeviationMatrix getDeviationMatrix() {
		return devMatrix;
	}
	
	public BaselinePredictor getBaselinePredictor() {
		return baseline;
	}

}
