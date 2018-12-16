package masters.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import masters.Constants;
import masters.image.ImageDTO;
import masters.image.PixelDTO;

public class CacheUtils {
	private static String SEPARATOR = ",";
	private static String NEW_LINE = System.getProperty("line.separator");
	
	
	public static List<List<Integer>> getSuperPixelDivision(ImageDTO image, String prefix) {
		
		try {
			File imageFile = new File(image.getPath());
			String name = imageFile.getName();
			name = name.replaceAll("." + Constants.IMAGE_EXTENSION, "");
			String fileName = prefix + "_" + name;
			String path = Constants.WORK_PATH + Constants.SUPERPIXEL_IMAGE_FOLDER + File.separator + fileName + ".txt";
			
			List<List<Integer>> superPixels = new ArrayList<List<Integer>>();
			
			BufferedReader br = new BufferedReader(new FileReader(path)); 
			String st; 
			while ((st = br.readLine()) != null) {
				List<Integer> row = Arrays.asList(st.split(SEPARATOR)).stream()
	                    .map(Integer::valueOf)
	                    .collect(Collectors.toList());
				superPixels.add(row);
				
			}
			br.close();
			return superPixels;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void saveSuperPixelDivision(ImageDTO image, String prefix) throws IOException {
		
		String name = DataHelper.getFileNameFromImageDTO(image);
		String fileName = prefix + "_" + name;
		String path = Constants.WORK_PATH + Constants.IMAGE_FOLDER + File.separator + fileName + ".txt";
		
		File file = new File(path);
		file.getParentFile().mkdirs();
		file.createNewFile();
		
		StringBuilder content = new StringBuilder();
		PixelDTO[][] pixelData = image.getPixelData();
		  for (int y = 0; y < pixelData.length; y++) {
			  for (int x = 0; x < pixelData[0].length; x++) {
				  int superPixelIndex = pixelData[x][y].getSuperPixelIndex();
				  content.append(superPixelIndex);
				  content.append(SEPARATOR);
			  }
			  content.append(NEW_LINE);
		}
		  
		  try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
				bw.write(content.toString());
		  } catch (IOException e) {
			  e.printStackTrace();

		  }
	}
}
