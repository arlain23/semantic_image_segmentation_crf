package noise;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections4.map.HashedMap;

import generator.GeneratorConstants;
import masters.Constants;
import masters.Constants.State;
import masters.colors.ColorSpaceException;
import masters.image.ImageDTO;
import masters.image.PixelDTO;
import masters.superpixel.SuperPixelDTO;
import masters.superpixel.SuperPixelHelper;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;

public class Noiser {
	
	
	private static List<SuperPixelDTO> getSuperPixelsCached(ImageDTO imageDTO, String prefix) throws IOException, ColorSpaceException {
		return SuperPixelHelper.getSuperPixelsCached(imageDTO, prefix);
	}
	public static void addNoiseToData(String baseImagePath, int noiseNumber) {
		Map<String, File> trainingFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_PATH);
		Map<String, File> resultFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_RESULT_PATH);
		addNoiseToData(baseImagePath, State.TRAIN, trainingFiles, resultFiles, noiseNumber);
		
		Map<String, File> validationFiles = DataHelper.getFilesFromDirectory(Constants.VALIDATION_PATH);
		Map<String, File> validationResultFiles = DataHelper.getFilesFromDirectory(Constants.VALIDATION_RESULT_PATH);
		
		addNoiseToData(baseImagePath, State.VALIDATION, validationFiles, validationResultFiles, noiseNumber);
		
		Map<String, File> testFiles = DataHelper.getFilesFromDirectory(Constants.TEST_PATH);
		addNoiseToData(baseImagePath, State.TEST, testFiles, new HashMap<String, File>(), noiseNumber);
		
		
	}
	private static void addNoiseToData(String baseImagePath, State phase, Map<String, File> trainingFiles, Map<String, File> resultFiles, int noiseNumberLimit) {
		baseImagePath = "src/main/resources/" + baseImagePath + "/" + phase.toString().toLowerCase() + "/";
		ParametersContainer parameterContainer = ParametersContainer.getInstance();
		Random rand = new Random();
		int limit = 40;
		if (phase.equals("test")) {
			limit = 20;
		}
		for (String fileName : trainingFiles.keySet()) {
			
			File trainFile = trainingFiles.get(fileName);
			File segmentedFile = resultFiles.get(fileName + Constants.RESULT_IMAGE_SUFFIX);
			
			ImageDTO image = DataHelper.getSingleImageSegmented(trainFile, segmentedFile, phase, parameterContainer);
    		int r = rand.nextInt(100);
			String imageName = DataHelper.getFileNameFromImageDTO(image);
			try {
				List<SuperPixelDTO> superPixels = getSuperPixelsCached(image, phase.toString());
				if (r > limit) {
					if (noiseNumberLimit > 0) {
						int noiseNumber = rand.nextInt(noiseNumberLimit + 1);
						for (int i = 0; i < noiseNumber; i++) {
							int noiseIndex = rand.nextInt(superPixels.size());
							SuperPixelDTO noisedSuperPixel = superPixels.get(noiseIndex);
							List<PixelDTO> pixels = noisedSuperPixel.getPixels();
							double[] meanRGB = noisedSuperPixel.getMeanRGB();
							Color meanColor = new Color((int)meanRGB[0], (int)meanRGB[1], (int)meanRGB[2]);
							
							try {
								List<Color> availableColors = GeneratorConstants.COLOR_TO_AVAILABLE_NOISE_MAP.get(meanColor);
								Collections.shuffle(availableColors);
								Color noiseColor = availableColors.get(0);
								
								for (PixelDTO pixel : pixels) {
									pixel.setR(noiseColor.getRed());
									pixel.setG(noiseColor.getGreen());
									pixel.setB(noiseColor.getBlue());
								}
							} catch (NullPointerException e) {
								
							}
						}
					}
				}
				DataHelper.saveImageBySuperPixelsPixelData(image, superPixels, baseImagePath + imageName + ".png");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ColorSpaceException e) {
				e.printStackTrace();
			}
			
		}
	}
}
