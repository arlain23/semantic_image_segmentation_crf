package masters.gmm;

import masters.Constants;

public interface ProbabilityEstimator {
	static final int divisions = Constants.NUMBER_OF_DIVISIONS_GMM;
	static final double minVal = 0.0;
	static final double maxVal = 1.0;
	static final double step = maxVal / divisions; 
	
	
	public double getProbabilityEstimation (double featureValue);
	
	public double[] getDataArr();
	
	public boolean getAllZerosOnInput();
}
