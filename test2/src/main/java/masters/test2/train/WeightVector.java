package masters.test2.train;

import java.util.ArrayList;
import java.util.List;

public class WeightVector {
	private List<List<Double>> pixelFeatureWeights;
	private List<Double> pixelSimilarityWeights;
	private List<Double> weights;
	private int numberOfLabels;
	private int numberOfFeatures;
	
	public WeightVector (int numberOfLabels, int numberOfFeatures) {
		//random
		int numberOfWeights = numberOfFeatures * numberOfLabels + 2;
		List<Double> weights = new ArrayList<Double>();
		for (int i = 0; i < numberOfWeights; i++) {
			double randValue = Math.random();
			weights.add(randValue);
		}
		this.numberOfLabels = numberOfLabels;
		this.numberOfFeatures = numberOfFeatures;
		this.weights = weights;
		
		transformWeightList();
	}
	public WeightVector (List<Double> weights, int numberOfLabels, int numberOfFeatures) {
		this.numberOfLabels = numberOfLabels;
		this.numberOfFeatures = numberOfFeatures;
		this.weights = weights;
		
		transformWeightList();
	}
	private void transformWeightList() {
		this.pixelFeatureWeights = new ArrayList<List<Double>>();
		this.pixelSimilarityWeights = new ArrayList<Double>();
		
		int weightIterator = 0;
		for (int i = 0; i < this.numberOfLabels; i++) {
			List<Double> singleFeatureWeights = new ArrayList<Double>();
			for (int j = 0; j < this.numberOfFeatures; j++) {
				singleFeatureWeights.add(weights.get(weightIterator++));
			}
			pixelFeatureWeights.add(singleFeatureWeights);
		}
		// same label
		pixelSimilarityWeights.add(weights.get(weightIterator++));
		// different label
		pixelSimilarityWeights.add(weights.get(weightIterator++));
	}
	
	public WeightVector (List<List<Double>> pixelFeatureWeights, List<Double> pixelSimilarityWeights) {
		this.pixelFeatureWeights = pixelFeatureWeights;
		this.pixelSimilarityWeights = pixelSimilarityWeights;
		
		weights = new ArrayList<Double>();
		numberOfLabels = pixelFeatureWeights.size();
		numberOfFeatures = pixelFeatureWeights.get(0).size();
		for (List<Double> weightsForLabel : pixelFeatureWeights) {
			for (Double featureWeight : weightsForLabel) {
				weights.add(featureWeight);
			}
		}
		for (Double similarityWeight : pixelSimilarityWeights) {
			weights.add(similarityWeight);
		}
	}
	
	public List<Double> getFeatureWeightsForLabel(int label) {
		return pixelFeatureWeights.get(label);
	}
	public Double getPairSimilarityWeight(int label1, int label2) {
		if (label1 == label2) {
			return pixelSimilarityWeights.get(0);
		} else {
			return pixelSimilarityWeights.get(1);
		}
	}
	public List<Double> getWeights() {
		return weights;
	}
	public int getWeightSize() {
		return weights.size();
	}
	
	public List<List<Double>> getPixelFeatureWeights() {
		return pixelFeatureWeights;
	}
	public List<Double> getPixelSimilarityWeights() {
		return pixelSimilarityWeights;
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
