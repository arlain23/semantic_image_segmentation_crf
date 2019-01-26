package masters.probabilistic_model;

import java.awt.Color;
import java.io.File;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.calendar.SingleDaySelectionModel;

import masters.Constants;
import masters.Constants.State;
import masters.cache.ProbabilityCacheException;
import masters.cache.ProbabilityEstimationCacheHelper;
import masters.features.BinaryMask;
import masters.features.Continous3DFeature;
import masters.features.ContinousFeature;
import masters.features.DiscreteFeature;
import masters.features.DiscretePositionFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.features.ValueDoubleMask;
import masters.features.ValueStringMask;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;

public class ProbabilityEstimatorHelper {
	
	private static Integer NUMBER_OF_ON_BYTES_CODE = 0;
	private static Integer TOTAL_LABEL_SIZE_CODE = 1;
	private static String ERROR_MESSAGE = "probability estimation implemented only for continuous, 3D and discrete features";
		
	/*
	 * 	
	 * 	FOR 3D HISTOGRAM ESTIMATION MODEL
	 * 
	 */
	public static Map<Feature, Map<Integer, ProbabilityEstimator>> getProbabilityEstimationDistribution(Feature testFeature, ParametersContainer parameterContainer) {
		Map<Feature, Map<Integer, ProbabilityEstimator>> distribution;
		
		try {
			distribution = getCachedProbabilityEstimationDistribution(testFeature);
			
			List<Double> labelProbabilities = ProbabilityEstimationCacheHelper.getCachedLabelProbabilities();
			
			int[] cachedFeatureNumbers = ProbabilityEstimationCacheHelper.getCachedFeatureNumbers();
			int numberOfLocalFeatures = cachedFeatureNumbers[0];
			int numberOfPairwiseFeatures = cachedFeatureNumbers[1];
			
			parameterContainer.setLabelProbabilities(labelProbabilities);
			parameterContainer.setNumberOfLocalFeatures(numberOfLocalFeatures);
			parameterContainer.setNumberOfParwiseFeatures(numberOfPairwiseFeatures);
			
			_log.info("probability distribution read from cache");
		} catch (ProbabilityCacheException e) {
			System.out.println("genereting new distribution");
			distribution = getNewProbabilityEstimationDistribution(testFeature, parameterContainer);
			_log.info("probability distribution freshly generated");
		}
		
		
		
		
		return distribution;
		
	}
	
	
	public static Map<Feature, Map<Integer, ProbabilityEstimator>> getNewProbabilityEstimationDistribution(Feature testFeature, ParametersContainer parameterContainer) {
		System.out.println("Getting new estimation");
		
		
		Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = new HashMap<>();
		Set<Feature> usedFeatures = initDistributionMap(probabilityEstimationDistribution, testFeature);
		
		Map<Integer, Map<Integer, Integer>> labelProbabilitiesMap = new HashMap<>();
		initLabelProbabilitiesMap(labelProbabilitiesMap);
		
		Map<String, File> trainingFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_PATH);
		Map<String, File> resultFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_RESULT_PATH);
		
		
		boolean featuresNumberAfterInit = false;
		int numberOfLocalFeatures = 0;
		int numberOfParwiseFeatures = 0;
		
		FeatureContainer featureContainer = (FeatureContainer) testFeature;
		Set<Feature> features = prepareFeatures(featureContainer);
		for (String fileName : trainingFiles.keySet()) {
			
			File trainFile = trainingFiles.get(fileName);
			File segmentedFile = resultFiles.get(fileName + Constants.RESULT_IMAGE_SUFFIX);
			
			ImageDTO trainingImage = DataHelper.getSingleImageSegmented(trainFile, segmentedFile, State.TRAIN, parameterContainer);
			
			/*
			 *  generate label probability
			 */
			for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
				BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
				Map<Integer, Integer> variablesToCountersMap = labelProbabilitiesMap.get(label);
				int totalLabelSize = variablesToCountersMap.get(TOTAL_LABEL_SIZE_CODE) + labelMask.getListSize();
				int numberOfOnBytes = variablesToCountersMap.get(NUMBER_OF_ON_BYTES_CODE) + labelMask.getNumberOfOnBytes();
				
				variablesToCountersMap.put(TOTAL_LABEL_SIZE_CODE, totalLabelSize);
				variablesToCountersMap.put(NUMBER_OF_ON_BYTES_CODE, numberOfOnBytes);
				
				labelProbabilitiesMap.put(label, variablesToCountersMap);
			}
			if (!featuresNumberAfterInit) {
				SuperPixelDTO superPixelDTO = trainingImage.getSuperPixels().get(0);
				numberOfLocalFeatures = superPixelDTO.getLocalFeatureVector().getFeatures().size();
				numberOfParwiseFeatures = superPixelDTO.getPairwiseFeatureVector().getFeatures().size();
				featuresNumberAfterInit = true;
			}
			
			/*
			 *  generate feature probability
			 */
			for (Feature singleFeature : features) {
				if (usedFeatures.contains(singleFeature)) {
					probabilityEstimationDistribution.remove(singleFeature);
					continue;
				}
				Map<Integer, ProbabilityEstimator> labelDistributionMap = probabilityEstimationDistribution.get(singleFeature);
				if (singleFeature instanceof Continous3DFeature) { 
					Continous3DFeature continuous3DFeature = (Continous3DFeature) singleFeature;		// RGB values together
					List<Feature> continuousFeatures = continuous3DFeature.getFeatures();
					
					for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
						List<List<Double>> feature3DData = new ArrayList<List<Double>> ();
						for (Feature continuousFeature : continuousFeatures) {
							BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
							ValueDoubleMask featureMask = trainingImage.getContinuousFeatureValueMask(continuousFeature);
							ValueDoubleMask featureOnLabelMask = new ValueDoubleMask(featureMask, labelMask);
							
							// feature data for each superpixel
							List<Double> singleData = new ArrayList<>();
							for (int i = 0; i < featureOnLabelMask.getListSize(); i++) {
								Double trainingFeatureValue = featureOnLabelMask.getValue(i);
								if (trainingFeatureValue != null) {
									singleData.add(trainingFeatureValue);
								}
							}
							feature3DData.add(singleData);
						}
						Histogram3DModel histogram3D = (Histogram3DModel) labelDistributionMap.get(label);
						histogram3D.addData(feature3DData);
						
					}
				} else if (singleFeature instanceof ContinousFeature) {
					ContinousFeature continuousFeature = (ContinousFeature) singleFeature;
					for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
						BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
						ValueDoubleMask featureMask = trainingImage.getContinuousFeatureValueMask(continuousFeature);
						ValueDoubleMask featureOnLabelMask = new ValueDoubleMask(featureMask, labelMask);
						
						// feature data for each superpixel
						List<Double> singleData = new ArrayList<>();
						for (int i = 0; i < featureOnLabelMask.getListSize(); i++) {
							Double trainingFeatureValue = featureOnLabelMask.getValue(i);
							if (trainingFeatureValue != null) {
								singleData.add(trainingFeatureValue);
							}
						}
						HistogramModel histogram = (HistogramModel) labelDistributionMap.get(label);
						histogram.addData(singleData);
					}
					
					
				} else if (singleFeature instanceof DiscreteFeature) {
					for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
						BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
						BinaryMask featureMask = trainingImage.getDiscreteFeatureBinaryMask(singleFeature);
						if (featureMask != null) {
							//BinaryMask featureOnLabelMask = new BinaryMask(featureMask, labelMask);
							BinaryMask featureOnLabelMask = new BinaryMask(labelMask, featureMask);
							
							int numberOfOnBytes = featureOnLabelMask.getNumberOfOnBytes();
							int featureOnLabelMaskSize = labelMask.getNumberOfOnBytes();
							
							DiscreteFeatureModel pdm = (DiscreteFeatureModel) labelDistributionMap.get(label);
							pdm.addData(numberOfOnBytes, featureOnLabelMaskSize);
						}
					}
				} else if (singleFeature instanceof DiscretePositionFeature) {
					DiscretePositionFeature discretePositionFeature = (DiscretePositionFeature) singleFeature;
					for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
						BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
						ValueStringMask featureMask = trainingImage.getDiscretePositionFeatureValueMask(discretePositionFeature);
						ValueStringMask featureOnLabelMask = new ValueStringMask(featureMask, labelMask);
						
						// feature data for each superpixel
						List<String> singleData = new ArrayList<>();
						for (int i = 0; i < featureOnLabelMask.getListSize(); i++) {
							String trainingFeatureValue = featureOnLabelMask.getValue(i);
							if (trainingFeatureValue != null) {
								singleData.add(trainingFeatureValue);
							}
						}
						
						DiscreteFeaturePositionModel pdm = (DiscreteFeaturePositionModel) labelDistributionMap.get(label);
						pdm.addData(singleData);
					}
				}
			}
		}
		
		//get labelProbabilities 
		List<Double> labelProbabilities = new ArrayList<>();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			Map<Integer, Integer> variablesToCountersMap = labelProbabilitiesMap.get(label);
			int totalLabelSize = variablesToCountersMap.get(TOTAL_LABEL_SIZE_CODE);
			int numberOfOnBytes = variablesToCountersMap.get(NUMBER_OF_ON_BYTES_CODE);
			double probabilityLabel = Double.valueOf(numberOfOnBytes) / Double.valueOf(totalLabelSize);
			labelProbabilities.add(probabilityLabel);
		}
		
		//cache
		normaliseAndCachePDMs(probabilityEstimationDistribution);
		cacheFeatureNumbersAndLabelProbabilities(numberOfLocalFeatures, numberOfParwiseFeatures, labelProbabilities);
		
		//set variables
		parameterContainer.setLabelProbabilities(labelProbabilities);
		parameterContainer.setNumberOfLocalFeatures(numberOfLocalFeatures);
		parameterContainer.setNumberOfParwiseFeatures(numberOfParwiseFeatures);
		
		return probabilityEstimationDistribution;
	}
	


	public static Map<Feature, Map<Integer, ProbabilityEstimator>> getCachedProbabilityEstimationDistribution(Feature testFeature) throws ProbabilityCacheException {
		FeatureContainer featureContainer = (FeatureContainer) testFeature;
		Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = new HashMap<>();
		
		Set<Feature> features = prepareFeatures(featureContainer);
		
		for (Feature singleFeature : features) {
			if (singleFeature instanceof Continous3DFeature) {
				Map<Integer, ProbabilityEstimator> labelDistribution = new HashMap<>();
				
				for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
					Histogram3DModel pdm;
					if (ProbabilityEstimationCacheHelper.isCachedPDMs(singleFeature, label)) {
						pdm = ProbabilityEstimationCacheHelper.getCachedHistogram3D(singleFeature, label);
						labelDistribution.put(label, pdm);
					} else {
						_log.error(singleFeature.getFeatureIndex() + " not found");
						throw new ProbabilityCacheException("");
					}
				}
				probabilityEstimationDistribution.put(singleFeature, labelDistribution);
			} else if (singleFeature instanceof ContinousFeature) {
				Map<Integer, ProbabilityEstimator> labelDistribution = new HashMap<>();
				
				for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
					HistogramModel pdm;
					if (ProbabilityEstimationCacheHelper.isCachedPDMs(singleFeature, label)) {
						pdm = ProbabilityEstimationCacheHelper.getCachedHistogram(singleFeature, label);
						labelDistribution.put(label, pdm);
					} else {
						_log.error(singleFeature.getFeatureIndex() + " not found");
						throw new ProbabilityCacheException("");
					}
				}
				probabilityEstimationDistribution.put(singleFeature, labelDistribution);
				
			} else if (singleFeature instanceof DiscreteFeature) {
				Map<Integer, ProbabilityEstimator> labelDistribution = new HashMap<>();
				
				for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
					DiscreteFeatureModel pdm;
					if (ProbabilityEstimationCacheHelper.isCachedPDMs(singleFeature, label)) {
						pdm = ProbabilityEstimationCacheHelper.getCachedDisreteModel(singleFeature, label);
						labelDistribution.put(label, pdm);
					} else {
						_log.error(singleFeature.getFeatureIndex() + " not found");
						throw new ProbabilityCacheException("");
					}
				}
				probabilityEstimationDistribution.put(singleFeature, labelDistribution);
				
			} else if (singleFeature instanceof DiscretePositionFeature) {
				Map<Integer, ProbabilityEstimator> labelDistribution = new HashMap<>();
				
				for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
					DiscreteFeaturePositionModel pdm;
					if (ProbabilityEstimationCacheHelper.isCachedPDMs(singleFeature, label)) {
						pdm = ProbabilityEstimationCacheHelper.getCachedDisretePositionModel(singleFeature, label);
						labelDistribution.put(label, pdm);
					} else {
						_log.error(singleFeature.getFeatureIndex() + " not found");
						throw new ProbabilityCacheException("");
					}
				}
				probabilityEstimationDistribution.put(singleFeature, labelDistribution);
				
			} else {_log.error(ERROR_MESSAGE); throw new RuntimeException(); }
		}
		return probabilityEstimationDistribution;
			
	}

	private static Set<Feature> initDistributionMap(Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution, Feature testFeature) {
		
		Set<Feature> usedFeatures = new HashSet<>();
		
		FeatureContainer featureContainer = (FeatureContainer) testFeature;
		Set<Feature> features = prepareFeatures(featureContainer);
		for (Feature singleFeature : features) {
			HashMap<Integer, ProbabilityEstimator> labelDistribution = new HashMap<>();
			for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
				if (ProbabilityEstimationCacheHelper.isCachedPDMs(singleFeature, label)) {
					usedFeatures.add(singleFeature);
				} else {
					if (singleFeature instanceof Continous3DFeature) {
						labelDistribution.put(label, new Histogram3DModel());
					} else if (singleFeature instanceof ContinousFeature) {
						labelDistribution.put(label, new HistogramModel());
					} else if (singleFeature instanceof DiscreteFeature) {
						labelDistribution.put(label, new DiscreteFeatureModel());
					} else if (singleFeature instanceof DiscretePositionFeature) {
						labelDistribution.put(label, new DiscreteFeaturePositionModel());
					} else {_log.error(ERROR_MESSAGE); throw new RuntimeException(); }
				}
			}
			probabilityEstimationDistribution.put(singleFeature, labelDistribution);
				
		}		
		return usedFeatures;
	}


	private static Set<Feature> prepareFeatures(
			FeatureContainer featureContainer) {
		List<Feature> featureList = featureContainer.getFeatures();
		Set<Feature> features = new HashSet<>(featureList);
		int featureIndex = featureList.get(featureList.size() - 1).getFeatureIndex();
		if (Constants.ADD_COLOUR_LOCAL_FEATURE && !Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION) {
			// init all descrete values
			for (Color color : Constants.AVAILABLE_COLOURS_SET){
				String baseHex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
				features.add(new DiscreteFeature(baseHex, ++featureIndex));
			}
		}
		return features;
	}
	private static void initLabelProbabilitiesMap(
			Map<Integer, Map<Integer, Integer>> labelProbabilitiesMap) {
		
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			Map<Integer, Integer> variablesToCountersMap = new HashMap<>();
			variablesToCountersMap.put(NUMBER_OF_ON_BYTES_CODE, 0);
			variablesToCountersMap.put(TOTAL_LABEL_SIZE_CODE, 0);
			
			labelProbabilitiesMap.put(label, variablesToCountersMap);
		}
		
		
	}
	
	private static void normaliseAndCachePDMs(Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution) {
		for (Feature feature : probabilityEstimationDistribution.keySet()) {
			Map<Integer, ProbabilityEstimator> labelDistribution = probabilityEstimationDistribution.get(feature);
			for (Integer label : labelDistribution.keySet()) {
				if (feature instanceof Continous3DFeature) {
					Histogram3DModel histogram3DModel = (Histogram3DModel) labelDistribution.get(label);
					histogram3DModel.normaliseHistogram();
					ProbabilityEstimationCacheHelper.cacheHistogram3D(feature, label, histogram3DModel);
				} else if (feature instanceof ContinousFeature) {
					HistogramModel histogramModel = (HistogramModel) labelDistribution.get(label);
					histogramModel.normaliseHistogram();
					ProbabilityEstimationCacheHelper.cacheHistogram(feature, label, histogramModel);
				} else if (feature instanceof DiscreteFeature) {
					DiscreteFeatureModel discreteModel = (DiscreteFeatureModel) labelDistribution.get(label);
					ProbabilityEstimationCacheHelper.cacheDiscreteModel(feature, label, discreteModel);
				} else if (feature instanceof DiscretePositionFeature) {
					DiscreteFeaturePositionModel discretePositionModel = (DiscreteFeaturePositionModel) labelDistribution.get(label);
					ProbabilityEstimationCacheHelper.cacheDiscretePositionModel(feature, label, discretePositionModel);
				} else {_log.error(ERROR_MESSAGE); throw new RuntimeException(); }
			}
				
		}
	}
	
	private static void cacheFeatureNumbersAndLabelProbabilities(
			int numberOfLocalFeatures, int numberOfParwiseFeatures,
			List<Double> labelProbabilities) {
		
		ProbabilityEstimationCacheHelper.cacheFeatureNumbers(numberOfLocalFeatures, numberOfParwiseFeatures);
		ProbabilityEstimationCacheHelper.cacheLabelProbabilities(labelProbabilities);
	}
	
	
	
	/*
	 * 
	 * 		OLD: FOR DIFFERENT ESTIMATION MODELS
	 * 
	 */
	
	
	public static Map<Feature, Map<Integer, ProbabilityEstimator>> getProbabilityEstimationDistribution(
			List<ImageDTO> trainImageList,
			Feature testFeature) {
		
		Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = new HashMap<>();
		
		if (testFeature instanceof FeatureContainer) {
				FeatureContainer featureContainer = (FeatureContainer) testFeature;
				for (Feature singleFeature : featureContainer.getFeatures()) {
					if (singleFeature instanceof ContinousFeature) {
						Map<Integer, ProbabilityEstimator> labelDistribution = new HashMap<>();
						for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
							
							List<Double> data = new ArrayList<>();
							List<ValueDoubleMask> featureOnLabelMasks = new ArrayList<ValueDoubleMask>(); 
							for (ImageDTO trainingImage : trainImageList) {
								BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
								ValueDoubleMask featureMask = trainingImage.getContinuousFeatureValueMask(singleFeature);
								ValueDoubleMask featureOnLabelMask = new ValueDoubleMask(featureMask, labelMask);
								
								featureOnLabelMasks.add(featureOnLabelMask);
							}
							for (ValueDoubleMask featureMask : featureOnLabelMasks) {
								for (int i = 0; i < featureMask.getListSize(); i++) {
									Double trainingFeatureValue = featureMask.getValue(i);
									if (trainingFeatureValue != null) {
										data.add(trainingFeatureValue);
									}
								}
							}
							// gaussian mixture
							double[] dataArr = new double[data.size()];
							boolean allzeros = true;
							for (int i = 0; i < data.size(); i++) {
								dataArr[i] = data.get(i);
								if (data.get(i) != 0) {
									allzeros = false;
								}
							}
							if (Constants.USE_GMM_ESTIMATION) {
								if (allzeros) {
									labelDistribution.put(label, new ZeroProbabilityModel());
								} else {
									GaussianMixtureModel gmm = new GaussianMixtureModel(dataArr);
									labelDistribution.put(label, gmm);
								}
							} else if (Constants.USE_HISTOGRAM_ESTIMATION) {
								HistogramModel gmm = new HistogramModel(dataArr);
								labelDistribution.put(label, gmm);
							} else {
								_log.error("Choose estimation mode: histogram or gmm! ");
								throw new RuntimeException();
							}
						}
						probabilityEstimationDistribution.put(singleFeature, labelDistribution);
					} else if (singleFeature instanceof Continous3DFeature) {
						Continous3DFeature continuous3DFeature = (Continous3DFeature) singleFeature;
						Map<Integer, ProbabilityEstimator> labelDistribution = new HashMap<>();
						List<Feature> continuousFeatures = continuous3DFeature.getFeatures();

						for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
							Histogram3DModel gmm;
							if (ProbabilityEstimationCacheHelper.isCachedPDMs(singleFeature, label)) {
								gmm = ProbabilityEstimationCacheHelper.getCachedHistogram3D(singleFeature, label);
							} else {
							
								List<List<Double>> data = new ArrayList<>();
								
								for (Feature continuousFeature : continuousFeatures) {
									List<Double> singleData = new ArrayList<>();
									List<ValueDoubleMask> featureOnLabelMasks = new ArrayList<ValueDoubleMask>(); 
									
									for (ImageDTO trainingImage : trainImageList) {
										BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
										ValueDoubleMask featureMask = trainingImage.getContinuousFeatureValueMask(continuousFeature);
										ValueDoubleMask featureOnLabelMask = new ValueDoubleMask(featureMask, labelMask);
										
										featureOnLabelMasks.add(featureOnLabelMask);
									}
									for (ValueDoubleMask featureMask : featureOnLabelMasks) {
										for (int i = 0; i < featureMask.getListSize(); i++) {
											Double trainingFeatureValue = featureMask.getValue(i);
											if (trainingFeatureValue != null) {
												singleData.add(trainingFeatureValue);
											}
										}
									}
									data.add(singleData);
								}
								
								double[][] dataArr = new double[data.get(0).size()][3];
								for (int i = 0; i < data.get(0).size(); i++) {
									dataArr[i] = new double[]{data.get(0).get(i), data.get(1).get(i),data.get(2).get(i)};
								}
								
								// histogram model
								if (Constants.USE_HISTOGRAM_ESTIMATION) {
									gmm = new Histogram3DModel(dataArr);
									ProbabilityEstimationCacheHelper.cacheHistogram3D(singleFeature, label, gmm);
								} else {
									_log.error("Continuous 3D features not supported for mode different than Histogram ");
									throw new RuntimeException();
								}
								dataArr = null;
								data = null;
							}
							labelDistribution.put(label, gmm);
						}
						probabilityEstimationDistribution.put(singleFeature, labelDistribution);
					}
				}
		  }
		return probabilityEstimationDistribution;
	}
	
	private static Logger _log = Logger.getLogger(ProbabilityEstimatorHelper.class);
	
}
