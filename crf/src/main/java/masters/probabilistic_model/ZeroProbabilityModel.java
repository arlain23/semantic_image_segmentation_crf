package masters.probabilistic_model;

public class ZeroProbabilityModel implements ProbabilityEstimator{

	@Override
	public double getProbabilityEstimation(Object featureValue) {
		return 0;
	}

	@Override
	public double[] getDataArr() {
		return null;
	}

	@Override
	public boolean getAllZerosOnInput() {
		return true;
	}

}
