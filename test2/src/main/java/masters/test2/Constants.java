package masters.test2;

import org.apache.log4j.Logger;

import masters.test2.sampler.GibbsSampler;

public class Constants {

	public static Logger _log = Logger.getLogger(App.class);

	public static enum ColorSpace {
	    RGB, HSL, HSV, CIELAB, HISTOGRAM
	}
	
//	public static ColorSpace colorSpace = ColorSpace.RGB; 
//	public static ColorSpace colorSpace = ColorSpace.HSL; 
//	public static ColorSpace colorSpace = ColorSpace.HSV; 
	public static ColorSpace colorSpace = ColorSpace.CIELAB; 
//	public static ColorSpace colorSpace = ColorSpace.HISTOGRAM;
	
	public static int NUMBER_OF_HISTOGRAM_DIVISIONS = 16;
	
	//image path
	
	public static enum ImageFolder {
	    cow, horse, paint, pig
	}
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.cow;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.horse;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.pig;
	public static ImageFolder IMAGE_FOLDER = ImageFolder.paint;
	
	public static String MAIN_PATH = System.getProperty("user.dir") + "\\src\\resources\\";
	public static String TRAIN_PATH = MAIN_PATH + IMAGE_FOLDER + "\\train\\" ;
	public static String RESULT_PATH = MAIN_PATH + IMAGE_FOLDER + "\\result\\" ;
	public static String TEST_PATH = MAIN_PATH + IMAGE_FOLDER + "\\test\\" ;
	
	
	public static int TRAIN_IMAGE_LIMIT = 10;
	public static int TEST_IMAGE_LIMIT = 10;
	
	
	// training
	public static int NUMBER_OF_ITERATIONS = 1000;
	public static double REGULARIZATION_FACTOR = 1000;
	public static double TRAINING_STEP = 0.000001;
	
	//factorisation
	public static int NUMBER_OF_STATES = 3;
	public static double CONVERGENCE_TOLERANCE = 0.000001;
}
