package masters.test2.train;

import java.util.ArrayList;
import java.util.List;

import masters.test2.Constants;

public class WeightVector {
	private List<Double> weights;
	
	public WeightVector (int numberOfLabels, int numberOfLocalFeatures, int numberOfPairwiseFeatures) {
		//random
		int numberOfWeights;
		if (Constants.USE_NON_LINEAR_MODEL) {
			numberOfWeights = numberOfLocalFeatures + numberOfPairwiseFeatures + 1;
		} else {
			numberOfWeights = numberOfLocalFeatures * numberOfLabels + 2;
		}
		List<Double> weights = new ArrayList<Double>();
		for (int i = 0; i < numberOfWeights; i++) {
			double randValue = Math.random();
			weights.add(randValue);
		}
		this.weights = weights;
	}
	public WeightVector (List<Double> weights) {
		this.weights = weights;
	}
	public WeightVector (List<List<Double>> pixelFeatureWeights, List<Double> pixelSimilarityWeights) {
		
		weights = new ArrayList<Double>();
		for (List<Double> weightsForLabel : pixelFeatureWeights) {
			for (Double featureWeight : weightsForLabel) {
				weights.add(featureWeight);
			}
		}
		for (Double similarityWeight : pixelSimilarityWeights) {
			weights.add(similarityWeight);
		}
	}
	
	public List<Double> getWeights() {
		return weights;
	}
	public int getWeightSize() {
		return weights.size();
	}
	
	public void scaleResults() {
		double max = 0;
		for (int i = 0; i < 6; i++) {
			double a = Math.abs(weights.get(i));
			if (a > max) max = a;
		}
		for (int i = 0; i < 6; i++) {
			double value = (this.weights.get(i) / max);
			this.weights.set(i, value);
		}
		this.weights.set(6, 0.0);
		this.weights.set(7, 0.0);
	}
	public void normalise() {
		double max = 0;
		for (int i = 0; i < 6; i++) {
			double a = Math.abs(weights.get(i));
			if (a > max) max = a;
		}
		for (int i = 0; i < 6; i++) {
			double value = (this.weights.get(i) / max);
			this.weights.set(i, value);
		}
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Double w : weights) {
			sb.append(w + " ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	
	
}
