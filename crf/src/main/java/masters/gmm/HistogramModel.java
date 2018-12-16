package masters.gmm;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import smile.math.Math;

public class HistogramModel implements ProbabilityEstimator{

	static int divisions;
	static final double minVal = 0.0;
	static final double maxVal = 1.0;
	static double step; 
	
	private double [] dataArr;
	
	private int [] histogram;
	private double [] normalisedHistogram;
	

	
	public HistogramModel (double [] dataArr, int numberOfHistogramDivision) {
		divisions = numberOfHistogramDivision;
		step = maxVal / divisions;
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
	
	
	public double[] getNormalisedHistogram() {
		return normalisedHistogram;
	}

	private int getHistogramBlock(double featureValue, double minVal, double maxVal, double step) {
		int dataIndex = 0;
		double border = minVal + step;
		while (border < maxVal) {
			if (featureValue < border) {
				return dataIndex;
			}
			border += step;
			border = round(border, 2);
			dataIndex++;
		}
		return this.histogram.length - 1;		
	}
	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}


	
}
