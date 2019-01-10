package masters.probabilistic_model;

import java.awt.Color;

import org.apache.log4j.Logger;

public class DiscreteFeatureModel implements ProbabilityEstimator{

	private int numberOfFeatureOnLabel;
	private int featureOnLabelMaskTotalSize;
	
	public DiscreteFeatureModel() {
		this.numberOfFeatureOnLabel = 0;
		this.featureOnLabelMaskTotalSize = 0;
	}
	
	public DiscreteFeatureModel(int numberOfFeatureOnLabel, int featureOnLabelMaskTotalSize) {
		this.numberOfFeatureOnLabel = numberOfFeatureOnLabel;
		this.featureOnLabelMaskTotalSize = featureOnLabelMaskTotalSize;
	}
	
	public void addData(int numberOfFeatureOnLabel, int featureOnLabelMaskTotalSize ) {
		this.numberOfFeatureOnLabel += numberOfFeatureOnLabel;
		this.featureOnLabelMaskTotalSize += featureOnLabelMaskTotalSize;
	}
	

	public int getNumberOfFeatureOnLabel() {
		return numberOfFeatureOnLabel;
	}

	public int getFeatureOnLabelMaskTotalSize() {
		return featureOnLabelMaskTotalSize;
	}

	@Override
	public double getProbabilityEstimation(Object featureValue) {
		return Double.valueOf(numberOfFeatureOnLabel) / Double.valueOf(featureOnLabelMaskTotalSize);
	    
	}

	
	@Override
	public double[] getDataArr() {
		_log.error("should not call this");
		return null;
	}

	@Override
	public boolean getAllZerosOnInput() {
		return false;
	}
	
	
	private static transient Logger _log = Logger.getLogger(DiscreteFeatureModel.class);



	
}
