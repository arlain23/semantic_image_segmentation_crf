package masters.test2.sampler;

import java.util.ArrayList;
import java.util.List;

import masters.test2.crf.FactorGraphModelSP;
import masters.test2.superpixel.SuperPixelDTO;

public class GibbsSampler {
	public static List<ImageMask> getSamples(FactorGraphModelSP factorGraph, WeightVector weightVector, int numberOfSamples) {
		List<SuperPixelDTO> superPixels = factorGraph.getSuperPixels();
		int numberOfLabels = FactorGraphModelSP.NUMBER_OF_STATES;
		
		List<ImageMask> resultSampling = new ArrayList<ImageMask>();
		int maskSize = superPixels.size();
		for (int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++) {
			// randomly generate first mask
			ImageMask singleSampling = new ImageMask(maskSize);
			List<Integer> resultingMask = new ArrayList<Integer>();
			
			List<Integer> initMask = new ArrayList<Integer>();
			for (int i = 0; i < maskSize; i++) {
				
				int randomLabel = getRandomLabel(numberOfLabels);
				initMask.add(randomLabel);
			}
			
			for (int maskIndex = 0; maskIndex < maskSize; maskIndex++) {
				List<Double> labelProbabilities = new ArrayList<Double>();
				
				for (int label = 0; label < numberOfLabels; label++) {
					List<Integer> tmpMask = joinLists(resultingMask, label, initMask);
					// get probability for label
					labelProbabilities.add(getLabelProbability(tmpMask, superPixels, weightVector));
				}
				normaliseLabelProbabilities(labelProbabilities);
				//choose label for mask position
				int chosenLabel = getLabelFromProbability(labelProbabilities);
				//set label
				resultingMask.add(chosenLabel);				
			}
			
			singleSampling.setMask(resultingMask);
			resultSampling.add(singleSampling);
		}
		
		return resultSampling;
		
	}
	private static void normaliseLabelProbabilities(List<Double> labelProbabilities) {
		double probabilitySum = labelProbabilities.stream().mapToDouble(Double::doubleValue).sum();
		for (int i = 0; i < labelProbabilities.size(); i++) {
			double normalisedValue = labelProbabilities.get(i) / probabilitySum;
			labelProbabilities.set(i, normalisedValue);
		}
	}
	private static int getLabelFromProbability(List<Double> labelProbabilities) {
		double randNumber = Math.random();
		double limitValue = 0;
		for (int label = 0; label < labelProbabilities.size(); label++) {
			System.out.println(labelProbabilities.get(label) + "->" + limitValue);
			limitValue += labelProbabilities.get(label);
			if (randNumber < limitValue) return label;
		}
		System.out.println("returning -1");
		return -1;
	}
	
	private static double getLabelProbability(List<Integer> mask, List<SuperPixelDTO> superPixels, WeightVector weightVector) {
		double d = -getSampleEnergy(mask, superPixels, weightVector);
		System.out.println(d);
		return Math.exp(d);
	}
	public static double getSampleEnergy(List<Integer> mask, List<SuperPixelDTO> superPixels, WeightVector weightVector) {
		// local model
		double localEnergy = 0;
		for (int superPixelIndex = 0; superPixelIndex < mask.size(); superPixelIndex++) {
			int label = mask.get(superPixelIndex);
			SuperPixelDTO currentSuperPixel = superPixels.get(superPixelIndex);
			List<Double> featureWeights = weightVector.getFeatureWeightsForLabel(label);
			localEnergy += currentSuperPixel.getEnergyByWeights(featureWeights);
			
		}
		// pairwise model
		double pairwiseEnergy = 0;
		for (int superPixelIndex = 0; superPixelIndex < mask.size(); superPixelIndex++) {
			int mainLabel = mask.get(superPixelIndex);
			SuperPixelDTO currentSuperPixel = superPixels.get(superPixelIndex);
			List<SuperPixelDTO> neighbouringPixels = currentSuperPixel.getNeigbouringSuperPixels();
			for (SuperPixelDTO neighbour : neighbouringPixels) {
				int neighbourLabel = mask.get(neighbour.getSuperPixelIndex());
				double pairEnergy = weightVector.getPairSimilarityWeight(mainLabel, neighbourLabel);
				pairwiseEnergy += pairEnergy;
			}
			
		}
		double totalEnergy = localEnergy + pairwiseEnergy;
		//totalEnergy = Math.exp(-totalEnergy);
		return totalEnergy;
	}
	private static List<Integer> joinLists(List<Integer> resultingList, int value, List<Integer> previousList) {
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(resultingList);
		list.add(value);
		for (int i = resultingList.size() + 1; i < previousList.size(); i++) {
			list.add(previousList.get(i));
		}
		return list;
		
	}

	private static int getRandomLabel(int numberOfLabels) {
		double blockSize = 1.0 / numberOfLabels;
		double randNumber = Math.random();
		for (int i = 0; i < numberOfLabels; i++) {
			if (randNumber < (i+1) * blockSize) return i;
		}
		return -1;
	}
	
	
	
}
