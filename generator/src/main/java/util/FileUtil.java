package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import masters.Constants;

public class FileUtil {
	public static void saveImage(BufferedImage img, String path, String fileName){
		try {
			
			File outputfile = new File("src/main/resources/" + path + "/" + fileName);
			System.out.println(outputfile.getAbsolutePath());
			outputfile.getParentFile().mkdirs();
			outputfile.createNewFile();
	        ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
