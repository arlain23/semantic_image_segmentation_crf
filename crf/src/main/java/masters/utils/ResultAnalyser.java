package masters.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.factorisation.Factor;
import masters.factorisation.FactorGraphModel;
import masters.features.DiscreteFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.image.ImageDTO;
import masters.probabilistic_model.ProbabilityEstimator;
import masters.superpixel.SuperPixelDTO;
import masters.train.FeatureVector;

public class ResultAnalyser {
	public static void analyzeEnergyMagnitude(List<ImageDTO> validationImages,
			List<ImageDTO> trainImageList,
			ParametersContainer parameterContainer) {

		double minPairwiseProb = Double.MAX_VALUE;
		double maxPairwiseProb = -1;

		int pairwiseProbDivisor = 0;
		double sumPairwiseProb = 0;

		int pairwiseProbDivisorNoZero = 0;
		double sumPairwiseProbNoZero = 0;

		double minLocalProb = Double.MAX_VALUE;
		double maxLocalProb = -1;

		int localProbDivisor = 0;
		double sumLocalProb = 0;

		int localProbDivisorNoZero = 0;
		double sumLocalProbNoZero = 0;

		for (ImageDTO currentImage : validationImages) {
			List<SuperPixelDTO> superPixels = currentImage.getSuperPixels();
			Set<String> processedPairs = new HashSet<String> ();
			for (SuperPixelDTO superPixel : superPixels) {
				int superPixelIndex = superPixel.getSuperPixelIndex();
				List<SuperPixelDTO> neighbours = superPixel.getNeigbouringSuperPixels();
				for (SuperPixelDTO neighbour : neighbours) {
					int neighbourIndex = neighbour.getSuperPixelIndex();
					String id = superPixelIndex + "_" + neighbourIndex; 
					String id2 = neighbourIndex + "_" + superPixelIndex;

					if (!processedPairs.contains(id) && !processedPairs.contains(id2)) {
						processedPairs.add(id);
						processedPairs.add(id2);

						FeatureVector pairwiseImageFi = superPixel.getPairwiseImageFi(neighbour, 
								null, superPixel.getLabel(), neighbour.getLabel(), currentImage, parameterContainer);

						double pairwiseValue = (double) pairwiseImageFi.getFeatures().get(1).getValue();
						if (!Helper.equals(pairwiseValue, 0.0)) {
							if (pairwiseValue < minPairwiseProb) {
								minPairwiseProb = pairwiseValue;
							}
							if (pairwiseValue > maxPairwiseProb) {
								maxPairwiseProb = pairwiseValue;
							}
							pairwiseProbDivisorNoZero++;
							sumPairwiseProbNoZero += pairwiseValue;
						}
						pairwiseProbDivisor++;
						sumPairwiseProb += pairwiseValue;
					}
				}

				FeatureVector localImageFi = superPixel.getLocalImageFi(superPixel.getLabel(), null, currentImage, 
						trainImageList, parameterContainer);

				double localValue = (double) localImageFi.getFeatures().get(0).getValue();

				if (!Helper.equals(localValue, 0.0)) {
					if (localValue < minLocalProb) {
						minLocalProb = localValue;
					}
					if (localValue > maxLocalProb) {
						maxLocalProb = localValue;
					}
					localProbDivisorNoZero++;
					sumLocalProbNoZero += localValue;
				}
				localProbDivisor++;
				sumLocalProb += localValue;
			}

		}

		double avgLocalProb = sumLocalProb / localProbDivisor;
		double avgLocalProbNoZero = sumLocalProbNoZero / localProbDivisorNoZero;

		double avgPairwiseProb = sumPairwiseProb / pairwiseProbDivisor;
		double avgPairwiseProbNoZero = sumPairwiseProbNoZero / pairwiseProbDivisorNoZero;

		System.out.println("########## LOCAL ##########");
		System.out.println("Minimum local probablity: " + minLocalProb);
		System.out.println("Maximum local probablity: " + maxLocalProb);
		System.out.println("Average local probablity: " + avgLocalProb);
		System.out.println("Average local probablity no zeros: " + avgLocalProbNoZero);

		System.out.println("######## PAIRWISE #########");

		System.out.println("Minimum pairwise probablity: " + minPairwiseProb);
		System.out.println("Maximum pairwise probablity: " + maxPairwiseProb);
		System.out.println("Average pairwise probablity: " + avgPairwiseProb);
		System.out.println("Average pairwise probablity no zeros: " + avgPairwiseProbNoZero);
	}

	public static double assessLabelisationCorrectness(Map<ImageDTO, List<List<Double>>> imageProbabilityMap) {
		return 1.0 - assessLabelisationError(imageProbabilityMap);
	}
	public static double assessLabelisationError(Map<ImageDTO, List<List<Double>>> imageProbabilityMap) {
		int totalNumberOfIncorrectLabels = 0;
		int totalNumberOfSuperPixels = 0;

		for (ImageDTO currentImage : imageProbabilityMap.keySet()) {
			int numberOfIncorrectLabels = 0;

			List<List<Double>> probabilities = imageProbabilityMap.get(currentImage);
			List<SuperPixelDTO> superPixels = currentImage.getSuperPixels();

			// choose the best label : List label -> list of superpixels
			for (int superPixelIndex = 0 ; superPixelIndex < superPixels.size(); superPixelIndex++) {
				int correctLabel = superPixels.get(superPixelIndex).getLabel();

				double maxProbability = -1;
				int bestLabel = -1;
				for (int label  = 0 ; label < Constants.NUMBER_OF_STATES; label++) {
					List<Double> superPixelProbabilities = probabilities.get(label);
					double currentProbability = superPixelProbabilities.get(superPixelIndex);
					if (currentProbability >= maxProbability) {
						maxProbability = currentProbability;
						bestLabel = label;
					}
				}

				if (correctLabel != bestLabel) {
					numberOfIncorrectLabels++;
				}

				totalNumberOfSuperPixels++;
			}

			totalNumberOfIncorrectLabels += numberOfIncorrectLabels;
//			System.out.println("NUMBER OF INCORRECT LABELS " + numberOfIncorrectLabels + " / " + superPixels.size());
		}

		double result = totalNumberOfIncorrectLabels * 1.0;
		return result / totalNumberOfSuperPixels;

	}


