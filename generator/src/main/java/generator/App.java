package generator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import colours.ColourUtil;
import graphics.BufferedImageFactoryUtil;
import graphics.GraphicsFactoryUtil;
import masters.Constants;
import noise.Noiser;
import shapes.ShapeDrawer;
import util.FileUtil;

public class App {

	public static int IMAGE_WIDTH = GeneratorConstants.WIDTH;
	public static int IMAGE_HEIGHT = GeneratorConstants.HEIGHT;
	
    public static void main( String[] args ) throws IOException {
    	
    	Random rand = new Random();
    	
    	if (GeneratorConstants.ADD_NOISE) {
    		String finalPath = GeneratorConstants.IMAGE_FOLDER + "_noise//";
    		Noiser.addNoiseToData(finalPath);
    	} else {
    		// generate new data
    	
	    	for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_TRAIN_IMAGES; i++) {
	    		BufferedImage trainImg = BufferedImageFactoryUtil.getImage();
	        	Graphics2D trainG2d = GraphicsFactoryUtil.getGraphics(trainImg);
	        	
	    		BufferedImage resultImg = BufferedImageFactoryUtil.getImage();
	        	Graphics2D resultG2d = GraphicsFactoryUtil.getGraphics(resultImg);
	        	
	        	Color baseColour = Color.blue;
				ColourUtil.fillColour(trainG2d, resultG2d, baseColour);
	        	ColourUtil.fillGraphicsWithRandomColours(trainG2d, resultG2d, baseColour);
	        	if (rand.nextInt() % 2 == 0) {
	        		ShapeDrawer.initDrawH(trainG2d, resultG2d);
	        	} else {
	        		ShapeDrawer.initDrawOthers(trainG2d, resultG2d);
	        	}
	        	
	        	
	        	FileUtil.saveImage(trainImg, GeneratorConstants.TRAIN_PATH,  (i + "." + Constants.IMAGE_EXTENSION));
	        	FileUtil.saveImage(resultImg, GeneratorConstants.RESULT_PATH, (i + Constants.RESULT_IMAGE_SUFFIX + "." + Constants.IMAGE_EXTENSION));
	    	}
	    	
	    	
	    	for (int i = 0; i < GeneratorConstants.NUMBER_OF_GENERATED_TEST_IMAGES; i++) {
	    		BufferedImage testImg = BufferedImageFactoryUtil.getImage();
	        	Graphics2D testG2d = GraphicsFactoryUtil.getGraphics(testImg);
	        	
	        	Color baseColour = Color.blue;
				ColourUtil.fillColour(testG2d, null, baseColour);
	        	ColourUtil.fillGraphicsWithRandomColours(testG2d, null, baseColour);
	        	if (rand.nextInt() % 2 == 0) {
	        		ShapeDrawer.initDrawH(testG2d, null);
	        	} else {
	        		ShapeDrawer.initDrawOthers(testG2d, null);
	        	}
	        	FileUtil.saveImage(testImg, GeneratorConstants.TEST_PATH, (i + ".png"));
	    	}
	    }
    }
}
