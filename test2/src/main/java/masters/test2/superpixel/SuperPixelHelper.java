package masters.test2.superpixel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import masters.test2.Constants;
import masters.test2.colors.ColorSpaceException;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.image.ImageDTO;
import masters.test2.image.PixelDTO;

public class SuperPixelHelper {
	
	public static List<SuperPixelDTO> getSuperPixel(ImageDTO imageDTO, int expectedNumberOfSuperpixels, double rigidness) throws ColorSpaceException {
		Runtime runtime = Runtime.getRuntime();
		List <SuperPixelDTO> superPixels = new ArrayList<SuperPixelDTO>();
		try {
			String jarPath = System.getProperty("user.dir") + "\\src\\superpixel.jar";
			String command = "java -jar " + jarPath + " " + imageDTO.getPath() + " " + expectedNumberOfSuperpixels + " " + rigidness;
			Process proc = runtime.exec(command);
			
			BufferedReader stdInput = new BufferedReader(new 
				     InputStreamReader(proc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
			     InputStreamReader(proc.getErrorStream()));

			// read the output from the command
			Map<SuperPixelDTO,SuperPixelDTO> createdSuperPixels = new HashMap<SuperPixelDTO, SuperPixelDTO>();
			String s = null;
			int rowIndex = 0;
			PixelDTO[][] pixelData = imageDTO.pixelData;
			while ((s = stdInput.readLine()) != null) {
				String [] rowData = s.split(" ");
				for (int colIndex = 0; colIndex < rowData.length; colIndex++) {
					PixelDTO currentPixel = pixelData[colIndex][rowIndex];
					int superPixelIndex = Integer.valueOf(rowData[colIndex]);
					SuperPixelDTO tmpSuperPixel = new SuperPixelDTO(superPixelIndex);
					if (!createdSuperPixels.containsKey(tmpSuperPixel)) {
						superPixels.add(tmpSuperPixel);
						createdSuperPixels.put(tmpSuperPixel, tmpSuperPixel);
						tmpSuperPixel.addPixel(currentPixel);
					} else {
						SuperPixelDTO currentSuperPixel = createdSuperPixels.get(tmpSuperPixel);
						currentSuperPixel.addPixel(currentPixel);
					}
					currentPixel.setSuperPixelIdex(superPixelIndex);
					//System.out.print(superPixelIndex + " ");
				}
				rowIndex++;
				//System.out.println();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(superPixels);
		initBorderData(imageDTO, superPixels);
		for (SuperPixelDTO superPixel : superPixels) {
			superPixel.initFeatureVector();
			superPixel.initNeighbours(imageDTO.getPixelData(), superPixels);
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
			// count label occurances
			for (PixelDTO pixel : pixels) {
				int label = pixel.getLabel();
				if (label >= Constants.NUMBER_OF_STATES) label = 0; //asign other colours as background
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
				Constants._log.error("updateSuperPixelLabels" + " chosen label -1");
				throw new RuntimeErrorException(null);
			}
			superPixel.setLabel(chosenLabel);
		}
		
	}
}
