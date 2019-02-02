package masters.train;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.Constants.State;
import masters.factorisation.FactorGraphModel;
import masters.image.ImageDTO;
import masters.image.ImageMask;
import masters.inference.InferenceHelper;
import masters.sampler.GibbsSampler;
import masters.utils.CRFUtils;
import masters.utils.DataHelper;
import masters.utils.Helper;
import masters.utils.InputHelper;
import masters.utils.ParametersContainer;

public class GradientDescentTrainer {
	private static int NUMBER_OF_ITERATIONS = Constants.NUMBER_OF_ITERATIONS;
	private static double REGULARIZATION_FACTOR = Constants.REGULARIZATION_FACTOR;
	private static double TRAINING_STEP = Constants.TRAINING_STEP;
	
	private static int NUMBER_OF_LABELS = Constants.NUMBER_OF_STATES; // {0 1}
	private int NUMBER_OF_LOCAL_FEATURES;
	private int NUMBER_OF_PAIRWISE_FEATURES;
	
	private ParametersContainer parameterContainer;
	
	public GradientDescentTrainer(ParametersContainer parameterContainer) {
		
		this.NUMBER_OF_LOCAL_FEATURES = parameterContainer.getNumberOfLocalFeatures();
		this.NUMBER_OF_PAIRWISE_FEATURES = parameterContainer.getNumberOfPairwiseFeatures();
		this.parameterContainer = parameterContainer;
		
	}
	
	public WeightVector train(WeightVector weightVector) {
		Map<String, File> trainingFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_PATH);
		Map<String, File> resultFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_RESULT_PATH);
		
		//random initial weights
		if (weightVector == null) {
			weightVector = new WeightVector(NUMBER_OF_LABELS, NUMBER_OF_LOCAL_FEATURES, NUMBER_OF_PAIRWISE_FEATURES);
		}
		
		int numberOfWeights = weightVector.getWeightSize();
		System.out.println(weightVector);
		for (int epoch = 0; epoch < NUMBER_OF_ITERATIONS; epoch++) {
			System.out.println("---------------------------");
			System.out.println("I " + epoch);
			
			// for each training image
			int size = trainingFiles.keySet().size();
			int iterator = 0;
			
			for (String fileName : trainingFiles.keySet()) {
				
				File trainFile = trainingFiles.get(fileName);
				File segmentedFile = resultFiles.get(fileName + Constants.RESULT_IMAGE_SUFFIX);
				
				ImageDTO trainingImage = DataHelper.getSingleImageSegmented(trainFile, segmentedFile, State.TRAIN, parameterContainer);
				FactorGraphModel factorGraph = new FactorGraphModel(trainingImage, weightVector, parameterContainer);
				iterator++;
				// get samples
				ImageMask currentMask = null;
				currentMask = GibbsSampler.getSample(factorGraph, weightVector, currentMask, parameterContainer);
				
				// calculate gradient
				List<Double> gradients = Helper.initFixedSizedListDouble(numberOfWeights);
				
				//fi for image
				FeatureVector imageFi = CRFUtils.calculateImageFi(weightVector, factorGraph, factorGraph.getImage().getImageMask(), parameterContainer);
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
				
				double gradientLength = getVectorLength(gradients);
				System.out.println("Gradient length: " + gradientLength);
				
				weightVector = newWeightVetor;
				DataHelper.saveWeights(weightVector, parameterContainer.getCurrentDate(), true);
				
			}
			//check if accuracy is better
			List<ImageDTO> testImageList = DataHelper.getTestData(parameterContainer);
			List<ImageDTO> trainImageList = new ArrayList<>();
			
			String baseImagePath = "C:\\Users\\anean\\Desktop\\CRF\\inference_data\\";
			InferenceHelper.runInference(testImageList, trainImageList, baseImagePath, "training_01_02_" + (epoch), parameterContainer, weightVector);
			
			
			System.out.println(weightVector);
			System.out.println();
			
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