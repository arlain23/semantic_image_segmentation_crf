package masters.cache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.poi.util.StringUtil;

import masters.Constants;
import masters.features.Feature;

public class FeatureSelectionCacheHelper {
	private static String DELIMITER = ";";
	private static String NEW_LINE = System.lineSeparator();
	
	public static boolean isFeatureSelectionCached(int numberOfStepsForward, int numberOfStepsBackward) {
				
		String path = getFeatureSelectionCachePath(numberOfStepsForward, numberOfStepsBackward);
		File file = new File(path);
		return file.exists();
	}
	
	public static void cacheSelectedFeatureIds(int numberOfStepsForward, int numberOfStepsBackward, 
			List<Integer> selectedFeatureIds) {
		String path = getFeatureSelectionCachePath(numberOfStepsForward, numberOfStepsBackward);
		
		String result = selectedFeatureIds.stream()
			      .map(n -> String.valueOf(n))
			      .collect(Collectors.joining(DELIMITER, "", ""));
		
		saveStringToFile(path, result);
	}
	
	public static List<Integer> getCachedSelectedFeatureIds(int numberOfStepsForward, int numberOfStepsBackward) {
		try {
			String path = getFeatureSelectionCachePath(numberOfStepsForward, numberOfStepsBackward);
			File file = new File(path);
	  
			LineIterator it = FileUtils.lineIterator(file, "UTF-8");
			
			List<Integer> result = Arrays.asList(it.nextLine().split(DELIMITER)).stream()
	                .map(Integer::valueOf)
	                .collect(Collectors.toList());
			
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	private static String getFeatureSelectionCachePath(int numberOfStepsForward, int numberOfStepsBackward) {
		String constants = "colour_" + Constants.ADD_COLOUR_LOCAL_FEATURE + 
				"_color_position_" + Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION + 
				"_neigbour_" + Constants.ADD_NEIGBOUR_FEATURES;
				
		
		return Constants.WORK_PATH + "cache_selected_features" + 
				File.separator + Constants.IMAGE_FOLDER +
				File.separator + constants +
				File.separator + "steps_forward_" + numberOfStepsForward + "_steps_backward_" + numberOfStepsBackward;
	}
	
	
	public static void saveStringToFile(String path, String stringToSave) {
		CacheUtils.saveStringToFile(path, stringToSave);
	}
}
