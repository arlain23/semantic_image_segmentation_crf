package masters.test2.train;

import java.util.List;

import masters.test2.Helper;

public class FeatureVector {
	private List<Double> featureValues;
	public FeatureVector (int featureSize) {
		featureValues = Helper.initFixedSizedListDouble(featureSize);
	}
	
	public List<Double> getFeatureValues() {
		return this.featureValues;
	}
	public void setFeatureValues(List<Double> featureValues) {
		this.featureValues = featureValues;
	}
	public void setFeatureValue(int index, double value) {
		this.featureValues.set(index, value);
	}
	public void add(FeatureVector otherVector) {
		for (int i = 0; i < featureValues.size(); i++) {
			double sum = featureValues.get(i) + otherVector.featureValues.get(i);
			featureValues.set(i, sum);
		}
	}
	public double calculateEnergy(WeightVector weightVector) {
		List<Double> weights = weightVector.getWeights();
		double energy = 0;
		for (int i = 0; i < weights.size(); i++) {
			energy += weights.get(i) * this.featureValues.get(i);
		}
		return energy;
	}
}
