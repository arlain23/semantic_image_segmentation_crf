package masters.gmm;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.factorisation.FactorGraphModel;
import smile.stat.distribution.GaussianMixture;


public class GaussianMixtureModel implements ProbabilityEstimator{
	private GaussianMixture gmm;
	private double [] dataArr;
	
	public GaussianMixtureModel (double [] dataArr) {
		this.dataArr = dataArr;
		this.gmm = new GaussianMixture(dataArr);
	}
	
	@Override
	public double[] getDataArr() {
		return dataArr;
	}
	
	@Override
	public double getProbabilityEstimation(double featureValue) {
		if (!Constants.USE_NON_LINEAR_MODEL  || !Constants.USE_GRID_MODEL) {
			_log.error("GMM available only for a grid of neighbours");
			throw new RuntimeException();
		}
		boolean divisionFound = false;
		double border = minVal;
		
		double a = 0;
		double b = 0;
		while (!divisionFound && border < maxVal) {
			if (featureValue < border) {
				divisionFound = true;
				a = border - step;
				b = border;
			}
			border += step;
		}
		return cdf(b) - cdf(a);
	}

	@Override
	public boolean getAllZerosOnInput() {
		return false;
	}
	private double cdf(double x) {
		return this.gmm.cdf(x);
	}
	
	
	private static transient Logger _log = Logger.getLogger(GaussianMixtureModel.class);
}
