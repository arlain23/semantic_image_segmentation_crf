package noise;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import generator.GeneratorConstants;
import masters.colors.ColorSpaceException;
import masters.image.ImageDTO;
import masters.image.PixelDTO;
import masters.superpixel.SuperPixelDTO;
import masters.superpixel.SuperPixelHelper;
import masters.utils.DataHelper;

public class Noiser {
	
	
	private static List<SuperPixelDTO> getSuperPixelsCached(ImageDTO imageDTO, String prefix) throws IOException, ColorSpaceException {
		return SuperPixelHelper.getSuperPixelsCached(imageDTO, prefix);
	}
	public static void addNoiseToData(String baseImagePath, int noiseNumber) {
		List<ImageDTO> trainingData = DataHelper.getTrainingDataSegmented();
		List<ImageDTO> testData = DataHelper.getTestData();
		List<ImageDTO> validationData = DataHelper.getValidationDataSegmented();
		
		addNoiseToData(baseImagePath, "train", trainingData, noiseNumber);
		addNoiseToData(baseImagePath, "test", testData, noiseNumber);
		addNoiseToData(baseImagePath, "validation", validationData, noiseNumber);
	}
	private static void addNoiseToData(String baseImagePath, String phase, List<ImageDTO> data, int noiseNumberLimit) {
		baseImagePath = "src/main/resources/" + baseImagePath + "/" + phase + "/";
		Random rand = new Random();
		int limit = 40;
		if (phase.equals("test")) {
			limit = 20;
		}
		for (ImageDTO image : data) {
    		int r = rand.nextInt(100);
			String imageName = DataHelper.getFileNameFromImageDTO(image);
			try {
				List<SuperPixelDTO> superPixels = getSuperPixelsCached(image, phase);
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
