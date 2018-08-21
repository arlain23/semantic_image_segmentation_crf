package masters.test2.sampler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import masters.test2.Constants;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.image.ImageMask;
import masters.test2.superpixel.SuperPixelDTO;
import masters.test2.train.FeatureVector;
import masters.test2.train.WeightVector;
import masters.test2.utils.CRFUtils;
import masters.test2.utils.DataHelper;
import masters.test2.utils.ProbabilityContainer;

public class GibbsSampler {

	public static ImageMask getSample(FactorGraphModel factorGraph, WeightVector weightVector,
			ImageMask previousMask, ProbabilityContainer probabilityContainer) {
		List<SuperPixelDTO> superPixels = factorGraph.getSuperPixels();
		int numberOfLabels = Constants.NUMBER_OF_STATES;
		int maskSize = superPixels.size();
		boolean save = false;

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

		String path = "E:\\Studia\\CSIT\\praca_magisterska\\segmentacja\\";
		String extension = ".png";

		if (save)
			DataHelper.saveImageSegmentedWithMask(factorGraph.getImage(), factorGraph.getSuperPixels(), initMask,
					(path + "1" + extension));
		for (int maskIndex = 0; maskIndex < maskSize; maskIndex++) {
			List<Double> labelProbabilities = new ArrayList<Double>();

			for (int label = 0; label < numberOfLabels; label++) {
				ImageMask tmpMask = new ImageMask(joinLists(resultingMask, label, initMask));
				// get probability for label
				labelProbabilities.add(getSampleEnergy(factorGraph, tmpMask, weightVector, probabilityContainer));
			}
			labelProbabilities = calculateLabelProbabilities(labelProbabilities);
			
			// choose label for mask position
			int chosenLabel = getLabelFromProbability(labelProbabilities);
			// set label
			resultingMask.add(chosenLabel);
			List<Integer> tmp = joinLists(resultingMask, initMask);
			if (save)
				DataHelper.saveImageSegmentedWithMask(factorGraph.getImage(), factorGraph.getSuperPixels(), tmp,
						(path + (maskIndex + 2) + extension));
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
						_log.error("Label probability is NaN: exp(" + probabilityDifference + ")");
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

	public static double getSampleEnergy(FactorGraphModel factorGraph, ImageMask mask, WeightVector weightVector, ProbabilityContainer probabilityContainer) {
		FeatureVector featureVector = CRFUtils.calculateImageFi(weightVector, factorGraph, mask, probabilityContainer);
		return featureVector.calculateEnergy(weightVector);
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
		System.out.println("sth wrong (get random label)");
		return 0;
	}
	private static Logger _log = Logger.getLogger(GibbsSampler.class);
}
