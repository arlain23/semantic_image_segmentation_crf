package colours;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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

public class Colouriser {
	
	private static List<SuperPixelDTO> getSuperPixelsCached(ImageDTO imageDTO, String prefix) {
		return SuperPixelHelper.getSuperPixelsCached(imageDTO, prefix);
	}
	public static void colouriseData(String baseImagePath) {
		Map<String, File> trainingFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_PATH);
		Map<String, File> resultFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_RESULT_PATH);
		colouriseData(baseImagePath, State.TRAIN, trainingFiles, resultFiles);
		
		Map<String, File> testFiles = DataHelper.getFilesFromDirectory(Constants.TEST_PATH);
		colouriseData(baseImagePath, State.TEST, testFiles, new HashMap<String, File>());
	}
	private static void colouriseData(String baseImagePath, State phase,
			Map<String, File> trainingFiles, Map<String, File> resultFiles) {
		
		int mainColourTolerance = 80;
		int subColourTolerance = 140;
		
		
		baseImagePath = "src/main/resources/" + baseImagePath + "/" + phase.toString().toLowerCase() + "/";
		ParametersContainer parameterContainer = ParametersContainer.getInstance();
		for (String fileName : trainingFiles.keySet()) {
			
			File trainFile = trainingFiles.get(fileName);
			File segmentedFile = resultFiles.get(fileName + Constants.RESULT_IMAGE_SUFFIX);
			
			ImageDTO image = DataHelper.getSingleImageSegmented(trainFile, segmentedFile, phase, parameterContainer);
			String imageName = DataHelper.getFileNameFromImageDTO(image);
			
			
			List<SuperPixelDTO> superPixels = getSuperPixelsCached(image, phase.toString());
			
			for (SuperPixelDTO superPixel : superPixels) {
				double[] meanRGB = superPixel.getMeanRGB();
				Color identifingColour = new Color((int)(meanRGB[0]),(int)(meanRGB[1]),(int)(meanRGB[2]));
				Color fillColour;
				if (identifingColour.equals(Color.RED)) {
					int red = ThreadLocalRandom.current().nextInt(256 - mainColourTolerance, 256);
					int green = ThreadLocalRandom.current().nextInt(0, subColourTolerance);
					int blue = ThreadLocalRandom.current().nextInt(0, subColourTolerance);
					fillColour = new Color(red, green, blue);
					
				} else if (identifingColour.equals(Color.GREEN)) {
					int red = ThreadLocalRandom.current().nextInt(0, subColourTolerance);
					int green = ThreadLocalRandom.current().nextInt(256 - mainColourTolerance, 256);
					int blue = ThreadLocalRandom.current().nextInt(0, subColourTolerance);
					fillColour = new Color(red, green, blue);
					
				} else if (identifingColour.equals(Color.BLUE)) {
					int red = ThreadLocalRandom.current().nextInt(0, subColourTolerance);
					int green = ThreadLocalRandom.current().nextInt(0, subColourTolerance);
					int blue = ThreadLocalRandom.current().nextInt(256 - mainColourTolerance, 256);
					fillColour = new Color(red, green, blue);
				} else {
					throw new RuntimeException();
				}
				List<PixelDTO> pixels = superPixel.getPixels();
				
				for (PixelDTO pixel : pixels) {
					pixel.setR(fillColour.getRed());
					pixel.setG(fillColour.getGreen());
					pixel.setB(fillColour.getBlue());
				}
			}
			
			DataHelper.saveImageBySuperPixelsPixelData(image, superPixels, baseImagePath + imageName + ".png");
				
		}
	}
}
