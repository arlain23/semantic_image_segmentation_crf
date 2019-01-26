package masters.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import masters.Constants;
import masters.colors.ColorSpaceException;
import masters.factorisation.FactorGraphModel;
import masters.image.ImageDTO;
import masters.train.WeightVector;

public class InputHelper {
	public static Map<ImageDTO, FactorGraphModel> prepareTestData(ParametersContainer parameterContainer,WeightVector weights,
			List<ImageDTO> testImageList, List<ImageDTO> trainImageList) {
		
		Map<ImageDTO, FactorGraphModel> testimageToFactorGraphMap = new HashMap<>();

		for (ImageDTO currentImage : testImageList) {
			FactorGraphModel factorGraph = new FactorGraphModel(currentImage, trainImageList, weights, parameterContainer);
			testimageToFactorGraphMap.put(currentImage, factorGraph);
		}
		return testimageToFactorGraphMap;
	}
	
	public static Map<ImageDTO, FactorGraphModel> prepareValidationData(ParametersContainer parameterContainer, WeightVector weights,
			List<ImageDTO> validationImageList, List<ImageDTO> trainImageList)
			throws IOException, ColorSpaceException {
		
		Map<ImageDTO, FactorGraphModel> validationImageToFactorGraphMap = new HashMap<>();
		for (ImageDTO currentImage : validationImageList) {
			FactorGraphModel factorGraph = new FactorGraphModel(currentImage, trainImageList, weights, parameterContainer);
			validationImageToFactorGraphMap.put(currentImage, factorGraph);
		}
		return validationImageToFactorGraphMap;
	}

	public static Map<ImageDTO, FactorGraphModel> prepareTrainingData(
			ParametersContainer parameterContainer, List<ImageDTO> imageList) throws IOException, ColorSpaceException {
		
		Map<ImageDTO, FactorGraphModel> trainingImageToFactorGraphMap = new HashMap<>();
		
		int numberOfLocalFeatures = 0;
		int numberOfParwiseFeatures = 0;
		for (ImageDTO currentImage : imageList) {
			  WeightVector randomWeightVector = new WeightVector(Constants.NUMBER_OF_STATES, numberOfLocalFeatures, numberOfParwiseFeatures);
			  FactorGraphModel factorGraph = new FactorGraphModel(currentImage, randomWeightVector, parameterContainer);

			  trainingImageToFactorGraphMap.put(currentImage, factorGraph);
		  }
		return trainingImageToFactorGraphMap;
	}
}