	public static Map<ImageDTO, List<List<Double>>> analyseProbabilityDistribution(
			ParametersContainer parameterContainer, List<ImageDTO> testImageList, String baseProbabilityImagePath) {

		boolean saveImages = baseProbabilityImagePath != null;
		Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = parameterContainer.getProbabilityEstimationDistribution();

		Map<ImageDTO, List<List<Double>>> imageProbabilityMap = new HashMap<>();

		int k = 0;
		for (ImageDTO currentImage : testImageList) {
			k++;
			List<SuperPixelDTO> superpixels = currentImage.getSuperPixels();
			List<List<Double>> labelProbabilities = new ArrayList<>();
			for (int objectLabel = 0; objectLabel < Constants.NUMBER_OF_STATES; objectLabel++) {
				List<Double> superPixelProbs = new ArrayList<Double>();
				int i = 0;
				for (SuperPixelDTO superPixel : superpixels) {
					i++;
					Feature feature = superPixel.getLocalFeatureVector().getFeatures().get(0);
					//  p(f1|l)* p(f2|l) * .... * p(fn|l)
					double featureOnLabelConditionalProbability = 1;

					// log p(l)
					double labelProbability = 0;

					//log p(f)
					double featureProbability = 0;

					boolean isCurrentProbabilityZero = false;
					for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
						// p(f1|l)* p(f2|l) * .... * p(fn|l)
						boolean isProbabilityZero = true;
						double currentFeatureOnLabelConditionalProbability = 1;

						if (feature instanceof FeatureContainer) {
							FeatureContainer featureContainer = (FeatureContainer) feature;
							for (Feature singleFeature : featureContainer.getFeatures()) {
								ProbabilityEstimator currentProbabilityEstimator = null;
								boolean hasAllZeros = false;
								if (probabilityEstimationDistribution.containsKey(singleFeature)) {
									currentProbabilityEstimator = probabilityEstimationDistribution.get(singleFeature).get(label);
									hasAllZeros = currentProbabilityEstimator.getAllZerosOnInput();
								}
								if (!hasAllZeros) {
									if (singleFeature.getValue() != null) {
										double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(null, currentImage, label, singleFeature, new ArrayList<>(), currentProbabilityEstimator);
										currentFeatureOnLabelConditionalProbability *= probabilityFeatureLabel;
										isProbabilityZero = false;
									} 
								} else {
									isProbabilityZero = true;
								}
							}
						} else {
							ProbabilityEstimator currentprobabilityEstimator = probabilityEstimationDistribution.get(feature).get(label);
							if (currentprobabilityEstimator != null) {
								if (feature.getValue() != null) {
									double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(null, currentImage, label, feature, new ArrayList<>(), currentprobabilityEstimator);
									currentFeatureOnLabelConditionalProbability *= probabilityFeatureLabel;
									isProbabilityZero = false;
								}
							}
						}

						// log p(l)
						double currentLabelProbability = parameterContainer.getLabelProbability(label);

						featureProbability += currentFeatureOnLabelConditionalProbability * currentLabelProbability;
						
						if (superPixel.getSuperPixelIndex() == 187 &&k ==4) {
//							System.out.println("# L(O)" + objectLabel  + " L:" + label + " " + currentFeatureOnLabelConditionalProbability + " " + currentLabelProbability) ;	
						}
						

						if (label == objectLabel) {
							featureOnLabelConditionalProbability = currentFeatureOnLabelConditionalProbability;
							labelProbability = currentLabelProbability;
							isCurrentProbabilityZero = isProbabilityZero;
						}
					}
					double finalProbability;
					if (isCurrentProbabilityZero) {
						finalProbability = 0;
					} else {
						finalProbability = featureOnLabelConditionalProbability * labelProbability / featureProbability;
						if (superPixel.getSuperPixelIndex() == 187 &&k ==4) {
//							System.out.println("#" + objectLabel + "  " + finalProbability + "		" + featureOnLabelConditionalProbability + "	" +
//									labelProbability + "		" + featureProbability) ;	
						}
						if (featureProbability == 0) {
							finalProbability = 1.0 / Constants.NUMBER_OF_STATES;
						}
					}
					superPixelProbs.add(finalProbability);
				}
				labelProbabilities.add(superPixelProbs);
				
				// save image probabilities
				String imageName = DataHelper.getFileNameFromImageDTO(currentImage);
				String basePath = baseProbabilityImagePath + imageName + "\\";
				if (saveImages) {
					DataHelper.saveImageFi1Probabilities(currentImage, superpixels,  basePath, objectLabel, superPixelProbs);
					DataHelper.saveImageSuperpixelBordersOnly(currentImage, superpixels, basePath +  "image.png");
				}


			}
			imageProbabilityMap.put(currentImage, labelProbabilities);
		}

		double result = assessLabelisationCorrectness(imageProbabilityMap);
//		_log.info("HIST: " + Constants.NUMBER_OF_HISTOGRAM_DIVISIONS + " " + result);

		return imageProbabilityMap;
	}

	private static Logger _log = Logger.getLogger(ResultAnalyser.class);

}
