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
import masters.features.ContinousFeature;
import masters.features.DiscreteFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.features.ValueMask;
import masters.image.ImageDTO;
import masters.image.ImageMask;
import masters.superpixel.LabelException;
import masters.superpixel.SuperPixelDTO;
import masters.train.FeatureVector;
import masters.train.WeightVector;

public class CRFUtils {
	public static FeatureVector calculateImageFi(WeightVector weightVector, FactorGraphModel factorGraph, ImageMask mask, ParametersContainer probabiltyContainer) {
		FeatureVector imageFi = new FeatureVector(weightVector.getWeightSize());

		List<SuperPixelDTO> superPixels = factorGraph.getSuperPixels();
		Set<Factor> createdFactors = factorGraph.getCreatedFactors();
		for (Factor factor : createdFactors) {
			int leftSuperPixelIndex = factor.getLeftSuperPixelIndex();
			SuperPixelDTO leftSuperPixel = superPixels.get(leftSuperPixelIndex);
			int rightSuperPixelIndex = factor.getRightSuperPixelIndex();
			if (rightSuperPixelIndex < 0) {
				// feature node - local model (R label*feature size)
				FeatureVector localModel = leftSuperPixel.getLocalImageFi(null, mask, factorGraph, null, probabiltyContainer);
				imageFi.add(localModel);
			} else {
				// output node - pairwise model (R2)
				SuperPixelDTO rightSuperPixel = superPixels.get(rightSuperPixelIndex);
				FeatureVector pairWiseModel = leftSuperPixel.getPairwiseImageFi(rightSuperPixel, mask, null, null, factorGraph);
				imageFi.add(pairWiseModel);
			}
		}
	return imageFi;
	}


  /*
   * 	LOCAL IMAGE FI
   */

