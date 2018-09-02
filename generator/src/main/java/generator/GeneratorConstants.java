package generator;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import masters.Constants;

public class GeneratorConstants {
	public static int WIDTH = 300;
	public static int HEIGHT = 300;
	
	public static Color FOREGROUND_COLOR = Color.RED;
	public static int BLOCK_SIZE = 30;
	public static int MAX_SCALE = 2;
	
	
	public static int NEIGHBOURHOOD_GRID_MAX_LEVEL = 2; 
	
	public static List<Color> AVAILABLE_COLOURS = Arrays.asList(new Color[] {Color.RED, Color.GREEN, Color.BLUE });
	public static Set<Color> AVAILABLE_COLOURS_SET = new HashSet<Color>(AVAILABLE_COLOURS);
	public static Map<Color, Integer> COLOR_TO_LABEL_MAP = new HashMap<Color, Integer> ();
	public static Map<Integer, Color> LABEL_TO_MARKUP_MAP = new HashMap<Integer, Color> ();
	
	static {
		COLOR_TO_LABEL_MAP.put(Color.RED, 0);
		COLOR_TO_LABEL_MAP.put(Color.GREEN, 1);
		COLOR_TO_LABEL_MAP.put(Color.BLUE, 2);
		
		LABEL_TO_MARKUP_MAP.put(0, Color.BLACK);
		LABEL_TO_MARKUP_MAP.put(1, Color.WHITE);
		LABEL_TO_MARKUP_MAP.put(2, new Color(127, 127, 127));
		LABEL_TO_MARKUP_MAP.put(3, new Color(127, 0, 127));
	}
	
	
	
	
	public static int NUMBER_OF_GENERATED_TRAIN_IMAGES = 100;
	public static int NUMBER_OF_GENERATED_TEST_IMAGES = 25;
	public static String IMAGE_FOLDER = "generated1";
	
	public static String TRAIN_PATH = IMAGE_FOLDER + File.separator + "train" + File.separator;
	public static String RESULT_PATH = IMAGE_FOLDER + File.separator + "result" + File.separator ;
	public static String TEST_PATH = IMAGE_FOLDER + File.separator + "test" + File.separator;
	

}
