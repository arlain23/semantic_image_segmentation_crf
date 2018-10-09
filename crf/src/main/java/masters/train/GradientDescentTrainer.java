package masters.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.factorisation.FactorGraphModel;
import masters.image.ImageDTO;
import masters.image.ImageMask;
import masters.sampler.GibbsSampler;
import masters.utils.CRFUtils;
import masters.utils.DataHelper;
import masters.utils.Helper;
import masters.utils.ParametersContainer;

public class GradientDescentTrainer {
	private static int NUMBER_OF_ITERATIONS = Constants.NUMBER_OF_ITERATIONS;
	private static double REGULARIZATION_FACTOR = Constants.REGULARIZATION_FACTOR;
	private static double TRAINING_STEP = Constants.TRAINING_STEP;
	
	private static int NUMBER_OF_LABELS = Constants.NUMBER_OF_STATES; // {0 1}
	private int NUMBER_OF_LOCAL_FEATURES;
	private int NUMBER_OF_PAIRWISE_FEATURES;
	
	private List<ImageDTO> imageList;
	private Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap;
	
	private ParametersContainer parameterContainer;
	
	public GradientDescentTrainer(List<ImageDTO> imageList, Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap, ParametersContainer parameterContainer) {
		
		this.NUMBER_OF_LOCAL_FEATURES = parameterContainer.getNumberOfLocalFeatures();
		this.NUMBER_OF_PAIRWISE_FEATURES = parameterContainer.getNumberOfParwiseFeatures();
		this.imageList = imageList;
		this.imageToFactorGraphMap = imageToFactorGraphMap;
		this.parameterContainer = parameterContainer;
	}
	
	public WeightVector train(WeightVector weightVector) {
		
		//random initial weights
		if (weightVector == null) {
			weightVector = new WeightVector(NUMBER_OF_LABELS, NUMBER_OF_LOCAL_FEATURES, NUMBER_OF_PAIRWISE_FEATURES);
		}
		
		int numberOfWeights = weightVector.getWeightSize();
		Map<ImageDTO, ImageMask> imageToMaskMap = new HashMap<ImageDTO, ImageMask>();
		System.out.println(weightVector);
		for (int epoch = 0; epoch < NUMBER_OF_ITERATIONS; epoch++) {
			// for each training image
			int size = imageList.size();
			int iterator = 0;
			for (ImageDTO trainingImage : imageList) {
				// get samples
				FactorGraphModel factorGraph = imageToFactorGraphMap.get(trainingImage);
				ImageMask currentMask = null;
				if (imageToMaskMap.containsKey(trainingImage)) {
					currentMask = imageToMaskMap.get(trainingImage);
				}
				currentMask = GibbsSampler.getSample(factorGraph, weightVector, currentMask, parameterContainer);
				
				imageToMaskMap.put(trainingImage, currentMask);
				// calculate gradient
				List<Double> gradients = Helper.initFixedSizedListDouble(numberOfWeights);
				
				//fi for image
				FeatureVector imageFi = CRFUtils.calculateImageFi(weightVector, factorGraph, factorGraph.getImageMask(), parameterContainer);
				//fi for sample
				FeatureVector sampleFi = CRFUtils.calculateImageFi(weightVector, factorGraph, currentMask, parameterContainer);
				
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
				/*if (epoch % 100 == 0) {
					double gradientLength = getVectorLength(gradients);
					System.out.println("Gradient length: " + gradientLength);
				}*/
				weightVector = newWeightVetor;
				DataHelper.saveWeights(weightVector, parameterContainer.getCurrentDate(), true);
				
			}
			if (epoch % 100 == 0) {
				System.out.println(weightVector);
				System.out.println();
				//Helper.playSound("cow-moo1.wav");
			}
			if (epoch % 101 == 0) {
				System.out.println(weightVector);
			}
			System.out.println("I " + epoch);
			//save weights 
			DataHelper.saveWeights(weightVector, parameterContainer.getCurrentDate(), false);
		}  
		return weightVector;
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