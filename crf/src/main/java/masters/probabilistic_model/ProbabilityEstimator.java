package masters.probabilistic_model;

import masters.Constants;

public interface ProbabilityEstimator {
	
	public double getProbabilityEstimation (Object featureValue);
	
	public double[] getDataArr();
	
	public boolean getAllZerosOnInput();
}
