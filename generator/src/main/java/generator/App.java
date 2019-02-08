package generator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import colours.ColourUtil;
import colours.Colouriser;
import generator.GeneratorConstants.Shape;
import graphics.BufferedImageFactoryUtil;
import graphics.GraphicsFactoryUtil;
import masters.Constants;
import masters.Constants.State;
import masters.colors.ColorSpaceException;
import masters.image.ImageDTO;
import masters.image.PixelDTO;
import masters.superpixel.SuperPixelDTO;
import masters.superpixel.SuperPixelHelper;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;
import noise.Noiser;
import shapes.ShapeDrawer;
import util.FileUtil;

public class App {

	public static int IMAGE_WIDTH = GeneratorConstants.WIDTH;
	public static int IMAGE_HEIGHT = GeneratorConstants.HEIGHT;
	
    public static void main( String[] args ) throws IOException {
    	    	Random rand = new Random();
    	    	
    	boolean createEqualImages = false;
    	
    	boolean linearCase = false;
    	boolean generateLinearBasicData = false;
    	boolean generateLinearColourfullData = false;
    	boolean addNoise = true;
    	int noiseLevel = 0; // % of chance that a superpixel will be noised
    	
    	
    	if (linearCase) {
    		if (generateLinearBasicData) {
    			generateLinearBasicData();
    			return;
        	} 
    		if (generateLinearColourfullData) {
    			String finalPath = GeneratorConstants.IMAGE_FOLDER + "_coloured_noise_" + noiseLevel + "//";
    			Colouriser.colouriseData(finalPath, noiseLevel);
    			return;
    		}
    		
		} else {
			if (generateLinearColourfullData) {
    			String finalPath = GeneratorConstants.IMAGE_FOLDER + "_coloured_noise_" + noiseLevel + "//";
    			Colouriser.colouriseData(finalPath, noiseLevel);
    			return;
    		}
			if (createEqualImages) {
	    		createEqualImages();
	    		return;
	    	}
	    	if (addNoise) {
//	    		for (int i = 1; i < 20; i++) {
	    			System.out.println("noise " + 19);
	    			String finalPath = GeneratorConstants.IMAGE_FOLDER + "_noise_" + 19 + "//";
	    			Noiser.addNoiseToData(finalPath, 19);
//	    		}
	    		return;
	    	} else {
	    		generateNonLinearData();
	    		return;
		    }
		}
    }

