package masters.superpixel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.colors.ColorSpaceException;
import masters.image.ImageDTO;
import masters.image.PixelDTO;
import masters.utils.CacheUtils;
import masters.utils.DataHelper;

public class SuperPixelHelper {
	
	public static List<SuperPixelDTO> getSuperPixels(ImageDTO imageDTO, List<List<Integer>> superPixelDivision) throws ColorSpaceException {
		List <SuperPixelDTO> superPixels = new ArrayList<SuperPixelDTO>();
		
		// read the output from the command
		Map<SuperPixelDTO,SuperPixelDTO> createdSuperPixels = new HashMap<SuperPixelDTO, SuperPixelDTO>();
		PixelDTO[][] pixelData = imageDTO.getPixelData();
		
		for(int rowIndex = 0; rowIndex < superPixelDivision.size(); rowIndex++) {
			List<Integer> row = superPixelDivision.get(rowIndex);
			for (int colIndex = 0; colIndex < row.size(); colIndex++) {
				
				PixelDTO currentPixel = pixelData[colIndex][rowIndex];
				int superPixelIndex = Integer.valueOf(row.get(colIndex));
				SuperPixelDTO tmpSuperPixel = new SuperPixelDTO(superPixelIndex);
				if (!createdSuperPixels.containsKey(tmpSuperPixel)) {
					superPixels.add(tmpSuperPixel);
					createdSuperPixels.put(tmpSuperPixel, tmpSuperPixel);
					tmpSuperPixel.addPixel(currentPixel);
				} else {
					SuperPixelDTO currentSuperPixel = createdSuperPixels.get(tmpSuperPixel);
					currentSuperPixel.addPixel(currentPixel);
				}
				currentPixel.setSuperPixelIndex(superPixelIndex);
			}
		}
		Collections.sort(superPixels);
		initBorderData(imageDTO, superPixels);
		for (SuperPixelDTO superPixel : superPixels) {
			superPixel.initMeanRGB();
			superPixel.initNeighbours(imageDTO.getPixelData(), superPixels);
		}
		return superPixels;
	}
	
	public static List<SuperPixelDTO> getSuperPixelsCached(ImageDTO imageDTO, String prefix) throws IOException, ColorSpaceException {
		List<List<Integer>> superPixelDivision = CacheUtils.getSuperPixelDivision(imageDTO, prefix);
		return getSuperPixels(imageDTO, superPixelDivision);
		
	}
	public static  List<SuperPixelDTO> getNewSuperPixels(ImageDTO imageDTO, int expectedNumberOfSuperpixels, double rigidness, String prefix, boolean savePath) throws IOException, ColorSpaceException {
		Runtime runtime = Runtime.getRuntime();
		String jarPath = System.getProperty("user.dir") + "\\src\\superpixel.jar";
		String command = "java -jar " + jarPath + " " + imageDTO.getPath() + " " + expectedNumberOfSuperpixels + " " + rigidness;
		Process proc = runtime.exec(command);
		
		BufferedReader stdInput = new BufferedReader(new 
			     InputStreamReader(proc.getInputStream()));
		
		List<List<Integer>> superPixelDivision = new ArrayList<>();
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			List<Integer> row = Arrays.asList(s.split(" ")).stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());
			superPixelDivision.add(row);
		}
		
		List<SuperPixelDTO> superPixels = getSuperPixels(imageDTO, superPixelDivision);
		CacheUtils.saveSuperPixelDivision(imageDTO, prefix);
		
		if (savePath) {
			String name = DataHelper.getFileNameFromImageDTO(imageDTO);
			name = name + "." + Constants.IMAGE_EXTENSION;
			String path = Constants.WORK_PATH + "segmentation" + File.separator +  Constants.IMAGE_FOLDER + File.separator + prefix + "_" + name;
			DataHelper.saveImageSegmentedBySuperPixels(imageDTO, superPixels, path);
		}
		return superPixels;
			
	}
	
	public static void initBorderData(ImageDTO image, List<SuperPixelDTO> superPixels) {
		PixelDTO [][] pixelData = image.getPixelData();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
            	PixelDTO currentPixel = pixelData[x][y];
            	int superPixelIndex = currentPixel.getSuperPixelIndex();
            	
            	//get neigbouring pixels
            	List<PixelDTO> adjacentPixels = image.getAdjacentPixels(x, y);
            	boolean isBorder = false;
            	for (PixelDTO adjacentPixel : adjacentPixels) {
            		if (adjacentPixel.getSuperPixelIndex() != superPixelIndex) {
            			isBorder = true;
            		}
            	}
            	currentPixel.setBorderPixel(isBorder);
            }
        }
        for (SuperPixelDTO superPixel : superPixels) {
        	superPixel.initBorderPixels();   
        }
	}
	
	public static void updateSuperPixelLabels(List<SuperPixelDTO> superPixels) {
		for (SuperPixelDTO superPixel : superPixels) {
			List<PixelDTO> pixels = superPixel.getPixels();
			List<Integer> labelOccurrences = new ArrayList<Integer>();
			for (int i = 0; i < Constants.NUMBER_OF_STATES; i++) {
				labelOccurrences.add(0);
			}
			// count label occurrences
			for (PixelDTO pixel : pixels) {
				int label = pixel.getLabel();
				if (label >= Constants.NUMBER_OF_STATES) label = 0; //Assign other colours as background
				int numberOfOccurances = labelOccurrences.get(label);
				labelOccurrences.set(label, ++numberOfOccurances);
			}
			// get most common label
			int occurreceMax = 0;
			int chosenLabel = -1;
			for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
				if (labelOccurrences.get(label) > occurreceMax) {
					occurreceMax = labelOccurrences.get(label);
					chosenLabel = label;
				}
			}
			if (chosenLabel == -1) {
				_log.error("updateSuperPixelLabels" + " chosen label -1");
				throw new RuntimeErrorException(null);
			}
			superPixel.setLabel(chosenLabel);
		}
		
	}
	
	public static List<SuperPixelDTO> getListNeighbours(List<SuperPixelDTO> superPixelList) {
		List<SuperPixelDTO> finalList = new ArrayList<SuperPixelDTO>();
		for (SuperPixelDTO sp : superPixelList){
			finalList.addAll(sp.getNeigbouringSuperPixels());
		}
		return finalList;
	}
	
	private static Logger _log = Logger.getLogger(SuperPixelHelper.class);
}
