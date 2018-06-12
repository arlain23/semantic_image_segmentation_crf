package masters.test2.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import masters.test2.DataHelper;
import masters.test2.Helper;
import masters.test2.factorisation.Factor;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.image.ImageDTO;
import masters.test2.sampler.GibbsSampler;
import masters.test2.sampler.ImageMask;
import masters.test2.superpixel.SuperPixelDTO;

public class GradientDescentTrainer {
	private static int NUMBER_OF_ITERATIONS = 2000;
	private static double REGULARIZATION_FACTOR = 500;
	private static double TRAINING_STEP = 0.0001;
	
	private static int NUMBER_OF_LABELS = FactorGraphModel.NUMBER_OF_STATES; // {0 1}
	private static int NUMBER_OF_FEATURES = SuperPixelDTO.NUMBER_OF_FEATURES;
	
	private List<ImageDTO> imageList;
	private Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap;
	
	public GradientDescentTrainer(List<ImageDTO> imageList, Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap) {
		this.imageList = imageList;
		this.imageToFactorGraphMap = imageToFactorGraphMap;
	}
	
	public WeightVector train(WeightVector weightVector) {
		
		//random initial weights
		if (weightVector == null) {
			weightVector = new WeightVector(NUMBER_OF_LABELS, NUMBER_OF_FEATURES);
		}
		
		int numberOfWeights = weightVector.getWeightSize();
		int counter = 0;
		Map<ImageDTO, ImageMask> imageToMaskMap = new HashMap<ImageDTO, ImageMask>();
		System.out.println(weightVector);
		for (int epoch = 0; epoch < NUMBER_OF_ITERATIONS; epoch++) {
			// for each training image
			for (ImageDTO trainingImage : imageList) {
				counter++;
				// get samples
				FactorGraphModel factorGraph = imageToFactorGraphMap.get(trainingImage);

				ImageMask currentMask = null;
				if (imageToMaskMap.containsKey(trainingImage)) {
					currentMask = imageToMaskMap.get(trainingImage);
				}
				currentMask = GibbsSampler.getSample(factorGraph, weightVector, currentMask);
				imageToMaskMap.put(trainingImage, currentMask);
				// calculate gradient
				List<Double> gradients = Helper.initFixedSizedListDouble(numberOfWeights);
				
				//fi for image
				FeatureVector imageFi = calculateImageFi(weightVector, factorGraph, null);
				
				//fi for sample
				FeatureVector sampleFi = calculateImageFi(weightVector, factorGraph, currentMask);
				
				for (int weightIndex = 0; weightIndex < numberOfWeights; weightIndex++) {
					double regularizationTerm = 2 * REGULARIZATION_FACTOR * weightVector.getWeights().get(weightIndex);
					double imageFiTerm = imageFi.getFeatureValues().get(weightIndex);
					double sampleFiTerm = sampleFi.getFeatureValues().get(weightIndex);
					double gradientValue = regularizationTerm + imageFiTerm - sampleFiTerm;
					gradients.set(weightIndex, gradientValue);
				}
				
				// update weights
				List<Double> previousWeights = weightVector.getWeights();
				
				List<Double> newWeights = new ArrayList<Double>();
				for (int i = 0; i < previousWeights.size(); i++) {
					double newWeight = previousWeights.get(i) - TRAINING_STEP * gradients.get(i);
					newWeights.add(newWeight);
				}
				WeightVector newWeightVetor = new WeightVector(newWeights, NUMBER_OF_LABELS, NUMBER_OF_FEATURES);
				if (epoch % 100 == 0) {
					double mse = MSE(weightVector, newWeightVetor);
					System.out.println("mse " + mse);
				}
				weightVector = newWeightVetor;
			}
			if (epoch % 100 == 0) {
				System.out.println(weightVector);
				System.out.println();
				//Helper.playSound("cow-moo1.wav");
			}
			if (epoch % 101 == 0) {
				System.out.println(weightVector);
			}
		}
		return weightVector;
	}
	
	public static FeatureVector calculateImageFi(WeightVector weightVector, FactorGraphModel factorGraph, ImageMask mask) {
		FeatureVector imageFi = new FeatureVector(weightVector.getWeightSize());

		List<SuperPixelDTO> superPixels = factorGraph.getSuperPixels();
		Set<Factor> createdFactors = factorGraph.getCreatedFactors();
		for (Factor factor : createdFactors) {
			int leftSuperPixelIndex = factor.getLeftSuperPixelIndex();
			SuperPixelDTO leftSuperPixel = superPixels.get(leftSuperPixelIndex);
			int rightSuperPixelIndex = factor.getRightSuperPixelIndex();
			if (rightSuperPixelIndex < 0) {
				// feature node - local model (R6)
				FeatureVector localModel = leftSuperPixel.getLocalImageFi(mask);
				imageFi.add(localModel);
			} else {
				// output node - pairwise model (R2)
				SuperPixelDTO rightSuperPixel = superPixels.get(rightSuperPixelIndex);
				FeatureVector pairWiseModel = leftSuperPixel.getPairwiseImageFi(rightSuperPixel, mask);
				imageFi.add(pairWiseModel);
			}
			
		}
		return imageFi;
	}
	private double MSE (WeightVector vector1, WeightVector vector2) {
		List<Double> weights1 = vector1.getWeights();
		List<Double> weights2 = vector2.getWeights();
		double sum = 0.0;
		
		for (int i = 0; i < weights1.size(); i++) {
			sum += Math.pow((weights1.get(i) - weights2.get(i)), 2);
		}

		return sum / (double) weights1.size();
	}
	
	
	final static Logger _log = Logger.getLogger(GradientDescentTrainer.class);
}