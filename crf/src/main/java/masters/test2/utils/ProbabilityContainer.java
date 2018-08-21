package masters.test2.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import masters.test2.Constants;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.features.BinaryMask;
import masters.test2.image.ImageDTO;

public class ProbabilityContainer {
	List<Double> labelProbabilities;
	
	public ProbabilityContainer(Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap) {
		setLabelProbabilities(imageToFactorGraphMap);
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
