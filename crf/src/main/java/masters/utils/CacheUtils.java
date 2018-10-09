package masters.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.NEW;

import masters.Constants;
import masters.image.ImageDTO;
import masters.image.PixelDTO;

public class CacheUtils {
	private static String SEPARATOR = ",";
	private static String NEW_LINE = System.getProperty("line.separator");
	
	public static void saveSuperPixelDivision(ImageDTO image, String prefix) {
		
		File imageFile = new File(image.getPath());
		String name = imageFile.getName();
		name = name.replaceAll("." + Constants.IMAGE_EXTENSION, "");
		String fileName = prefix + "_" + name;
		String path = Constants.WORK_PATH + Constants.IMAGE_FOLDER + File.separator + fileName + ".txt";
		
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