	public static FeatureVector getLocalImageFi(int superPixelIndex, Integer objectLabel, ImageMask mask, FactorGraphModel factorGraph,
			Map<ImageDTO, FactorGraphModel> trainingDataimageToFactorGraphMap, ParametersContainer probabiltyContainer,
			List<Feature> localFeatures, int numberOfLocalFeatures, int numberOfPairwiseFeatures) {
		FeatureVector imageFi;
		int featureIndex = 0;
		if (objectLabel == null) {
			objectLabel = mask.getMask().get(superPixelIndex);
		}
		if (Constants.USE_NON_LINEAR_MODEL) {
			imageFi = new FeatureVector(numberOfLocalFeatures + (numberOfPairwiseFeatures + 1));
			for (Feature feature : localFeatures) {
				if (feature instanceof FeatureContainer) {
					FeatureContainer featureContainer = (FeatureContainer) feature;
					double featureSum = 0;
					for (Feature singleFeature : featureContainer.getFeatures()) {
						double featureValue = CRFUtils.getFeatureProbability(mask, factorGraph, objectLabel, singleFeature, trainingDataimageToFactorGraphMap, probabiltyContainer);
						featureValue = -Math.log(featureValue);
						featureSum += featureValue;
					}
					imageFi.setFeatureValue(featureIndex++, featureSum);
				} else {
					double featureValue = CRFUtils.getFeatureProbability(mask, factorGraph, objectLabel, feature, trainingDataimageToFactorGraphMap, probabiltyContainer);
					featureValue = -Math.log(featureValue);
					imageFi.setFeatureValue(featureIndex++, featureValue);
				}
			}
		} else {
			imageFi = new FeatureVector(Constants.NUMBER_OF_STATES * numberOfLocalFeatures + 2);
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
		return imageFi;
	}


  /*
   * 
   * PAIRWISE IMAGE FI
   * 
   */

	public static FeatureVector getPairwiseImageFi(int superPixelIndex, SuperPixelDTO superPixel, ImageMask mask, Integer label, Integer variableLabel, FactorGraphModel factorGraph, List<Feature> pairwiseFeatures, int numberOfLocalFeatures, int numberOfPairwiseFeatures){
		if (Constants.USE_NON_LINEAR_MODEL) {
			return getPairwiseImageFiNonLinear(superPixel, factorGraph, pairwiseFeatures, numberOfLocalFeatures, numberOfPairwiseFeatures);
		} else {
			return getPairwiseImageFiLinear(superPixel, mask, label, variableLabel, superPixelIndex, numberOfPairwiseFeatures);
		}
	}

	public static FeatureVector getPairwiseImageFiNonLinear(SuperPixelDTO superPixel, FactorGraphModel factorGraph, List<Feature> pairwiseFeatures, int numberOfLocalFeatures, int numberOfPairwiseFeatures) {
		FeatureVector imageFi = new FeatureVector(numberOfLocalFeatures + (numberOfPairwiseFeatures + 1));
		int featureIndex = numberOfLocalFeatures;
		for (int i = 0; i < numberOfPairwiseFeatures; i++) {
			double featureValue = getPairWiseFeatureTerm(pairwiseFeatures.get(i), 
					superPixel.getPairwiseFeatureVector().getFeatures().get(i), factorGraph);
			imageFi.setFeatureValue(featureIndex++, featureValue);
		}
		imageFi.setFeatureValue(featureIndex++, 1);
		
		return imageFi;
	}

	private static double getPairWiseFeatureTerm(Feature thisFeature, Feature otherFeature, FactorGraphModel factorGraph) {
		double beta = factorGraph.getBeta(thisFeature);
		double featureDifference = thisFeature.getDifference(otherFeature);
		double featureValue = Math.exp(-beta * Math.pow(Math.abs(featureDifference), 2));
		return featureValue;
	}


	public static FeatureVector getPairwiseImageFiLinear(SuperPixelDTO superPixel, ImageMask mask, Integer label1, Integer label2, int superPixelIndex, int numberOfLocalFeatures){
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


	public static double getFeatureProbability(ImageMask mask, FactorGraphModel factorGraph, int objectLabel, Feature feature, 
			Map<ImageDTO, FactorGraphModel> trainingDataimageToFactorGraphMap, ParametersContainer probabiltyContainer) {
		if (feature instanceof DiscreteFeature) {
			return getDiscreteFeatureProbability(mask, factorGraph, objectLabel, feature, trainingDataimageToFactorGraphMap, probabiltyContainer);
		} else if (feature instanceof ContinousFeature) {
			return getContinuousFeatureProbability(mask, factorGraph, objectLabel, feature, trainingDataimageToFactorGraphMap, probabiltyContainer);
		}
		throw new RuntimeException("Undefined feature type -> " + feature);
	}

	private static double getDiscreteFeatureProbability(ImageMask mask, FactorGraphModel factorGraph, int objectLabel, Feature feature, Map<ImageDTO, FactorGraphModel> trainingDataimageToFactorGraphMap, ParametersContainer probabiltyContainer) {
		if (trainingDataimageToFactorGraphMap == null) {
			return getDiscreteFeatureProbabilityTraining(mask, factorGraph, objectLabel, feature, probabiltyContainer);
		} else {
			return getDisreteFeatureProbabilityInference(trainingDataimageToFactorGraphMap, objectLabel, feature, probabiltyContainer);
		}
	}
  
	private static double getDisreteFeatureProbabilityInference(Map<ImageDTO, FactorGraphModel> trainingDataimageToFactorGraphMap, int objectLabel, Feature feature, ParametersContainer probabiltyContainer) {
       
		double probablityCurrentLabel = 0;
		double probabilityFeatureCurrentLabel = 0;
		double probabilityFeature = 0;
    
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			double probabilityLabel = probabiltyContainer.getLabelProbability(label);
			int numberOfFeatureOnLabel = 0;
			int featureOnLabelMaskTotalSize = 0;

			for (ImageDTO trainingImage : trainingDataimageToFactorGraphMap.keySet()) {
				FactorGraphModel trainingFactorGraph = trainingDataimageToFactorGraphMap.get(trainingImage);
				BinaryMask labelMask = new BinaryMask(trainingFactorGraph.getImageMask(), label);
				BinaryMask featureMask = trainingFactorGraph.getDiscreteFeatureBinaryMask(feature);
				if (featureMask != null) {
					BinaryMask featureOnLabelMask = new BinaryMask(featureMask, labelMask);
					numberOfFeatureOnLabel += featureOnLabelMask.getNumberOfOnBytes();
					featureOnLabelMaskTotalSize += featureOnLabelMask.getListSize();
				}
			}
      
			double probabilityFeatureLabel = Double.valueOf(numberOfFeatureOnLabel) / Double.valueOf(featureOnLabelMaskTotalSize);

			if (label == objectLabel) {
				probablityCurrentLabel = probabilityLabel;
				probabilityFeatureCurrentLabel = probabilityFeatureLabel;
			}

			probabilityFeature += probabilityFeatureLabel * probabilityLabel;
		}
		
		// p (l|f) = (p(f|l)*p(l)/p(f)
		double probabilityLabelFeature = (probabilityFeatureCurrentLabel * probablityCurrentLabel) / probabilityFeature;
		return probabilityLabelFeature;
	}


	private static double getDiscreteFeatureProbabilityTraining(ImageMask mask, FactorGraphModel factorGraph, int objectLabel, Feature feature, ParametersContainer probabiltyContainer) {
		BinaryMask labelMask = new BinaryMask(mask, objectLabel);
	    BinaryMask featureMask = factorGraph.getDiscreteFeatureBinaryMask(feature);
	    BinaryMask featureOnLabelMask = new BinaryMask(featureMask, labelMask);
	    
	    double probabilityLabel = probabiltyContainer.getLabelProbability(objectLabel);
	    double probabilityFeatureLabel = Double.valueOf(featureOnLabelMask.getNumberOfOnBytes()) / Double.valueOf(featureOnLabelMask.getListSize());
    
	    // get probability feature 
	    double probabilityFeature = 0;
	    for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
	    	BinaryMask currentLabelMask = new BinaryMask(mask, label);
	    	BinaryMask currentFeatureMask = factorGraph.getDiscreteFeatureBinaryMask(feature);
	    	BinaryMask currentFeatureOnLabelMask = new BinaryMask(currentFeatureMask, currentLabelMask);
      
	    	double currentProbabilityLabel =  probabiltyContainer.getLabelProbability(label);
	    	double currentProbabilityFeatureLabel = Double.valueOf(currentFeatureOnLabelMask.getNumberOfOnBytes()) / Double.valueOf(currentFeatureOnLabelMask.getListSize());
	    	probabilityFeature += currentProbabilityLabel * currentProbabilityFeatureLabel;
	    }

	    // p (l|f) = (p(f|l)*p(l)/p(f)
	    double probabilityLabelFeature = (probabilityFeatureLabel * probabilityLabel) / probabilityFeature;
	    return probabilityLabelFeature;
	}

  
	private static double getContinuousFeatureProbability(ImageMask mask, FactorGraphModel factorGraph, int objectLabel,
			Feature feature, Map<ImageDTO, FactorGraphModel> trainingDataimageToFactorGraphMap, ParametersContainer probabiltyContainer) {
		if (trainingDataimageToFactorGraphMap == null) {
			return getFeatureKernelProbabilityTraining(mask, factorGraph, objectLabel, feature, probabiltyContainer);
		} else {
			return getFeatureKernelProbabilityInference(trainingDataimageToFactorGraphMap, objectLabel, feature, probabiltyContainer);
		}
	}

 
	private static double getFeatureKernelProbabilityInference(Map<ImageDTO, FactorGraphModel> trainingDataimageToFactorGraphMap, int objectLabel, Feature feature,
			ParametersContainer probabiltyContainer) {
		double probablityCurrentLabel = 0;
		double probabilityFeatureCurrentLabel = 0;
		double probabilityFeature = 0;
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			double probabilityLabel = probabiltyContainer.getLabelProbability(label);
			List<ValueMask> featureMasks = new ArrayList<ValueMask>();
			List<ValueMask> featureOnLabelMasks = new ArrayList<ValueMask>();
      
			for (ImageDTO trainingImage : trainingDataimageToFactorGraphMap.keySet()) {
				FactorGraphModel trainingFactorGraph = trainingDataimageToFactorGraphMap.get(trainingImage);
				BinaryMask labelMask = new BinaryMask(trainingFactorGraph.getImageMask(), label);
				ValueMask featureMask = trainingFactorGraph.getContinuousFeatureValueMask(feature);
				ValueMask featureOnLabelMask = new ValueMask(featureMask, labelMask);	

				featureMasks.add(featureMask);
				featureOnLabelMasks.add(featureOnLabelMask);
			}
			double probabilityFeatureLabel;
			try {
				probabilityFeatureLabel = getParzenKernelEstimate(feature, featureOnLabelMasks);
			} catch (LabelException e) {
				_log.error(e.getMessage());
				probabilityFeatureLabel = 1.0 / Constants.NUMBER_OF_STATES;
			}
			if (label == objectLabel) {
				probablityCurrentLabel = probabilityLabel;
        
				probabilityFeatureCurrentLabel = probabilityFeatureLabel;
			}

			probabilityFeature += probabilityFeatureLabel * probabilityLabel;

		}

