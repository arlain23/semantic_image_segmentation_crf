package masters.gmm;

import masters.Constants;

public interface ProbabilityEstimator {
	
	public double getProbabilityEstimation (double featureValue);
	
	public double[] getDataArr();
	
	public boolean getAllZerosOnInput();
}
