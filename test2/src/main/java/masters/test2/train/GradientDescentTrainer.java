package masters.test2.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import masters.test2.Constants;
import masters.test2.factorisation.Factor;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.image.ImageDTO;
import masters.test2.image.ImageMask;
import masters.test2.sampler.GibbsSampler;
import masters.test2.superpixel.SuperPixelDTO;
import masters.test2.utils.CRFUtils;
import masters.test2.utils.DataHelper;
import masters.test2.utils.Helper;
import masters.test2.utils.ProbabilityContainer;

public class GradientDescentTrainer {
	private static int NUMBER_OF_ITERATIONS = Constants.NUMBER_OF_ITERATIONS;
	private static double REGULARIZATION_FACTOR = Constants.REGULARIZATION_FACTOR;
	private static double TRAINING_STEP = Constants.TRAINING_STEP;
	
	private static int NUMBER_OF_LABELS = Constants.NUMBER_OF_STATES; // {0 1}
	private int NUMBER_OF_LOCAL_FEATURES;
	private int NUMBER_OF_PAIRWISE_FEATURES;
	
	private List<ImageDTO> imageList;
	private Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap;
	
	private ProbabilityContainer probabiltyContainer;
	
	public GradientDescentTrainer(List<ImageDTO> imageList, Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap, ProbabilityContainer probabiltyContainer,
			int numberOfLocalFeatures, int numberOfPairwiseFeatures) {
		
		this.NUMBER_OF_LOCAL_FEATURES = numberOfLocalFeatures;
		this.NUMBER_OF_PAIRWISE_FEATURES = numberOfPairwiseFeatures;
		this.imageList = imageList;
		this.imageToFactorGraphMap = imageToFactorGraphMap;
		this.probabiltyContainer = probabiltyContainer;
	}
	
	public WeightVector train(WeightVector weightVector) {
		
		//random initial weights
		if (weightVector == null) {
			weightVector = new WeightVector(NUMBER_OF_LABELS, NUMBER_OF_LOCAL_FEATURES, NUMBER_OF_PAIRWISE_FEATURES);
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
				currentMask = GibbsSampler.getSample(factorGraph, weightVector, currentMask, probabiltyContainer);
				imageToMaskMap.put(trainingImage, currentMask);
				// calculate gradient
				List<Double> gradients = Helper.initFixedSizedListDouble(numberOfWeights);
				
				//fi for image
				FeatureVector imageFi = CRFUtils.calculateImageFi(weightVector, factorGraph, factorGraph.getImageMask(), probabiltyContainer);
				
				//fi for sample
				FeatureVector sampleFi = CRFUtils.calculateImageFi(weightVector, factorGraph, currentMask, probabiltyContainer);
				
				for (int weightIndex = 0; weightIndex < numberOfWeights; weightIndex++) {
					double regularizationTerm = 2 * REGULARIZATION_FACTOR * weightVector.getWeights().get(weightIndex);
					double imageFiTerm = (Double)imageFi.getFeatures().get(weightIndex).getValue();
					double sampleFiTerm = (Double)sampleFi.getFeatures().get(weightIndex).getValue();
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
				WeightVector newWeightVetor = new WeightVector(newWeights);
				if (epoch % 100 == 0) {
					double mse = MSE(weightVector, newWeightVetor);
					double gradientLength = getVectorLength(gradients);
					System.out.println("Gradient length: " + gradientLength);
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
	
	private double MSE (WeightVector vector1, WeightVector vector2) {
		List<Double> weights1 = vector1.getWeights();
		List<Double> weights2 = vector2.getWeights();
		double sum = 0.0;
		
		for (int i = 0; i < weights1.size(); i++) {
			sum += Math.pow((weights1.get(i) - weights2.get(i)), 2);
		}

		return sum / (double) weights1.size();
	}
	private double getVectorLength(List<Double> vector) {
		double length = 0;
		for (Double d : vector) {
			length += d*d;
		}
		length = Math.sqrt(length);
		return length;
	}
	
	
	final static Logger _log = Logger.getLogger(GradientDescentTrainer.class);
}