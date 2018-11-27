package masters.gmm;

import smile.math.Math;

public class HistogramModel implements ProbabilityEstimator{

	private double [] dataArr;
	
	private int [] histogram;
	private double [] normalisedHistogram;
	

	
	public HistogramModel (double [] dataArr) {
		this.dataArr = dataArr;
		
		
		this.histogram = new int [divisions];
		for (int i = 0; i < divisions; i++) {
			this.histogram[i] = 0;
		}
		
		for (int i = 0; i < dataArr.length; i++) {
			double featureValue = dataArr[i];
			
			int dataIndex = getHistogramBlock(featureValue, minVal, maxVal, step);
			int tmp = this.histogram[dataIndex];
			tmp++;
			this.histogram[dataIndex] = tmp;
		}
		
		this.normalisedHistogram = new double[histogram.length];
		for (int i = 0; i < this.histogram.length; i++) {
			int tmp = histogram[i];
			normalisedHistogram[i] = (tmp * 1.0) / dataArr.length;
			
		}
		
	}
	
	@Override
	public double getProbabilityEstimation(double featureValue) {
		int dataIndex = getHistogramBlock(featureValue, minVal, maxVal, step);
		
		return this.normalisedHistogram[dataIndex];
	}

	
	@Override
	public double[] getDataArr() {
		return dataArr;
	}

	@Override
	public boolean getAllZerosOnInput() {
		return false;
	}
	
	
	private int getHistogramBlock(double featureValue, double minVal, double maxVal, double step) {
		boolean divisionFound = false;
		int dataIndex = 0;
		double border = minVal;
		while (!divisionFound && border < maxVal) {
			if (featureValue < border) {
				divisionFound = true;
				return dataIndex - 1;
			}
			border += step;
			border = round(border, 2);
			dataIndex++;
		}
		return dataIndex - 1;		
	}
	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}


	
}
