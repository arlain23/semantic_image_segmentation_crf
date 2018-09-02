package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import generator.App;
import masters.Constants;

public class FileUtil {
	public static void saveImage(BufferedImage img, String path, String fileName){
		try {
			File outputfile = new File(Constants.class.getClassLoader().getResource(path).getPath() + fileName);
			outputfile.getParentFile().mkdirs();
			outputfile.createNewFile();
			
	        ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
