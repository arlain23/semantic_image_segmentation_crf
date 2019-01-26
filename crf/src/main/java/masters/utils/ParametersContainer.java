package masters.utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import masters.Constants;
import masters.factorisation.FactorGraphModel;
import masters.features.BinaryMask;
import masters.features.Feature;
import masters.image.ImageDTO;
import masters.probabilistic_model.ProbabilityEstimator;
import masters.superpixel.SuperPixelDTO;

public class ParametersContainer implements Serializable{
	private static final long serialVersionUID = -1023100465899785888L;
  
	private List<Double> labelProbabilities;
	private int numberOfLocalFeatures;
	private int numberOfPairwiseFeatures;
	private String currentDate;
	
	private Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution;
	private List<Integer> selectedFeatureIds;
	
	private static ParametersContainer instance = null;
	
	private ParametersContainer() {
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyy_HHmm");
		this.currentDate = format.format(now);
	}
	
	public static ParametersContainer getInstance() {
		if (instance == null) {
			instance = new ParametersContainer();
		}
		return instance;
	}
	
	public List<Integer> getSelectedFeatureIds() {
		return selectedFeatureIds;
	}

	public void setSelectedFeatureIds(List<Integer> selectedFeatureIds) {
		this.selectedFeatureIds = selectedFeatureIds;
	}

	public Map<Feature, Map<Integer, ProbabilityEstimator>> getProbabilityEstimationDistribution() {
		return probabilityEstimationDistribution;
	}

	public void setProbabilityEstimationDistribution(
			Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution) {
		this.probabilityEstimationDistribution = probabilityEstimationDistribution;
	}

	public void setLabelProbabilities(List<Double> labelProbabilities) {
		this.labelProbabilities = labelProbabilities;
	}

	public void setNumberOfLocalFeatures(int numberOfLocalFeatures) {
		this.numberOfLocalFeatures = numberOfLocalFeatures;
	}

	public void setNumberOfParwiseFeatures(int numberOfPairwiseFeatures) {
		this.numberOfPairwiseFeatures = numberOfPairwiseFeatures;
	}

	public String getCurrentDate() {
		return currentDate;
	}

	public int getNumberOfLocalFeatures() {
		return numberOfLocalFeatures;
	}

	public int getNumberOfPairwiseFeatures() {
		return numberOfPairwiseFeatures;
	}

	public void setParameters(List<ImageDTO> trainingImageList) {
		labelProbabilities = new ArrayList<Double>();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label ++) {
			int numberOfOnBytes = 0;
			int totalLabelSize = 0;
			
			for (ImageDTO image : trainingImageList) {
				BinaryMask labelMask = new BinaryMask(image.getImageMask(), label);
				numberOfOnBytes += labelMask.getNumberOfOnBytes();
				totalLabelSize += labelMask.getListSize();
			}
			
			double probabilityLabel = Double.valueOf(numberOfOnBytes) / Double.valueOf(totalLabelSize);
			labelProbabilities.add(probabilityLabel);
		}
		SuperPixelDTO superPixelDTO = trainingImageList.get(0).getSuperPixels().get(0);
		this.numberOfLocalFeatures = superPixelDTO.getLocalFeatureVector().getFeatures().size();
		this.numberOfPairwiseFeatures = superPixelDTO.getPairwiseFeatureVector().getFeatures().size();
		
		  
	}
	
	public double getLabelProbability(int label) {
		return labelProbabilities.get(label);
	}
}
