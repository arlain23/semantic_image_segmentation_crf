package masters.sampler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.factorisation.FactorGraphModel;
import masters.image.ImageMask;
import masters.superpixel.SuperPixelDTO;
import masters.train.FeatureVector;
import masters.train.WeightVector;
import masters.utils.CRFUtils;
import masters.utils.ParametersContainer;

public class GibbsSampler {

	public static ImageMask getSample(FactorGraphModel factorGraph, WeightVector weightVector,
			ImageMask previousMask, ParametersContainer parameterContainer) {
		List<SuperPixelDTO> superPixels = factorGraph.getImage().getSuperPixels();
		int numberOfLabels = Constants.NUMBER_OF_STATES;
		int maskSize = superPixels.size();

		ImageMask singleSampling;
		List<Integer> resultingMask = new ArrayList<Integer>();
		List<Integer> initMask = new ArrayList<Integer>();
		if (previousMask == null) {
			// randomly generate first mask
			singleSampling = new ImageMask(maskSize);
			for (int i = 0; i < maskSize; i++) {
				int randomLabel = getRandomLabel(numberOfLabels);
				initMask.add(randomLabel);
			}
		} else {
			singleSampling = previousMask;
			initMask = previousMask.getMask();
		}

		for (int maskIndex = 0; maskIndex < maskSize; maskIndex++) {
			List<Double> labelProbabilities = new ArrayList<Double>();

			for (int label = 0; label < numberOfLabels; label++) {
				ImageMask tmpMask = new ImageMask(joinLists(resultingMask, label, initMask));
				// get probability for label
				labelProbabilities.add(getSampleEnergy(factorGraph, tmpMask, weightVector, parameterContainer));
			}
			labelProbabilities = calculateLabelProbabilities(labelProbabilities);
			
			// choose label for mask position
			int chosenLabel = getLabelFromProbability(labelProbabilities);
			// set label
			resultingMask.add(chosenLabel);
		}
		singleSampling.setMask(resultingMask);

		return singleSampling;

	}

	private static List<Double> calculateLabelProbabilities(List<Double> labelProbabilities) {
		List<Double> finalLabelProbabilities = new ArrayList<Double>();
		for (int i = 0; i < labelProbabilities.size(); i++) {
			double sum = 0;
			for (int j = 0; j < labelProbabilities.size(); j++) {
				if (i != j) {
					double probabilityDifference = labelProbabilities.get(i) - labelProbabilities.get(j);
					sum += Math.exp(probabilityDifference);
					if (Double.isNaN(sum)) {
						System.out.println(labelProbabilities.get(i)  + " - " + labelProbabilities.get(j));
						_log.error("Label probability is NaN: exp(" + probabilityDifference + ")");
						throw new RuntimeException();
					}
				}
			}
			double labelProbability = (1 / (1 + sum));

			finalLabelProbabilities.add(labelProbability);
		}
		
		return finalLabelProbabilities;
	}

	private static int getLabelFromProbability(List<Double> labelProbabilities) {
		double randNumber = Math.random();
		double limitValue = 0;
		for (int label = 0; label < labelProbabilities.size(); label++) {
			limitValue += labelProbabilities.get(label);
			if (randNumber < limitValue)
				return label;
		}
		_log.error("getLabelFromProbability return 0");
		return 0;
	}

	public static double getSampleEnergy(FactorGraphModel factorGraph, ImageMask mask, WeightVector weightVector, ParametersContainer parameterContainer) {
		FeatureVector featureVector = CRFUtils.calculateImageFi(weightVector, factorGraph, mask, parameterContainer);
		double energy = featureVector.calculateEnergy(weightVector);
		return energy;
	}

	private static List<Integer> joinLists(List<Integer> resultingList, int value, List<Integer> previousList) {
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(resultingList);
		list.add(value);
		for (int i = list.size(); i < previousList.size(); i++) {
			list.add(previousList.get(i));
		}
		return list;

	}

	private static List<Integer> joinLists(List<Integer> resultingList, List<Integer> previousList) {
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(resultingList);
		for (int i = resultingList.size(); i < previousList.size(); i++) {
			list.add(previousList.get(i));
		}
		return list;

	}

	private static int getRandomLabel(int numberOfLabels) {
		double blockSize = 1.0 / numberOfLabels;
		double randNumber = Math.random();
		for (int i = 0; i < numberOfLabels; i++) {
			if (randNumber < (i + 1) * blockSize)
				return i;
		}
		_log.error("sth wrong (get random label)");
		return 0;
	}
	private static Logger _log = Logger.getLogger(GibbsSampler.class);
}
