package masters.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import masters.Constants;
import masters.features.Feature;
import masters.probabilistic_model.DiscreteFeatureModel;
import masters.probabilistic_model.DiscreteFeaturePositionModel;
import masters.probabilistic_model.Histogram3DModel;
import masters.probabilistic_model.HistogramModel;

public class ProbabilityEstimationCacheHelper {
	
	private static String DELIMITER = ";";
	private static String NEW_LINE = System.lineSeparator();
	
	public static boolean isCachedPDMs(Feature singleFeature,
			int label) {
				
		String path = getPDMCachePath(singleFeature, label);
		File file = new File(path);
		return file.exists();
	}
	
	public static Histogram3DModel getCachedHistogram3D(Feature singleFeature,
			int label) {
		try {
			String path = getPDMCachePath(singleFeature, label);
			File file = new File(path);
	  
			LineIterator it = FileUtils.lineIterator(file, "UTF-8");

			List<Integer> rgbSize = Arrays.asList(it.nextLine().split(DELIMITER)).stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
			
			double[][][] normalisedHistogram = new double [rgbSize.get(0)][rgbSize.get(1)][rgbSize.get(2)];
			
			for (int r = 0; r < normalisedHistogram.length; r++) {
				for (int g = 0; g < normalisedHistogram[0].length; g++) {
					for (int b = 0; b < normalisedHistogram[0][0].length; b++) {
						normalisedHistogram[r][g][b] = Double.valueOf(it.nextLine());
					}
				}
			}
			return new Histogram3DModel(normalisedHistogram);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return null;
		
		
	}

	public static void cacheHistogram3D(Feature singleFeature, int label,
			Histogram3DModel gmm) {

		String path = getPDMCachePath(singleFeature, label);
		
		double[][][] normalisedHistogram = gmm.getNormalisedHistogram();
		StringBuffer buffer = new StringBuffer();
		buffer.append(normalisedHistogram.length); buffer.append(DELIMITER);
		buffer.append(normalisedHistogram[0].length); buffer.append(DELIMITER);
		buffer.append(normalisedHistogram[0][0].length); buffer.append(DELIMITER);
		buffer.append(NEW_LINE);
		
		for (int r = 0; r < normalisedHistogram.length; r++) {
			for (int g = 0; g < normalisedHistogram[0].length; g++) {
				for (int b = 0; b < normalisedHistogram[0][0].length; b++) {
					buffer.append(normalisedHistogram[r][g][b]);
					buffer.append(NEW_LINE);
				}
			}
		}
		saveStringToFile(path, buffer.toString());
			
	}
	
	public static HistogramModel getCachedHistogram(Feature singleFeature,
			int label) {
		try {
			String path = getPDMCachePath(singleFeature, label);
			File file = new File(path);
	  
			LineIterator it = FileUtils.lineIterator(file, "UTF-8");

			Integer dataLength = Integer.valueOf(it.nextLine().trim());
			
			double[] normalisedHistogram = new double [dataLength];
			
			for (int i = 0; i < normalisedHistogram.length; i++) {
				normalisedHistogram[i] = Double.valueOf(it.nextLine());
			}
			
			return new HistogramModel(normalisedHistogram);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return null;
		
		
	}
	
	public static void cacheHistogram(Feature feature, Integer label,
			HistogramModel gmm) {
		String path = getPDMCachePath(feature, label);
		double[] normalisedHistogram = gmm.getNormalisedHistogram();
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(normalisedHistogram.length);
		buffer.append(NEW_LINE);
		for (int i = 0; i < normalisedHistogram.length; i++) {
			buffer.append(normalisedHistogram[i]);
			buffer.append(NEW_LINE);
		}
		saveStringToFile(path, buffer.toString());
	}
	

	public static DiscreteFeatureModel getCachedDisreteModel(
			Feature singleFeature, int label) {
		try {
			String path = getPDMCachePath(singleFeature, label);
			File file = new File(path);
	  
			LineIterator it = FileUtils.lineIterator(file, "UTF-8");

			List<Integer> cachedValues = Arrays.asList(it.nextLine().split(DELIMITER)).stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
			

			return new DiscreteFeatureModel(cachedValues.get(0), cachedValues.get(1));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void cacheDiscreteModel(Feature feature, Integer label,
			DiscreteFeatureModel pdm) {
		String path = getPDMCachePath(feature, label);
		int numberOfFeatureOnLabel = pdm.getNumberOfFeatureOnLabel();
		int featureOnLabelMaskTotalSize = pdm.getFeatureOnLabelMaskTotalSize();
		String values = numberOfFeatureOnLabel + DELIMITER + featureOnLabelMaskTotalSize;
		saveStringToFile(path, values);
	}
	
	public static void cacheDiscretePositionModel(Feature feature,
			Integer label, DiscreteFeaturePositionModel pdm) {
		String path = getPDMCachePath(feature, label);
		StringBuffer buffer = new StringBuffer();
		
		int featureOnLabelMaskTotalSize = pdm.getFeatureOnLabelMaskTotalSize();
		buffer.append(featureOnLabelMaskTotalSize);
		buffer.append(NEW_LINE);
		Map<String, Integer> featureOnLabelNumber = pdm.getNumberOfFeatureOnLabel();
		for (String featureValue : featureOnLabelNumber.keySet()) {
			int counter = featureOnLabelNumber.get(featureValue);
			buffer.append(featureValue + DELIMITER + counter);
			buffer.append(NEW_LINE);
			
		}
		saveStringToFile(path, buffer.toString());
	}

	public static DiscreteFeaturePositionModel getCachedDisretePositionModel(
			Feature singleFeature, int label) {
		try {
			String path = getPDMCachePath(singleFeature, label);
			File file = new File(path);
	  
			LineIterator it = FileUtils.lineIterator(file, "UTF-8");
			Integer featureOnLabelMaskTotalSize = Integer.valueOf(it.nextLine());
			Map<String, Integer> featureOnLabelNumber = new HashMap<>();
			while(it.hasNext()) {
				String[] definition = it.nextLine().split(DELIMITER);
				featureOnLabelNumber.put(definition[0], Integer.valueOf(definition[1]));
			}
			return new DiscreteFeaturePositionModel(featureOnLabelNumber, featureOnLabelMaskTotalSize);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void cacheFeatureNumbers(
			int numberOfLocalFeatures, int numberOfParwiseFeatures) {
		String path = getFeatureNumbersCachePath();
		String featureNumbers = numberOfLocalFeatures + DELIMITER + numberOfParwiseFeatures;
		saveStringToFile(path, featureNumbers);
	}
	
	public static int[] getCachedFeatureNumbers() {
		String path = getFeatureNumbersCachePath();
		File file = new File(path);
		int[] featureNumbers = new int[2];
  
		
		try {
			LineIterator it = FileUtils.lineIterator(file, "UTF-8");
			String[] featureNumbersStringArr = it.nextLine().split(DELIMITER);
			if (featureNumbersStringArr.length < 2) {
				_log.error("wrong input in file " + path + ". Cannot get feature numbers");
				throw new RuntimeException();
			}
			featureNumbers[0] = Integer.valueOf(featureNumbersStringArr[0]);
			featureNumbers[1] = Integer.valueOf(featureNumbersStringArr[1]);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return featureNumbers;
	}
	
	public static void cacheLabelProbabilities(List<Double> labelProbabilities) {
		String path = getLabelProbabilityCachePath();
		StringBuffer labelBuffer = new StringBuffer();
		for (Double labelProbability : labelProbabilities) {
			labelBuffer.append(labelProbability);
			labelBuffer.append(DELIMITER);
		}
		saveStringToFile(path, labelBuffer.toString());
	}
	
	public static List<Double> getCachedLabelProbabilities() {
		String path = getLabelProbabilityCachePath();
		File file = new File(path);
		LineIterator it;
		List<Double> labelProbabilities = new ArrayList<>();
		try {
			it = FileUtils.lineIterator(file, "UTF-8");
			labelProbabilities = Arrays.asList(it.nextLine().split(DELIMITER)).stream()
					.map(Double::valueOf)
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return labelProbabilities;

	}

	public static void saveStringToFile(String path, String stringToSave) {
		CacheUtils.saveStringToFile(path, stringToSave);
	}
	
	private static String getPDMCachePath(Feature singleFeature, int label) {
		String constants = "grid_" + Constants.GRID_SIZE + "_neigh_" + Constants.NEIGHBOURHOOD_SIZE +
				"_hist_" + Constants.NUMBER_OF_HISTOGRAM_DIVISIONS + "_distance_" + Constants.MEAN_SUPERPIXEL_DISTANCE_MULTIPLIER;
		
		return Constants.WORK_PATH + "cache_pdms" + 
				File.separator + Constants.IMAGE_FOLDER +
				File.separator + "use_histogram_3d_" + Constants.USE_HISTOGRAMS_3D +
				File.separator + constants +
				File.separator + "feature_" + singleFeature.getFeatureIndex() + 
				File.separator + "label_" + label;
	}
	private static String getLabelProbabilityCachePath() {
		
		return Constants.WORK_PATH + "cache_labels" + 
				File.separator + Constants.IMAGE_FOLDER +
				File.separator + "labels";
	}
	
	private static String getFeatureNumbersCachePath() {
		String constants = "grid_" + Constants.GRID_SIZE + "_neigh_" + Constants.NEIGHBOURHOOD_SIZE + "_hist_" + Constants.NUMBER_OF_HISTOGRAM_DIVISIONS;
		
		return Constants.WORK_PATH + "cache_feature_numbers" + 
				File.separator + Constants.IMAGE_FOLDER +
				File.separator + constants;
	}

	
	private static transient Logger _log = Logger.getLogger(ProbabilityEstimationCacheHelper.class);

}
