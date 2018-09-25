package masters.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import masters.Constants;
import masters.factorisation.FactorGraphModel;
import masters.features.BinaryMask;
import masters.grid.GridHelper;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;

public class ParametersContainer implements Serializable{
	private static final long serialVersionUID = -1023100465899785888L;
  
	private List<Double> labelProbabilities;
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
