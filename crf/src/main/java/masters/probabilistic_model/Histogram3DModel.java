package masters.probabilistic_model;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import masters.Constants;
import smile.math.Math;

public class Histogram3DModel implements ProbabilityEstimator{

	int divisions;
	static final double minVal = 0.0;
	static final double maxVal = 1.0;
	double step; 
	
	private double [] dataArr;
	
	private int [][][] histogram;
	private double [][][] normalisedHistogram;
	
	private int trainingDataLength;
	
	public Histogram3DModel() {
		// empty constructor for further data adding
		divisions = Constants.NUMBER_OF_HISTOGRAM_DIVISIONS;
		step = maxVal / divisions;
		
		this.histogram = new int [divisions][divisions][divisions];
		for (int i = 0; i < divisions; i++) {
			for (int j = 0; j < divisions; j++) {
				for (int k = 0; k < divisions; k++) {
					this.histogram[i][j][k] = 0;
				}
			}
		}
		this.trainingDataLength = 0;
		
	}
	
	public void addData(List<List<Double>> feature3dData) {
		// needed after initialising with empty constructor
		List<Double> redValues = feature3dData.get(0);
		List<Double> greenValues = feature3dData.get(1);
		List<Double> blueValues = feature3dData.get(2);
		
		for (int i = 0; i < redValues.size(); i++) {
			double redValue = redValues.get(i);
			double greenValue = greenValues.get(i);
			double blueValue = blueValues.get(i);
			
			
			int redDataIndex = getHistogramBlock(redValue, minVal, maxVal, step);
			int greenDataIndex = getHistogramBlock(greenValue, minVal, maxVal, step);
			int blueDataIndex = getHistogramBlock(blueValue, minVal, maxVal, step);
			
			int tmp = this.histogram[redDataIndex][greenDataIndex][blueDataIndex];
			tmp++;
			this.histogram[redDataIndex][greenDataIndex][blueDataIndex] = tmp;
		}
		trainingDataLength += redValues.size();
	}
	
	public void normaliseHistogram() {
		normaliseHistogram(this.trainingDataLength);
	}
	public Histogram3DModel (double[][][] normalisedHistogram) {
		divisions = Constants.NUMBER_OF_HISTOGRAM_DIVISIONS;
		step = maxVal / divisions;
		
		this.normalisedHistogram = normalisedHistogram;
		
	}
	
	public Histogram3DModel (double[][] data) {
		divisions = Constants.NUMBER_OF_HISTOGRAM_DIVISIONS;
		step = maxVal / divisions;

		this.histogram = new int [divisions][divisions][divisions];
		for (int i = 0; i < divisions; i++) {
			for (int j = 0; j < divisions; j++) {
				for (int k = 0; k < divisions; k++) {
					this.histogram[i][j][k] = 0;
				}
			}
		}
		
		for (int i = 0; i < data.length; i++) {
			double redValue = data[i][0];
			double greenValue = data[i][1];
			double blueValue = data[i][2];
			
			
			int redDataIndex = getHistogramBlock(redValue, minVal, maxVal, step);
			int greenDataIndex = getHistogramBlock(greenValue, minVal, maxVal, step);
			int blueDataIndex = getHistogramBlock(blueValue, minVal, maxVal, step);
			
			
			int tmp = this.histogram[redDataIndex][greenDataIndex][blueDataIndex];
			tmp++;
			this.histogram[redDataIndex][greenDataIndex][blueDataIndex] = tmp;
		}
		
		
		normaliseHistogram(data.length);
	}

	private void normaliseHistogram(int dataLength) {
		this.normalisedHistogram = new double[divisions][divisions][divisions];
		for (int i = 0; i < divisions; i++) {
			for (int j = 0; j < divisions; j++) {
				for (int k = 0; k < divisions; k++) {
					int tmp = this.histogram[i][j][k];
					normalisedHistogram[i][j][k] = (tmp * 1.0) / dataLength;
				}
			}
		}
	}
	


	@Override
	public double getProbabilityEstimation(Object featureValue) {
		Double[] rgbValues = (Double[]) featureValue;
		double redValue = rgbValues[0];
		double greenValue = rgbValues[1];
		double blueValue = rgbValues[2];
		
		int redDataIndex = getHistogramBlock(redValue, minVal, maxVal, step);
		int greenDataIndex = getHistogramBlock(greenValue, minVal, maxVal, step);
		int blueDataIndex = getHistogramBlock(blueValue, minVal, maxVal, step);
		return this.normalisedHistogram[redDataIndex][greenDataIndex][blueDataIndex];
	}

	
	@Override
	public double[] getDataArr() {
		_log.error("should not call this");
		return this.dataArr;
	}

	@Override
	public boolean getAllZerosOnInput() {
		return false;
	}
	
	
	public double[][][] getNormalisedHistogram() {
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

	private static transient Logger _log = Logger.getLogger(Histogram3DModel.class);



	
}
