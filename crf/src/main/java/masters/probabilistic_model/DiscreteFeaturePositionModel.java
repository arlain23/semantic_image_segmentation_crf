package masters.probabilistic_model;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.utils.Helper;

public class DiscreteFeaturePositionModel implements ProbabilityEstimator{

	private Map<String, Integer> numberOfFeatureOnLabel;
	int featureOnLabelMaskTotalSize;
	
	public DiscreteFeaturePositionModel() {
		this.numberOfFeatureOnLabel = new HashMap<>();
		for (Color color : Constants.AVAILABLE_COLOURS) {
			numberOfFeatureOnLabel.put(Helper.getColorHex(color), 0);
		}
		this.featureOnLabelMaskTotalSize = 0;
	}
	
	public DiscreteFeaturePositionModel(Map<String, Integer> numberOfFeatureOnLabel, int featureOnLabelMaskTotalSize) {
		this.numberOfFeatureOnLabel = new HashMap<>();
		for (Color color : Constants.AVAILABLE_COLOURS) {
			String colorHex = Helper.getColorHex(color);
			if (numberOfFeatureOnLabel.containsKey(colorHex)) {
				this.numberOfFeatureOnLabel.put(colorHex, numberOfFeatureOnLabel.get(colorHex));
			} else {
				this.numberOfFeatureOnLabel.put(colorHex, 0);
			}
		}
		this.featureOnLabelMaskTotalSize = featureOnLabelMaskTotalSize;
	}
	
	public void addData(List<String> discreteValues) {
		for (String discreteValue : discreteValues) {
			Integer featureOnLabels = this.numberOfFeatureOnLabel.get(discreteValue) + 1;
			this.numberOfFeatureOnLabel.put(discreteValue, featureOnLabels);
			this.featureOnLabelMaskTotalSize ++;
		}
		
	}
	


	public Map<String, Integer> getNumberOfFeatureOnLabel() {
		return numberOfFeatureOnLabel;
	}

	public void setNumberOfFeatureOnLabel(
			Map<String, Integer> numberOfFeatureOnLabel) {
		this.numberOfFeatureOnLabel = numberOfFeatureOnLabel;
	}

	public int getFeatureOnLabelMaskTotalSize() {
		return featureOnLabelMaskTotalSize;
	}

	@Override
	public double getProbabilityEstimation(Object featureValue) {
		String discreteValue = (String)featureValue;
		Integer featureOnLabels = this.numberOfFeatureOnLabel.get(discreteValue);
		
		return Double.valueOf(featureOnLabels) / Double.valueOf(featureOnLabelMaskTotalSize);
	    
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
	
	
	private static transient Logger _log = Logger.getLogger(DiscreteFeaturePositionModel.class);



	
}
