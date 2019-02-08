package generator;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import masters.Constants;

public class GeneratorConstants {
	public static int WIDTH = 400;
	public static int HEIGHT = 400;
	
	public static Color FOREGROUND_COLOR = Color.RED;
	public static Color NEIGHBOUR_COLOR = Color.GREEN;
	public static int BLOCK_SIZE = 40;
	public static int NEIGHBOURHOOD_BLOCK_SIZE = 25;
	
	public static int NEIGHBOURHOOD_GRID = 2; 
	public static int BUFFER = 0; 
	
	public static List<Color> AVAILABLE_COLOURS = Constants.AVAILABLE_COLOURS;
	public static Set<Color> AVAILABLE_COLOURS_SET = Constants.AVAILABLE_COLOURS_SET;
	public static Map<Color, Integer> COLOR_TO_LABEL_MAP = new HashMap<Color, Integer> ();
	public static Map<Integer, Color> LABEL_TO_MARKUP_MAP = new HashMap<Integer, Color> ();
	
	public static Map<Color, List<Color>> COLOR_TO_AVAILABLE_NOISE_MAP = new HashMap<Color, List<Color>> ();
	
	static {
		COLOR_TO_LABEL_MAP.put(Color.RED, 0);
		COLOR_TO_LABEL_MAP.put(Color.GREEN, 1);
		COLOR_TO_LABEL_MAP.put(Color.BLUE, 2);
		
		LABEL_TO_MARKUP_MAP.put(0, Color.BLACK);
		LABEL_TO_MARKUP_MAP.put(1, Color.WHITE);
		LABEL_TO_MARKUP_MAP.put(2, new Color(127, 127, 127));
		LABEL_TO_MARKUP_MAP.put(3, new Color(127, 0, 127));
		
		COLOR_TO_AVAILABLE_NOISE_MAP.put(Color.RED, Arrays.asList(new Color [] {Color.GREEN, Color.BLUE}));
		COLOR_TO_AVAILABLE_NOISE_MAP.put(Color.GREEN, Arrays.asList(new Color [] {Color.BLUE, Color.RED}));
		COLOR_TO_AVAILABLE_NOISE_MAP.put(Color.BLUE, Arrays.asList(new Color [] {Color.GREEN, Color.RED}));
	}
	
	
	public static int NUMBER_OF_GENERATED_TRAIN_IMAGES = 1000;
	public static int NUMBER_OF_GENERATED_TEST_IMAGES = 30;
	public static int NUMBER_OF_GENERATED_VALIDATION_IMAGES = 150;
	public static String IMAGE_FOLDER = "generated_03_01";
	
	public static String TRAIN_PATH = IMAGE_FOLDER + "/train/";
	public static String RESULT_PATH = IMAGE_FOLDER + "/result/";
	
	public static String VALIDATION_PATH = IMAGE_FOLDER + "/validation/";
	public static String VALIDATION_RESULT_PATH = IMAGE_FOLDER + "/validation_result/";
	
	
	public static String TEST_PATH = IMAGE_FOLDER + "/test/";
	

	public static enum Direction {
		UP_RIGHT, UP_LEFT, DOWN_RIGHT, DOWN_LEFT
	}
	
	public static enum Shape {
		SQUARE, CIRCLE, PENTAGON, HEXAGON
	}
	
	public static final List<Shape> availableShapes = Arrays.asList(Shape.SQUARE, Shape.CIRCLE);
	
	
	public static boolean ADD_NOISE = true;
	public static int GENERATOR_VERSION = 1;
}
