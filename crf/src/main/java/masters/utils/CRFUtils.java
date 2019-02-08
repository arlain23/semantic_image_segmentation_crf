package masters.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.factorisation.Factor;
import masters.factorisation.FactorGraphModel;
import masters.features.BinaryMask;
import masters.features.Continous3DFeature;
import masters.features.ContinousFeature;
import masters.features.DiscreteFeature;
import masters.features.DiscretePositionFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.features.ValueDoubleMask;
import masters.image.ImageDTO;
import masters.image.ImageMask;
import masters.probabilistic_model.GaussianMixtureModel;
import masters.probabilistic_model.ProbabilityEstimator;
import masters.superpixel.LabelException;
import masters.superpixel.SuperPixelDTO;
import masters.train.FeatureVector;
import masters.train.WeightVector;
import smile.stat.distribution.GaussianMixture;

public class CRFUtils {
	public static FeatureVector calculateImageFi(WeightVector weightVector, FactorGraphModel factorGraph, ImageMask mask, 
			ParametersContainer parametersContainer) {
		FeatureVector imageFi = new FeatureVector(weightVector.getWeightSize());

		List<SuperPixelDTO> superPixels = factorGraph.getImage().getSuperPixels();
		Set<Factor> createdFactors = factorGraph.getCreatedFactors();
		for (Factor factor : createdFactors) {
			int leftSuperPixelIndex = factor.getLeftSuperPixelIndex();
			SuperPixelDTO leftSuperPixel = superPixels.get(leftSuperPixelIndex);
			int rightSuperPixelIndex = factor.getRightSuperPixelIndex();
			if (rightSuperPixelIndex < 0) {
				// feature node - local model (R label*feature size)
				FeatureVector localModel = leftSuperPixel.getLocalImageFi(null, mask, factorGraph.getImage(), null, parametersContainer);
				imageFi.add(localModel);
			} else {
				// output node - pairwise model (R2)
				SuperPixelDTO rightSuperPixel = superPixels.get(rightSuperPixelIndex);
				FeatureVector pairWiseModel = leftSuperPixel.getPairwiseImageFi(rightSuperPixel, mask, null, null, factorGraph.getImage(), parametersContainer);
				imageFi.add(pairWiseModel);
			}
		}
		return imageFi;
	}


