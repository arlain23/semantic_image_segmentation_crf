package masters.test2.superpixel;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import masters.test2.LabelHelper;
import masters.test2.image.ImageDTO;
import masters.test2.image.PixelDTO;

public class SuperPixelHelper {
	
	public static List<SuperPixelDTO> getSuperPixel(ImageDTO imageDTO, int expectedNumberOfSuperpixels, double rigidness) {
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
			superPixel.initMeanColours();
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
	
	public static void updateSuperPixelLabels(List<SuperPixelDTO> superPixels, int numberOfLabels) {
		for (SuperPixelDTO superPixel : superPixels) {
			List<PixelDTO> pixels = superPixel.getPixels();
			List<Integer> labelOccurences = new ArrayList<Integer>();
			for (int i = 0; i < numberOfLabels; i++) {
				labelOccurences.add(0);
			}
			// count label occurances
			for (PixelDTO pixel : pixels) {
				int label = pixel.getLabel();
				int numberOfOccurances = labelOccurences.get(label);
				labelOccurences.set(label, ++numberOfOccurances);
			}
			// get most common label
			int occuraceMax = 0;
			int chosenLabel = -1;
			for (int label = 0; label < labelOccurences.size(); label++) {
				if (labelOccurences.get(label) > occuraceMax) {
					occuraceMax = labelOccurences.get(label);
					chosenLabel = label;
				}
			}
			superPixel.setLabel(chosenLabel);
		}
		
	}
}
