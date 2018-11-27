package masters.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import masters.Constants;
import masters.colors.ColorSpaceException;
import masters.factorisation.FactorGraphModel;
import masters.grid.GridHelper;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;
import masters.superpixel.SuperPixelHelper;
import masters.train.WeightVector;

public class InputHelper {
	public static void prepareTestData(ParametersContainer parameterContainer,
			Constants.State state, WeightVector weights,
			List<ImageDTO> testImageList,
			Map<ImageDTO, FactorGraphModel> testimageToFactorGraphMap)
			throws IOException, ColorSpaceException {
		int totalNumerOfImages = testImageList.size();
		int counter = 0;
		for (ImageDTO currentImage : testImageList) {
			System.out.println("image " + (++counter) + "/" + totalNumerOfImages);
			List<SuperPixelDTO> createdSuperPixels ;
			if (Constants.CLEAR_CACHE) {
				createdSuperPixels = SuperPixelHelper.getNewSuperPixels(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS, state.toString(), false);
			} else {
				createdSuperPixels = SuperPixelHelper.getSuperPixelsCached(currentImage, state.toString());
			}
				
			// set feature vectors
			int meanDistance = GridHelper.getMeanSuperPixelDistance(createdSuperPixels);
			for (SuperPixelDTO superPixel : createdSuperPixels) {
				superPixel.initFeatureVector(meanDistance, createdSuperPixels, currentImage);
			}
			FactorGraphModel factorGraph = new FactorGraphModel(currentImage, createdSuperPixels, null, weights, parameterContainer);

			testimageToFactorGraphMap.put(currentImage, factorGraph);
		}
	}

	public static void prepareTrainingData(
			ParametersContainer parameterContainer, List<ImageDTO> imageList,
			Map<ImageDTO, FactorGraphModel> trainingImageToFactorGraphMap,
			Constants.State state) throws IOException, ColorSpaceException {
		
		
		int totalNumerOfImages = imageList.size();
		int counter = 0;
		int numberOfLocalFeatures = 0;
		int numberOfParwiseFeatures = 0;
		for (ImageDTO currentImage : imageList) {
			System.out.println("image " + (++counter) + "/" + totalNumerOfImages);
			List<SuperPixelDTO> createdSuperPixels = null;
			  
			if (Constants.CLEAR_CACHE) {
				createdSuperPixels = SuperPixelHelper.getNewSuperPixels(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS, state.toString(), true);
			} else {
				try {
					createdSuperPixels = SuperPixelHelper.getSuperPixelsCached(currentImage, state.toString());
				} catch (Exception e) {
					createdSuperPixels = SuperPixelHelper.getNewSuperPixels(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS, state.toString(), true);
				}
			}
			// set feature vectors
			int meanDistance = GridHelper.getMeanSuperPixelDistance(createdSuperPixels);
			for (SuperPixelDTO superPixel : createdSuperPixels) {
				  superPixel.initFeatureVector(meanDistance, createdSuperPixels, currentImage);
				  numberOfLocalFeatures = superPixel.getLocalFeatureVector().getFeatures().size();
				  numberOfParwiseFeatures = superPixel.getPairwiseFeatureVector().getFeatures().size();
			  }
			  
			  WeightVector randomWeightVector = new WeightVector(Constants.NUMBER_OF_STATES, numberOfLocalFeatures, numberOfParwiseFeatures);
			
			  parameterContainer.setNumberOfLocalFeatures(numberOfLocalFeatures);
			  parameterContainer.setNumberOfParwiseFeatures(numberOfParwiseFeatures);
			  
			  SuperPixelHelper.updateSuperPixelLabels(createdSuperPixels);
			  FactorGraphModel factorGraph = new FactorGraphModel(currentImage,createdSuperPixels, randomWeightVector, parameterContainer);

			  trainingImageToFactorGraphMap.put(currentImage, factorGraph);
		  }
		  
		  //Data holders
		  parameterContainer.setLabelProbabilities(trainingImageToFactorGraphMap);
		  parameterContainer.setNumberOfLocalFeatures(numberOfLocalFeatures);
		  parameterContainer.setNumberOfParwiseFeatures(numberOfParwiseFeatures);
		  parameterContainer.setCurrentDate();
	}
}