    private static void generateTestResultsData() {
    	Map<String, File> testFiles = DataHelper.getFilesFromDirectory(Constants.TEST_PATH);
    	ParametersContainer parameterContainer = ParametersContainer.getInstance();
    	State phase = State.TEST;
    	String baseImagePath = "src/main/resources/" + GeneratorConstants.IMAGE_FOLDER + "/" + phase.toString().toLowerCase() + "_result/";
    	
    	for (String fileName : testFiles.keySet()) {
			File trainFile = testFiles.get(fileName);
			
			ImageDTO image = DataHelper.getSingleImageSegmented(trainFile, null, null, phase, parameterContainer);
			String imageName = DataHelper.getFileNameFromImageDTO(image);
			List<SuperPixelDTO> superPixels = SuperPixelHelper.getSuperPixelsCached(image, phase.toString(), null);
			for (SuperPixelDTO superPixel : superPixels) {
				Color superPixelColor = DataHelper.getColorFromRGB(superPixel.getMeanRGB());
				Color labelColor = ShapeDrawer.getColorMarkup(superPixelColor);
				List<PixelDTO> pixels = superPixel.getPixels();
				for (PixelDTO pixel : pixels) {
					pixel.setR(labelColor.getRed());
					pixel.setG(labelColor.getGreen());
					pixel.setB(labelColor.getBlue());
				}
			}
			DataHelper.saveImageBySuperPixelsPixelData(image, superPixels, baseImagePath + imageName + "_N.png");
		}
    }
	private static void generateNonLinearData() {
		Random rand = new Random();
		// generate new data
		if (GeneratorConstants.GENERATOR_VERSION == 1) {
//			// generate different shapes - circle, square, pentagon - H as main object. Red Green and Blue
//			for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_TRAIN_IMAGES; i++) {
//				BufferedImage trainImg = BufferedImageFactoryUtil.getImage();
//		    	Graphics2D trainG2d = GraphicsFactoryUtil.getGraphics(trainImg);
//		    	
//				BufferedImage resultImg = BufferedImageFactoryUtil.getImage();
//		    	Graphics2D resultG2d = GraphicsFactoryUtil.getGraphics(resultImg);
//		    	
//		    	Color baseColour = Color.blue;
//				ColourUtil.fillColour(trainG2d, resultG2d, baseColour);
//		    	ColourUtil.fillGraphicsWithRandomColours(trainG2d, resultG2d, baseColour);
//		    	if (rand.nextInt() % 2 == 0) {
//		    		ShapeDrawer.initDrawH(trainG2d, resultG2d);
//		    	} else {
//		    		ShapeDrawer.initDrawOthers(trainG2d, resultG2d);
//		    	}
//		    	
//		    	
//		    	FileUtil.saveImage(trainImg, GeneratorConstants.TRAIN_PATH,  (i + "." + Constants.IMAGE_EXTENSION));
//		    	FileUtil.saveImage(resultImg, GeneratorConstants.RESULT_PATH, (i + Constants.RESULT_IMAGE_SUFFIX + "." + Constants.IMAGE_EXTENSION));
//			}
//			
			for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_VALIDATION_IMAGES; i++) {
				BufferedImage validationImg = BufferedImageFactoryUtil.getImage();
		    	Graphics2D validationG2d = GraphicsFactoryUtil.getGraphics(validationImg);
		    	
				BufferedImage resultImg = BufferedImageFactoryUtil.getImage();
		    	Graphics2D resultG2d = GraphicsFactoryUtil.getGraphics(resultImg);
		    	
		    	Color baseColour = Color.blue;
				ColourUtil.fillColour(validationG2d, resultG2d, baseColour);
		    	ColourUtil.fillGraphicsWithRandomColours(validationG2d, resultG2d, baseColour);
		    	if (rand.nextInt() % 2 == 0) {
		    		ShapeDrawer.initDrawH(validationG2d, resultG2d);
		    	} else {
		    		ShapeDrawer.initDrawOthers(validationG2d, resultG2d);
		    	}
		    	
		    	
		    	FileUtil.saveImage(validationImg, GeneratorConstants.VALIDATION_PATH,  (i + "." + Constants.IMAGE_EXTENSION));
		    	FileUtil.saveImage(resultImg, GeneratorConstants.VALIDATION_RESULT_PATH, (i + Constants.RESULT_IMAGE_SUFFIX + "." + Constants.IMAGE_EXTENSION));
			}
			
//			for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_TEST_IMAGES; i++) {
//				BufferedImage testImg = BufferedImageFactoryUtil.getImage();
//				Graphics2D testG2d = GraphicsFactoryUtil.getGraphics(testImg);
//				
//				Color baseColour = Color.blue;
//				ColourUtil.fillColour(testG2d, null, baseColour);
//				ColourUtil.fillGraphicsWithRandomColours(testG2d, null, baseColour);
//				if (rand.nextInt(100) > 60) {
//					ShapeDrawer.initDrawH(testG2d, null);
//				} else {
//					ShapeDrawer.initDrawOthers(testG2d, null);
//				}
//				FileUtil.saveImage(testImg, GeneratorConstants.TEST_PATH, (i + ".png"));
//			}
		} else if (GeneratorConstants.GENERATOR_VERSION == 2) {
			// only circle and square
			for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_TRAIN_IMAGES; i++) {
				BufferedImage trainImg = BufferedImageFactoryUtil.getImage();
		    	Graphics2D trainG2d = GraphicsFactoryUtil.getGraphics(trainImg);
		    	
				BufferedImage resultImg = BufferedImageFactoryUtil.getImage();
		    	Graphics2D resultG2d = GraphicsFactoryUtil.getGraphics(resultImg);
		    	
				
		    	int index = rand.nextInt();
				if (index % 3 == 0) {
		    		ShapeDrawer.initDrawVersion2(trainG2d, resultG2d, Shape.SQUARE, true);
		    	} else if (index % 3 == 1) {
		    		ShapeDrawer.initDrawVersion2(trainG2d, resultG2d, Shape.CIRCLE, false);
		    	} else {
		    		Color baseColour = Color.blue;
		    		ColourUtil.fillColour(trainG2d, resultG2d, baseColour);
		    		ColourUtil.fillGraphicsWithRandomColours(trainG2d, resultG2d, baseColour);
		    	}
		    	
		    	
		    	FileUtil.saveImage(trainImg, GeneratorConstants.TRAIN_PATH,  (i + "." + Constants.IMAGE_EXTENSION));
		    	FileUtil.saveImage(resultImg, GeneratorConstants.RESULT_PATH, (i + Constants.RESULT_IMAGE_SUFFIX + "." + Constants.IMAGE_EXTENSION));
			}
			
