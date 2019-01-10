package masters.probabilistic_model;

import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import masters.Constants;
import smile.math.Math;

public class HistogramModel implements ProbabilityEstimator{

	int divisions;
	static final double minVal = 0.0;
	static final double maxVal = 1.0;
	double step; 
	
	private double [] dataArr;
	
	private int [] histogram;
	private double [] normalisedHistogram;
	
	private int trainingDataLength;
	
	public HistogramModel() {
		// empty constructor for further data adding
		divisions = Constants.NUMBER_OF_HISTOGRAM_DIVISIONS;
		step = maxVal / divisions;
		
		this.histogram = new int [divisions];
		for (int i = 0; i < divisions; i++) {
			this.histogram[i] = 0;
		}
		this.trainingDataLength = 0;
	}
	
	public HistogramModel (double[] normalisedHistogram) {
		divisions = Constants.NUMBER_OF_HISTOGRAM_DIVISIONS;
		step = maxVal / divisions;
		
		this.normalisedHistogram = normalisedHistogram;
	}
	
	public void addData(List<Double> colourValues) {
		// needed after initialising with empty constructor
		for (int i = 0; i < colourValues.size(); i++) {
			double value = colourValues.get(i);
			
			int dataIndex = getHistogramBlock(value, minVal, maxVal, step);
			
			int tmp = this.histogram[dataIndex];
			tmp++;
			this.histogram[dataIndex] = tmp;
		}
		trainingDataLength += colourValues.size();
	}
	
	public void normaliseHistogram() {
		normaliseHistogram(this.trainingDataLength);
	}
	
	private void normaliseHistogram(int dataLength) {
		this.normalisedHistogram = new double[divisions];
		for (int i = 0; i < divisions; i++) {
			int tmp = this.histogram[i];
			normalisedHistogram[i] = (tmp * 1.0) / dataLength;
		}
	}
	
	@Override
	public double getProbabilityEstimation(Object featureValue) {
		
		int dataIndex = getHistogramBlock((Double)featureValue, minVal, maxVal, step);
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
			if (featureValue <= border) {
				return dataIndex;
			}
			border += step;
			//border = round(border, 2);
			dataIndex++;
		}
		return this.divisions - 1;			
	}
	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}


	
}