  /*
   * 	LOCAL IMAGE FI
   */
	public static FeatureVector getLocalImageFi(int superPixelIndex, Integer objectLabel, ImageMask mask, ImageDTO trainImage,
			List<ImageDTO> trainImageList, ParametersContainer parameterContainer,
			List<Feature> localFeatures) {
		
		boolean print = false;
		
		Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = 
				parameterContainer.getProbabilityEstimationDistribution();
		
		FeatureVector imageFi;
		int featureIndex = 0;
		if (objectLabel == null) {
			objectLabel = mask.getMask().get(superPixelIndex);
		}
		if (Constants.USE_NON_LINEAR_MODEL) {
			imageFi = new FeatureVector(parameterContainer.getNumberOfLocalFeatures() + (parameterContainer.getNumberOfPairwiseFeatures() + 1));
			for (Feature feature : localFeatures) {
				/*
				 * 		p(l|f) = p(f|l)*p(l) / p(f)
				 * 		p(f|l) = p(f1|l)* p(f2|l) * .... * p(fn|l)
				 * 		fi = -log p(f|l)
				 * 		p(f) = sum ( p(f|l) * p(l) )
				 */
				
				//  p(f1|l)* p(f2|l) * .... * p(fn|l)
				double featureOnLabelConditionalProbability = 1.0;

				// log p(l)
				double labelProbability = 0;
				
				//log p(f)
				double featureProbability = 0;

				boolean isCurrentProbabilityZero = false;
				for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
					// p(f1|l)* p(f2|l) * .... * p(fn|l)
					boolean isProbabilityZero = true;
					double currentFeatureOnLabelConditionalProbability = 1.0;
					if (feature instanceof FeatureContainer) {
						FeatureContainer featureContainer = (FeatureContainer) feature;
						for (Feature singleFeature : featureContainer.getFeatures()) {
							if (singleFeature.getValue() != null) {
								ProbabilityEstimator currentProbabilityEstimator = null;
								boolean hasAllZeros = false;
								if (probabilityEstimationDistribution.containsKey(singleFeature)) {
									currentProbabilityEstimator = probabilityEstimationDistribution.get(singleFeature).get(label);
									hasAllZeros = currentProbabilityEstimator.getAllZerosOnInput();
								}
								if (!hasAllZeros) {
									if (singleFeature.getValue() != null) {
										double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(mask, trainImage, label, singleFeature, trainImageList, currentProbabilityEstimator);
										if (probabilityFeatureLabel == 0) {
											probabilityFeatureLabel = 1e-6;
										}
										currentFeatureOnLabelConditionalProbability *= probabilityFeatureLabel;
										isProbabilityZero = false;
									} 
								} else {
									isProbabilityZero = true;
								}
							}
						}
					} else {
						if (feature.getValue() != null) {
							ProbabilityEstimator currentProbabilityEstimator = null;
							if (probabilityEstimationDistribution.containsKey(feature)) {
								currentProbabilityEstimator = probabilityEstimationDistribution.get(feature).get(label);
								double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(mask, trainImage, label, feature, trainImageList, currentProbabilityEstimator);
								currentFeatureOnLabelConditionalProbability *= probabilityFeatureLabel;
								isProbabilityZero = false;
								
							} else {
								double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(mask, trainImage, label, feature, trainImageList, currentProbabilityEstimator);
								currentFeatureOnLabelConditionalProbability *= probabilityFeatureLabel;
							}
						}
					}
					
					
					// log p(l)

					double currentLabelProbability = parameterContainer.getLabelProbability(label);

					featureProbability += currentFeatureOnLabelConditionalProbability * currentLabelProbability;
					
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
					if (featureProbability == 0) {
						finalProbability = 1.0 / Constants.NUMBER_OF_STATES;
					}
					finalProbability = -Math.log(finalProbability);
				}
				imageFi.setFeatureValue(featureIndex++, finalProbability);

				
			}
		} else {
			imageFi = new FeatureVector(Constants.NUMBER_OF_STATES * parameterContainer.getNumberOfLocalFeatures() + 2);
			for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
				if (label == objectLabel) {
					for (Feature feature : localFeatures) {
						imageFi.setFeatureValue(featureIndex++, (Double)feature.getValue());
					}
				} else {
					for (@SuppressWarnings("unused") Feature feature : localFeatures) {
						imageFi.setFeatureValue(featureIndex++, 0.0);
					}
				}
			}
		}
		if (print) System.out.println("LOCAL " +objectLabel + "  "+ imageFi);
		return imageFi;
	}


  /*
   * 
   * PAIRWISE IMAGE FI
   * 
   */

	public static FeatureVector getPairwiseImageFi(int superPixelIndex, SuperPixelDTO superPixel, ImageMask mask, Integer label, Integer variableLabel,
			ImageDTO image, SuperPixelDTO neighbouringSuperPixel, ParametersContainer parameterContainer){
		if (Constants.USE_NON_LINEAR_MODEL) {
			return getPairwiseImageFiNonLinear(superPixel, label, variableLabel, image, neighbouringSuperPixel, parameterContainer.getNumberOfLocalFeatures(), parameterContainer.getNumberOfPairwiseFeatures());
		} else {
			return getPairwiseImageFiLinear(superPixel, mask, label, variableLabel, superPixelIndex, parameterContainer.getNumberOfLocalFeatures());
		}
	}

	public static FeatureVector getPairwiseImageFiNonLinear(SuperPixelDTO superPixel, Integer label, Integer variableLabel, ImageDTO image, SuperPixelDTO neighbouringSuperPixel, int numberOfLocalFeatures, int numberOfPairwiseFeatures) {
		FeatureVector imageFi = new FeatureVector(numberOfLocalFeatures + (numberOfPairwiseFeatures + 1));
		int featureIndex = numberOfLocalFeatures;
		for (int i = 0; i < numberOfPairwiseFeatures; i++) {
			double featureValue = 0.0; 
			if (label != variableLabel || Constants.USE_INIT_IMAGE_FOR_PARIWISE_POTENTIAL) {
				featureValue = getPairWiseFeatureTerm(superPixel.getPairwiseFeatureVector().getFeatures().get(i), 
						neighbouringSuperPixel.getPairwiseFeatureVector().getFeatures().get(i), image, label==variableLabel);
			}
			imageFi.setFeatureValue(featureIndex++, featureValue);
		}
		imageFi.setFeatureValue(featureIndex++, 1);
		
		return imageFi;
	}

	private static double getPairWiseFeatureTerm(Feature thisFeature, Feature otherFeature, ImageDTO image, boolean areLabelsTheSame) {
		double beta = image.getBeta(thisFeature);
		double featureDifference = thisFeature.getDifference(otherFeature);
		double featureValue = 0.0;
		if (areLabelsTheSame) {
			featureValue = 1 - Math.exp(-beta * Math.pow(Math.abs(featureDifference), 2));
		} else {
			featureValue = Math.exp(-beta * Math.pow(Math.abs(featureDifference), 2));
		}
		return featureValue;
	}


	public static FeatureVector getPairwiseImageFiLinear(SuperPixelDTO superPixel, ImageMask mask, Integer label1, Integer label2, 
			int superPixelIndex, int numberOfLocalFeatures){
		
		if (label1 == null) {
			label1 = mask.getMask().get(superPixelIndex);
		} 
		if (label2 == null) {
			label2 = superPixel.getLabel();
		}
		FeatureVector imageFi = new FeatureVector(Constants.NUMBER_OF_STATES * numberOfLocalFeatures + 2);
		boolean labelsEquality = label1.equals(label2);
		int labelDiff = (labelsEquality) ? 0 : 1;
		int featureIndex = Constants.NUMBER_OF_STATES * numberOfLocalFeatures;
		imageFi.setFeatureValue(featureIndex++, 1 - labelDiff);
		imageFi.setFeatureValue(featureIndex++, labelDiff);
		return imageFi;
	}
  
	public double getPairSimilarityFeature(int label1, int label2) {
		if (label1 == label2) return 1;
		return 0;
	}
	


	/*
   * 
   * FEATURE PROBABILITY 
   * 
   */


	public static double getFeatureOnLabelProbability(ImageMask mask, ImageDTO trainImage, int objectLabel, Feature feature, 
			List<ImageDTO> trainImageList, ProbabilityEstimator currentProbabilityEstimator) {
		if (feature instanceof DiscreteFeature || feature instanceof DiscretePositionFeature) {
			return  getDiscreteFeatureOnLabelProbability(mask, trainImage, objectLabel, feature, trainImageList, currentProbabilityEstimator);
		} else if (feature instanceof ContinousFeature || feature instanceof Continous3DFeature) {
			return getContinuousFeatureOnLabelProbability(mask, trainImage, objectLabel, feature, trainImageList, currentProbabilityEstimator);
		}

		throw new RuntimeException("Undefined feature type -> " + feature);
	}

	private static double getDiscreteFeatureOnLabelProbability(ImageMask mask, ImageDTO trainImage, int objectLabel, Feature feature,
			List<ImageDTO> trainImageList, ProbabilityEstimator currentProbabilityEstimator) {
		if (trainImageList == null) {
			return getDiscreteFeatureOnLabelProbabilityTraining(mask, trainImage, objectLabel, feature, currentProbabilityEstimator);
		} else {
			return getDisreteFeatureOnLabelProbabilityInference(trainImageList, objectLabel, feature, currentProbabilityEstimator);
		}
	}
  
	private static double getDisreteFeatureOnLabelProbabilityInference(List<ImageDTO> trainImageList, int objectLabel, Feature feature, ProbabilityEstimator currentProbabilityEstimator) {
       
		double probabilityFeatureLabel;
		
		if (currentProbabilityEstimator != null) {
			probabilityFeatureLabel = currentProbabilityEstimator.getProbabilityEstimation(feature.getValue());
		} else {
			int numberOfFeatureOnLabel = 0;
			int featureOnLabelMaskTotalSize = 0;
			
			for (ImageDTO trainingImage : trainImageList) {
				BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), objectLabel);
				BinaryMask featureMask = trainingImage.getDiscreteFeatureBinaryMask(feature);
				if (featureMask != null) {
					BinaryMask featureOnLabelMask = new BinaryMask(featureMask, labelMask);
					numberOfFeatureOnLabel += featureOnLabelMask.getNumberOfOnBytes();
					featureOnLabelMaskTotalSize += featureOnLabelMask.getListSize();
				}
				
			}
			
			probabilityFeatureLabel = Double.valueOf(numberOfFeatureOnLabel) / Double.valueOf(featureOnLabelMaskTotalSize);
			
		}
		return probabilityFeatureLabel;
	}


	private static double getDiscreteFeatureOnLabelProbabilityTraining(ImageMask mask, ImageDTO image, int objectLabel, Feature feature, ProbabilityEstimator currentProbabilityEstimator) {
		 double probabilityFeatureLabel;
		
		if (currentProbabilityEstimator != null) {
			probabilityFeatureLabel = currentProbabilityEstimator.getProbabilityEstimation(feature.getValue());
		} else {
		
			BinaryMask labelMask = new BinaryMask(mask, objectLabel);
		    BinaryMask featureMask = image.getDiscreteFeatureBinaryMask(feature);
		    BinaryMask featureOnLabelMask = new BinaryMask(featureMask, labelMask);
		    
		    probabilityFeatureLabel = Double.valueOf(featureOnLabelMask.getNumberOfOnBytes()) / Double.valueOf(featureOnLabelMask.getListSize());
		}
		
	    return probabilityFeatureLabel;
	}

  
	private static double getContinuousFeatureOnLabelProbability(ImageMask mask, ImageDTO trainImage, int objectLabel,
			Feature feature, List<ImageDTO> trainImageList, ProbabilityEstimator currentProbabilityEstimator) {
		if (trainImageList == null) {
			return getFeatureOnLabelKernelProbabilityTraining(mask, trainImage, objectLabel, feature, currentProbabilityEstimator);
		} else {
			return getFeatureOnLabelKernelProbabilityInference(trainImageList, objectLabel, feature, currentProbabilityEstimator);
		}
	}

 
	private static double getFeatureOnLabelKernelProbabilityInference(List<ImageDTO> trainImageList, 
			int objectLabel, Feature feature,ProbabilityEstimator currentProbabilityEstimator) {
  
		double probabilityFeatureLabel;
		
		if (currentProbabilityEstimator != null) {
			probabilityFeatureLabel = currentProbabilityEstimator.getProbabilityEstimation(feature.getValue());
		} else {
			List<ValueDoubleMask> featureOnLabelMasks = new ArrayList<ValueDoubleMask>();
			for (ImageDTO trainingImage : trainImageList) {
				BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), objectLabel);
				ValueDoubleMask featureMask = trainingImage.getContinuousFeatureValueMask(feature);
				ValueDoubleMask featureOnLabelMask = new ValueDoubleMask(featureMask, labelMask);	
				
				featureOnLabelMasks.add(featureOnLabelMask);
			}
			try {
				probabilityFeatureLabel = getParzenKernelEstimate(feature, featureOnLabelMasks);
			} catch (LabelException e) {
				_log.error(e.getMessage());
				probabilityFeatureLabel = 1.0 / Constants.NUMBER_OF_STATES;
			}
			
		}

		
		return probabilityFeatureLabel;
  
	}

  
	private static double getFeatureOnLabelKernelProbabilityTraining(ImageMask mask, ImageDTO image, int objectLabel, Feature feature,
			ProbabilityEstimator currentProbabilityEstimator) {

		double probabilityFeatureLabel;
		
		if (currentProbabilityEstimator != null) {
			probabilityFeatureLabel = currentProbabilityEstimator.getProbabilityEstimation(feature.getValue());
		} else {
			List<ValueDoubleMask> featureMasks = new ArrayList<ValueDoubleMask>();
			List<ValueDoubleMask> featureOnLabelMasks = new ArrayList<ValueDoubleMask>();
	 
			BinaryMask labelMask = new BinaryMask(image.getImageMask(), objectLabel);
			ValueDoubleMask featureMask = image.getContinuousFeatureValueMask(feature);
			ValueDoubleMask featureOnLabelMask = new ValueDoubleMask(featureMask, labelMask);	
	
			featureMasks.add(featureMask);
			featureOnLabelMasks.add(featureOnLabelMask);
	
			try {
				probabilityFeatureLabel = getParzenKernelEstimate(feature, featureOnLabelMasks);
			} catch (LabelException e) {
				probabilityFeatureLabel = 1.0 / Constants.NUMBER_OF_STATES;
			}
	      
		}

		return probabilityFeatureLabel;
	}

  
	private static double getParzenKernelEstimate(Feature feature, List<ValueDoubleMask> featureMasks) throws LabelException {
    
		
		Double featureValue = (Double)feature.getValue();
		int numberOfTrainingData = 0;
		double output = 0;
		boolean allNull = true;
		for (ValueDoubleMask featureMask : featureMasks) {
			for (int i = 0; i < featureMask.getListSize(); i++) {
				Double trainingFeatureValue = featureMask.getValue(i);
				if (trainingFeatureValue != null) {
					allNull = false;
					numberOfTrainingData++;
					double u = (featureValue - trainingFeatureValue) / Constants.KERNEL_BANDWIDTH;
					output += getKernelValue(u);
				}
			}
		}
		if (allNull) {
			throw new LabelException("Feature " + feature + " not found in an image");
		}
    
		return output / (numberOfTrainingData * Constants.KERNEL_BANDWIDTH);
	}
  
	private static double getKernelValue(double input) {
    
		double prob =  Math.exp( -0.5 * Math.pow(input, 2)) / Math.sqrt(2*Math.PI);
		if (prob == 0) {
		//	_log.error("KERNEL VALUE IS 0 for " + input);
    
		}
		return prob;
	}

  
	private static Logger _log = Logger.getLogger(CRFUtils.class);
}
