package masters.gmm;

public class ZeroProbabilityModel implements ProbabilityEstimator{

	@Override
	public double getProbabilityEstimation(double featureValue) {
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