		// p (l|f) = (p(f|l)*p(l)/p(f)
		double probabilityLabelFeature = (probabilityFeatureCurrentLabel * probablityCurrentLabel) / probabilityFeature;
		return probabilityLabelFeature;
  
	}

  
	private static double getFeatureKernelProbabilityTraining(ImageMask mask, FactorGraphModel factorGraph, int objectLabel, Feature feature, ParametersContainer probabiltyContainer) {

		double probablityCurrentLabel = 0;
		double probabilityFeatureCurrentLabel = 0;
		double probabilityFeature = 0;
		
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			double probabilityLabel = probabiltyContainer.getLabelProbability(label);
			List<ValueMask> featureMasks = new ArrayList<ValueMask>();
			List<ValueMask> featureOnLabelMasks = new ArrayList<ValueMask>();
     
			BinaryMask labelMask = new BinaryMask(factorGraph.getImageMask(), label);
			ValueMask featureMask = factorGraph.getContinuousFeatureValueMask(feature);
			ValueMask featureOnLabelMask = new ValueMask(featureMask, labelMask);	

			featureMasks.add(featureMask);
			featureOnLabelMasks.add(featureOnLabelMask);

			double probabilityFeatureLabel;
			try {
				probabilityFeatureLabel = getParzenKernelEstimate(feature, featureOnLabelMasks);
			} catch (LabelException e) {
				probabilityFeatureLabel = 1.0 / Constants.NUMBER_OF_STATES;
			}
      
			if (label == objectLabel) {
				probablityCurrentLabel = probabilityLabel;
				probabilityFeatureCurrentLabel = probabilityFeatureLabel;
			}

			probabilityFeature += probabilityFeatureLabel * probabilityLabel;
		}

		// p (l|f) = (p(f|l)*p(l)/p(f)
		double probabilityLabelFeature = (probabilityFeatureCurrentLabel * probablityCurrentLabel) / probabilityFeature;

		return probabilityLabelFeature;
	}

  
	private static double getParzenKernelEstimate(Feature feature, List<ValueMask> featureMasks) throws LabelException {
    
		Double featureValue = (Double)feature.getValue();
		int numberOfTrainingData = 0;
		double output = 0;
		boolean allNull = true;
		for (ValueMask featureMask : featureMasks) {
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
			_log.error("KERNEL VALUE IS 0 for " + input);
    
		}
		return prob;
	}

  
	private static Logger _log = Logger.getLogger(CRFUtils.class);
}
