package masters;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class Constants {



	public static Logger _log = Logger.getLogger(App.class);

	public static boolean USE_NON_LINEAR_MODEL = true;
	public static boolean USE_GRID_MODEL = true;
	public static boolean ADD_NEIGBOUR_FEATURES = true;
	public static boolean ADD_COLOUR_LOCAL_FEATURE = true;
	public static boolean ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION = true;
	
	public static enum ColorSpace {
	    RGB, HSL, HSV, CIELAB, HISTOGRAM
	}
	
	public static ColorSpace colorSpace = ColorSpace.RGB; 
//	public static ColorSpace colorSpace = ColorSpace.HSL; 
//	public static ColorSpace colorSpace = ColorSpace.HSV; 
//	public static ColorSpace colorSpace = ColorSpace.CIELAB; 
//	public static ColorSpace colorSpace = ColorSpace.HISTOGRAM;
	
	public static enum State {
	    TRAIN, TEST, VALIDATION
	}
	
	public static enum ColorAverageMethod {
	    MEAN, POPULARITY
	}
	public static ColorAverageMethod colorAverageMethod = ColorAverageMethod.POPULARITY; 
//	public static ColorAverageMethod colorAverageMethod = ColorAverageMethod.MEAN; 
	
	public static int COLOR_FEATURE_HISTOGRAM_DIVISIONS = 16;
	
	//image path
	
	public static enum ImageFolder {
	    cow, horse, paint, pig, paint_neighbours, generated, generated2, generated_large, generated_large_noise,
	    generated_large_noise_0, generated_large_noise_1, generated_large_noise_2, generated_large_noise_3,
	    generated_large_noise_4, generated_large_noise_5, generated_large_noise_6, generated_large_noise_7,
	    generated_large_noise_8, generated_large_noise_9, generated_large_noise_10, generated_large_noise_11,
	    generated_large_noise_12, generated_large_noise_13, generated_large_noise_14, generated_large_noise_15,
	    generated_large_noise_16, generated_large_noise_17, generated_large_noise_18, generated_large_noise_19,
	    generated_large_noise_20, generated_equal,
	    generated_equals_noise_0, generated_equals_noise_1, generated_equals_noise_2, generated_equals_noise_3,
	    generated_equals_noise_4, generated_equals_noise_5, generated_equals_noise_6, generated_equals_noise_7,
	    generated_equals_noise_8, generated_equals_noise_9, generated_equals_noise_10, generated_equals_noise_11,
	    generated_equals_noise_12, generated_equals_noise_13, generated_equals_noise_14, generated_equals_noise_15,
	    generated_equals_noise_16, generated_equals_noise_17, generated_equals_noise_18, generated_equals_noise_19,
	    generated_equals_noise_20,
	    generated_version2,
	    generated_version2_noise_0, generated_version2_noise_1, generated_version2_noise_2, generated_version2_noise_3,
	    generated_version2_noise_4, generated_version2_noise_5, generated_version2_noise_6, generated_version2_noise_7,
	    generated_version2_noise_8, generated_version2_noise_9, generated_version2_noise_10, generated_version2_noise_11,
	    generated_version2_noise_12, generated_version2_noise_13, generated_version2_noise_14, generated_version2_noise_15,
	    generated_version2_noise_16, generated_version2_noise_17, generated_version2_noise_18, generated_version2_noise_19,
	    generated_version2_noise_20,
	    generated_03_01,
	    generated_03_01_noise_0, generated_03_01_noise_1, generated_03_01_noise_2, generated_03_01_noise_3,
	    generated_03_01_noise_4, generated_03_01_noise_5, generated_03_01_noise_6, generated_03_01_noise_7,
	    generated_03_01_noise_8, generated_03_01_noise_9, generated_03_01_noise_10, generated_03_01_noise_11,
	    generated_03_01_noise_12, generated_03_01_noise_13, generated_03_01_noise_14, generated_03_01_noise_15,
	    generated_03_01_noise_16, generated_03_01_noise_17, generated_03_01_noise_18, generated_03_01_noise_19,
	    generated_03_01_noise_20,
	}
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.generated_equal;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.horse;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.pig;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.paint;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.paint_neighbours;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.generated;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.generated2;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.generated_large;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.generated_large_noise;
//	public static ImageFolder IMAGE_FOLDER = ImageFolder.generated_version2;
	public static ImageFolder IMAGE_FOLDER = ImageFolder.generated_03_01;
	
	
	public static ImageFolder SUPERPIXEL_IMAGE_FOLDER = ImageFolder.generated_03_01;
	
	
	public static String TRAIN_PATH = IMAGE_FOLDER + File.separator + "train" + File.separator ;
	public static String TRAIN_RESULT_PATH = IMAGE_FOLDER + File.separator + "result" + File.separator ;
	public static String VALIDATION_PATH = IMAGE_FOLDER + File.separator + "validation" + File.separator ;
	public static String VALIDATION_RESULT_PATH = IMAGE_FOLDER + File.separator + "validation_result" + File.separator ;
	
	public static String TEST_PATH = IMAGE_FOLDER + File.separator + "test" + File.separator;
	public static String WORK_PATH = System.getProperty("user.dir") + File.separator + "src" + File.separator + "work" + File.separator; 
	public static String RESULT_IMAGE_SUFFIX = "_N";
	public static String IMAGE_EXTENSION= "png";
	
	public static int TRAIN_IMAGE_LIMIT = 1000;
	public static int TEST_IMAGE_LIMIT = 10;
	
	// training
	public static int NUMBER_OF_ITERATIONS = 100;
	public static double REGULARIZATION_FACTOR = 1000;
	public static double TRAINING_STEP = 0.10000;
	
	//factorisation
	public static int NUMBER_OF_STATES = 4;
	public static double CONVERGENCE_TOLERANCE = 0.3;
	
	// image input
	
	public static List<Color> AVAILABLE_COLOURS = Arrays.asList(new Color[] {Color.RED, Color.GREEN, Color.BLUE });
	public static Set<Color> AVAILABLE_COLOURS_SET = new HashSet<Color>(AVAILABLE_COLOURS);
	
	public static List<String>	SEGMENTED_HEX_COLOURS = Arrays.asList(new String [] {"#000000","#ffffff","#7f7f7f", "#7f007f"});
	public static Map<Color,Color> COLOR_TO_MARKING_COLOUR_MAP = new HashMap<Color, Color>();
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
        
        COLOR_TO_MARKING_COLOUR_MAP.put(Color.RED, label0Colour);
        COLOR_TO_MARKING_COLOUR_MAP.put(Color.GREEN, label1Colour);
        COLOR_TO_MARKING_COLOUR_MAP.put(Color.BLUE, label2Colour);
	}
	
	//superpixels 
	public static int NUMBER_OF_SUPERPIXELS = 500;
	public static double RIGIDNESS = 3;
	
	 /* serialization */
  
	public static boolean CLEAR_CACHE = false;
	public static String IMAGE_TO_FACTOR_GRAPH_TRAIN_FILE_NAME = "image_to_factor_graph";
	public static String IMAGE_TO_FACTOR_GRAPH_TEST_FILE_NAME = "image_to_factor_graph_test";
	
	/* Parzen */
	public static int GRID_SIZE = 3;
	public static int NEIGHBOURHOOD_SIZE = 2;
	public static double KERNEL_BANDWIDTH = 0.03; 
	public static double MEAN_SUPERPIXEL_DISTANCE_MULTIPLIER = 1; 
  
	
	public static boolean USE_HISTOGRAMS_3D = false;
	public static int NUMBER_OF_HISTOGRAM_DIVISIONS = 21;
	public static boolean USE_GMM_ESTIMATION = false;
	public static boolean USE_HISTOGRAM_ESTIMATION = true;
}
