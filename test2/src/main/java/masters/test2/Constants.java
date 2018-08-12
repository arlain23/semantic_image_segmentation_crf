package masters.test2;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Constants {

	public static Logger _log = Logger.getLogger(App.class);

	public static boolean USE_NON_LINEAR_MODEL = true;
	public static boolean INCLUDE_DESCRETE_FEATURES = false;
	public static boolean INCLUDE_CONTINUOUS_FEATURES = true;
	
	public static enum ColorSpace {
	    RGB, HSL, HSV, CIELAB, HISTOGRAM
	}
	
	public static ColorSpace colorSpace = ColorSpace.RGB; 
//	public static ColorSpace colorSpace = ColorSpace.HSL; 
//	public static ColorSpace colorSpace = ColorSpace.HSV; 
//	public static ColorSpace colorSpace = ColorSpace.CIELAB; 
//	public static ColorSpace colorSpace = ColorSpace.HISTOGRAM;
	
	public static enum ColorAverageMethod {
	    MEAN, POPULARITY
	}
	public static ColorAverageMethod colorAverageMethod = ColorAverageMethod.POPULARITY; 
//	public static ColorAverageMethod colorAverageMethod = ColorAverageMethod.MEAN; 
	
	public static int NUMBER_OF_HISTOGRAM_DIVISIONS = 16;
	
	//image path
	
	public static enum ImageFolder {
	    cow, horse, paint, pig, paint_neighbours
	}
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.cow;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.horse;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.pig;
	public static ImageFolder IMAGE_FOLDER = ImageFolder.paint;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.paint_neighbours;
	
	public static String MAIN_PATH = System.getProperty("user.dir") + "\\src\\resources\\";
	public static String TRAIN_PATH = MAIN_PATH + IMAGE_FOLDER + "\\train\\" ;
	public static String RESULT_PATH = MAIN_PATH + IMAGE_FOLDER + "\\result\\" ;
	public static String TEST_PATH = MAIN_PATH + IMAGE_FOLDER + "\\test\\" ;
	
	
	public static int TRAIN_IMAGE_LIMIT = 5;
	public static int TEST_IMAGE_LIMIT = 5;
	
	public static double KERNEL_BANDWIDTH = 0.25; 
	
	// training
	public static int NUMBER_OF_ITERATIONS = 1000;
	public static double REGULARIZATION_FACTOR = 1000;
	public static double TRAINING_STEP = 0.000001;
	
	//factorisation
	public static int NUMBER_OF_STATES = 3;
	public static double CONVERGENCE_TOLERANCE = 0.000001;
	
	// image input
	public static List<String>	SEGMENTED_HEX_COLOURS = Arrays.asList(new String [] {"#000000","#ffffff","#7f7f7f", "#7f007f"});
	public static Map<Integer,Color> LABEL_TO_COLOUR_MAP = new HashMap<Integer,Color>();
	static {
        Color label0Colour =  new Color(0, 0, 0); 		// Colour black
        Color label1Colour = new Color(255, 255, 255); 	// Colour white
        Color label2Colour = new Color(127,127,127); 	// Colour gray
        Color label3Colour = new Color(127,0,127); 	// Colour violet
        LABEL_TO_COLOUR_MAP.put(0, label0Colour);
        LABEL_TO_COLOUR_MAP.put(1, label1Colour);
        LABEL_TO_COLOUR_MAP.put(2, label2Colour);
        LABEL_TO_COLOUR_MAP.put(3, label3Colour);
	}
	
	//superpixels 
	public static int NUMBER_OF_SUPERPIXELS = 30;
	public static double RIGIDNESS = 5.0;
}
