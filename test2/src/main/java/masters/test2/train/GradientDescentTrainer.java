package masters.test2.train;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import masters.test2.crf.FactorGraphModelSP;
import masters.test2.image.ImageDTO;
import masters.test2.sampler.GibbsSampler;
import masters.test2.sampler.ImageMask;
import masters.test2.sampler.WeightVector;
import masters.test2.superpixel.SuperPixelDTO;

public class GradientDescentTrainer {
	private static int NUMBER_OF_ITERATIONS = 100;
	private static double REGULARIZATION_FACTOR = 0.0;
	private static double TRAINING_STEP = 0.1;
	
	private static int NUMBER_OF_LABELS = FactorGraphModelSP.NUMBER_OF_STATES; // {0 1}
	private static int NUMBER_OF_FEATURES = 3; // {r g B}
	
	private List<ImageDTO> imageList;
	private Map<ImageDTO, FactorGraphModelSP> imageToFactorGraphMap;
	
	public GradientDescentTrainer(List<ImageDTO> imageList, Map<ImageDTO, FactorGraphModelSP> imageToFactorGraphMap) {
		this.imageList = imageList;
		this.imageToFactorGraphMap = imageToFactorGraphMap;
	}
	
	public WeightVector train() {
		//random initial weights
		WeightVector weightVector = new WeightVector(NUMBER_OF_LABELS, NUMBER_OF_FEATURES);
		
		int numberOfWeights = weightVector.getWeightSize();
		System.out.println("number of weights " + numberOfWeights);
		int numberOfSamples = 100;
		
		System.out.println(weightVector);
		for (int epoch = 0; epoch < NUMBER_OF_ITERATIONS; epoch++) {
			// for each training image
			for (ImageDTO trainingImage : imageList) {
				// get samples
				FactorGraphModelSP factorGraph = imageToFactorGraphMap.get(trainingImage);
				List<ImageMask> samples = GibbsSampler.getSamples(factorGraph, weightVector, numberOfSamples);
				
				// calculate gradient
				List<Double> gradients = createList(numberOfWeights);
				
				double energy = calculateEnergy(weightVector, factorGraph); 
				
				double samplesEnergy = 0;
				for (ImageMask mask : samples) {
					double sampleEnergy = calculateSampleEnergy(weightVector, factorGraph, mask);
					samplesEnergy += sampleEnergy;
				}
				
				for (int weightIndex = 0; weightIndex < numberOfWeights; weightIndex++) {
					double regularizationTerm = 2 * REGULARIZATION_FACTOR * weightVector.getWeights().get(weightIndex);
					double gradientValue = regularizationTerm + energy - (samplesEnergy / samples.size());
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
				double mse = MSE(weightVector, newWeightVetor);
				if (mse < 10) {
					System.out.println(mse);
				}
				weightVector = newWeightVetor;
			}
			System.out.println(weightVector);
		}
		return weightVector;
	}
	
	
	private List<Double> createList(int numberOfWeights) {
		List<Double> result =  new ArrayList<Double>();
		for (int i = 0; i < numberOfWeights; i++) {
			result.add(0.0);
		}
		return result;
	}

	private double calculateSampleEnergy(WeightVector weightVector, FactorGraphModelSP factorGraph, ImageMask mask) {
		return GibbsSampler.getSampleEnergy(mask.getMask(), factorGraph.getSuperPixels(), weightVector);
	}
	private double calculateEnergy(WeightVector weightVector, FactorGraphModelSP factorGraph) {
		List<SuperPixelDTO> superPixels = factorGraph.getSuperPixels();
		// local model
		double localEnergy = 0;
		for (SuperPixelDTO superPixel : superPixels) {
			int label = superPixel.getLabel();
			List<Double> featureWeights = weightVector.getFeatureWeightsForLabel(label);
			localEnergy += superPixel.getEnergyByWeights(featureWeights);
		}
		
		// pairwise model
		double pairwiseEnergy = 0;
		for (SuperPixelDTO superPixel : superPixels) {
			int mainLabel = superPixel.getLabel();
			List<SuperPixelDTO> neighbouringPixels = superPixel.getNeigbouringSuperPixels();
			for (SuperPixelDTO neighbour : neighbouringPixels) {
				int neighbourLabel = neighbour.getLabel();
				double pairEnergy = weightVector.getPairSimilarityWeight(mainLabel, neighbourLabel);
				pairwiseEnergy += pairEnergy;
			}
		}
		
		double totalEnergy = localEnergy + pairwiseEnergy;
		//totalEnergy = Math.exp(-totalEnergy);
		return totalEnergy;
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
}