			for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_VALIDATION_IMAGES; i++) {
				BufferedImage validationImg = BufferedImageFactoryUtil.getImage();
		    	Graphics2D validationG2d = GraphicsFactoryUtil.getGraphics(validationImg);
		    	
				BufferedImage resultImg = BufferedImageFactoryUtil.getImage();
		    	Graphics2D resultG2d = GraphicsFactoryUtil.getGraphics(resultImg);
		    	
		    	int index = rand.nextInt();
				if (index % 3 == 0) {
		    		ShapeDrawer.initDrawVersion2(validationG2d, resultG2d, Shape.SQUARE, true);
		    	} else if (index % 3 == 1) {
		    		ShapeDrawer.initDrawVersion2(validationG2d, resultG2d, Shape.CIRCLE, false);
		    	} else {
		    		Color baseColour = Color.blue;
		    		ColourUtil.fillColour(validationG2d, resultG2d, baseColour);
		    		ColourUtil.fillGraphicsWithRandomColours(validationG2d, resultG2d, baseColour);
		    	}
		    	
		    	
		    	FileUtil.saveImage(validationImg, GeneratorConstants.VALIDATION_PATH,  (i + "." + Constants.IMAGE_EXTENSION));
		    	FileUtil.saveImage(resultImg, GeneratorConstants.VALIDATION_RESULT_PATH, (i + Constants.RESULT_IMAGE_SUFFIX + "." + Constants.IMAGE_EXTENSION));
			}
			
			for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_TEST_IMAGES; i++) {
				BufferedImage testImg = BufferedImageFactoryUtil.getImage();
				Graphics2D testG2d = GraphicsFactoryUtil.getGraphics(testImg);
				
				int index = rand.nextInt();
				if (index % 3 == 0) {
		    		ShapeDrawer.initDrawVersion2(testG2d, null, Shape.SQUARE, true);
		    	} else if (index % 3 == 1) {
		    		ShapeDrawer.initDrawVersion2(testG2d, null, Shape.CIRCLE, false);
		    	} else {
		    		Color baseColour = Color.blue;
		    		ColourUtil.fillColour(testG2d, null, baseColour);
		    		ColourUtil.fillGraphicsWithRandomColours(testG2d, null, baseColour);
		    	}
		    	
				FileUtil.saveImage(testImg, GeneratorConstants.TEST_PATH, (i + ".png"));
			}
			
		}
	}

	private static void createEqualImages() throws IOException {
		String basePath = "E:\\Studia\\CSIT\\praca_magisterska\\repository\\crf\\src\\main\\resources\\generated_equal\\";
		String workPath = "E:\\Studia\\CSIT\\praca_magisterska\\repository\\crf\\src\\work\\generated_equal\\";
		
		String trainPath = basePath + "train\\";
		String resultPath = basePath + "result\\";
		String testPath = basePath + "test\\";
		String validationPath = basePath + "validation\\";
		String vaidation_resultPath = basePath + "validation_result\\";
		
		
		File trainFile1 = new File(trainPath + "0.png");
		File trainFile2 = new File(trainPath + "0.png");
		File resultFile1 = new File(resultPath + "0_N.png");
		File resultFile2 = new File(resultPath + "0_N.png");
		
		File image1Division = new File(workPath + "TRAIN_0.txt");
		File image2Division = new File(workPath + "TRAIN_0.txt");
		
		// generate train
		for (int i = 2; i < 101; i++) {
			File newTrainFile1 = new File(trainPath + i + ".png");
			File newResultFile1 = new File(resultPath + i + "_N.png");
			File trainDivision1 = new File(workPath + "TRAIN_" + i + ".txt");

			File newTrainFile2 = new File(trainPath + (++i) + ".png");
			File newResultFile2 = new File(resultPath + i + "_N.png");
			File trainDivision2 = new File(workPath + "TRAIN_" + i + ".txt");
			
			
		    FileUtils.copyFile(trainFile1, newTrainFile1);
		    FileUtils.copyFile(trainFile2, newTrainFile2);
		    FileUtils.copyFile(resultFile1, newResultFile1);
		    FileUtils.copyFile(resultFile2, newResultFile2);
		    
		    FileUtils.copyFile(image1Division, trainDivision1);
		    FileUtils.copyFile(image2Division, trainDivision2);
		}
		
		// generate validation
		for (int i = 0; i < 5; i++) {
			File newValdationFile1 = new File(validationPath + i + ".png");
			File validationDivision1 = new File(workPath + "VALIDATION_" + i + ".txt");
			File newValdationResultFile1 = new File(vaidation_resultPath + i + "_N.png");
			File newTestFile1 = new File(testPath + i + ".png");
			File testDivision1 = new File(workPath + "TEST_" + i + ".txt");


			File newValidationFile2 = new File(validationPath + (++i) + ".png");
			File validationDivision2 = new File(workPath + "VALIDATION_" + i + ".txt");
			File newValidationResultFile2 = new File(vaidation_resultPath + i + "_N.png");
			File newTestFile2 = new File(testPath + i + ".png");
			File testDivision2 = new File(workPath + "TEST_" + i + ".txt");
			
			FileUtils.copyFile(trainFile1, newValdationFile1);
		    FileUtils.copyFile(trainFile2, newValidationFile2);
		    FileUtils.copyFile(resultFile1, newValdationResultFile1);
		    FileUtils.copyFile(resultFile2, newValidationResultFile2);
		    FileUtils.copyFile(trainFile1, newTestFile1);
		    FileUtils.copyFile(trainFile2, newTestFile2);
		    
		    FileUtils.copyFile(image1Division, validationDivision1);
		    FileUtils.copyFile(image1Division, testDivision1);
		    FileUtils.copyFile(image2Division, validationDivision2);
		    FileUtils.copyFile(image2Division, testDivision2);
		}
	}

	private static void generateLinearBasicData() {
		for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_TRAIN_IMAGES; i++) {
			BufferedImage trainImg = BufferedImageFactoryUtil.getImage();
			Graphics2D trainG2d = GraphicsFactoryUtil.getGraphics(trainImg);
			
			BufferedImage resultImg = BufferedImageFactoryUtil.getImage();
			Graphics2D resultG2d = GraphicsFactoryUtil.getGraphics(resultImg);
			
			ShapeDrawer.drawNonLinearShapes(trainG2d, resultG2d);
			
			FileUtil.saveImage(trainImg, GeneratorConstants.TRAIN_PATH,  (i + "." + Constants.IMAGE_EXTENSION));
			FileUtil.saveImage(resultImg, GeneratorConstants.RESULT_PATH, (i + Constants.RESULT_IMAGE_SUFFIX + "." + Constants.IMAGE_EXTENSION));
		}
		for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_TEST_IMAGES; i++) {
			BufferedImage testImg = BufferedImageFactoryUtil.getImage();
			Graphics2D testG2d = GraphicsFactoryUtil.getGraphics(testImg);
			
			ShapeDrawer.drawNonLinearShapes(testG2d, null);
			
			FileUtil.saveImage(testImg, GeneratorConstants.TEST_PATH, (i + ".png"));
		}
	}
}
