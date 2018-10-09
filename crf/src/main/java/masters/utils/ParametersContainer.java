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
import masters.image.ImageDTO;

public class ParametersContainer implements Serializable{
	private static final long serialVersionUID = -1023100465899785888L;
  
	private List<Double> labelProbabilities;
	private int numberOfLocalFeatures;
	private int numberOfParwiseFeatures;
	private String currentDate;
	
	
	
	public String getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate() {
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyy_HHmm");
		this.currentDate = format.format(now);
	}

	public int getNumberOfLocalFeatures() {
		return numberOfLocalFeatures;
	}

	public void setNumberOfLocalFeatures(int numberOfLocalFeatures) {
		this.numberOfLocalFeatures = numberOfLocalFeatures;
	}

	public int getNumberOfParwiseFeatures() {
		return numberOfParwiseFeatures;
	}

	public void setNumberOfParwiseFeatures(int numberOfParwiseFeatures) {
		this.numberOfParwiseFeatures = numberOfParwiseFeatures;
	}

	public void setLabelProbabilities(Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap) {
		labelProbabilities = new ArrayList<Double>();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label ++) {
			int numberOfOnBytes = 0;
			int totalLabelSize = 0;
			
			for (ImageDTO image : imageToFactorGraphMap.keySet()) {
				FactorGraphModel factorGraph = imageToFactorGraphMap.get(image);
				BinaryMask labelMask = new BinaryMask(factorGraph.getImageMask(), label);
				numberOfOnBytes += labelMask.getNumberOfOnBytes();
				totalLabelSize += labelMask.getListSize();
			}
			
			double probabilityLabel = Double.valueOf(numberOfOnBytes) / Double.valueOf(totalLabelSize);
			labelProbabilities.add(probabilityLabel);
		}
	}
	
	public double getLabelProbability(int label) {
		return labelProbabilities.get(label);
	}
}
